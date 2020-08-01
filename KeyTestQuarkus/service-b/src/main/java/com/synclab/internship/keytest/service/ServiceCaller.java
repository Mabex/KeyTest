package com.synclab.internship.keytest.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.synclab.internship.keytest.KeyPairBean;
import com.synclab.internship.keytest.client.ServiceClient;
import com.synclab.internship.keytest.client.SimpleConsulRegistry;
import com.synclab.internship.keytest.model.Message;
import com.synclab.internship.keytest.model.Register;
import com.synclab.internship.keytest.util.AsyCripto;
import io.quarkus.scheduler.Scheduled;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Iterator;

@Singleton
public class ServiceCaller {

    private final KeyPair keyPair;
    private final AsyCripto asyCripto;
    private JsonNode query;

    @Inject
    @RestClient
    SimpleConsulRegistry registry;

    @ConfigProperty(name = "service-name")
    String serviceName;
    @ConfigProperty(name = "quarkus.http.port")
    int port;

    @Inject
    public ServiceCaller(KeyPairBean keyPairBean)
            throws NoSuchAlgorithmException, NoSuchPaddingException {
        this.keyPair = keyPairBean.generateKeyPair();
        this.asyCripto = new AsyCripto("RSA");
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public boolean register()
            throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException {

        registry.putRegistration(new Register(serviceName + port, serviceName, "127.0.0.1", port));

        Message message = new Message(
                serviceName,
                AsyCripto.keyToString(keyPair.getPublic()),
                asyCripto.encryptText(keyPair.getPrivate(), serviceName));

        ServiceClient serviceClient = buildClient("AC");

        try {
            System.out.println(serviceClient.postKey(message).readEntity(String.class));
        } catch (WebApplicationException exception) {
            System.out.println(exception.getResponse().readEntity(String.class));
            return false;
        }

        return true;
    }

    public boolean deRegister() {
        registry.putDeregistration(serviceName + port);
        return true;
    }

    public PublicKey getKey(String serviceName) throws InvalidKeySpecException, NoSuchAlgorithmException {

        ServiceClient serviceClient = buildClient("AC");
        try {
            Response response = serviceClient.getKey(serviceName);
            String keyString = response.readEntity(String.class);

            //System.out.println(keyString);
            return asyCripto.stringToKey(keyString);
        } catch (WebApplicationException exception) {
            System.out.println(exception.getResponse().readEntity(String.class));
            return null;
        }
    }

    public Message postMessage(String from, String to, PublicKey toKey, String text)
            throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException {

        ServiceClient serviceClient = buildClient(to);
        Message message = new Message(
                from,
                asyCripto.encryptText(toKey, text),
                asyCripto.encryptText(keyPair.getPrivate(), from));

        try {
            return serviceClient.postMessage(message).readEntity(Message.class);
        } catch (WebApplicationException exception) {
            System.out.println(exception.getResponse().readEntity(String.class));
            return null;
        }
    }

    private ServiceClient buildClient(String serviceName) {
        if (query == null) {
            queryRegistry();
        }

        Iterator<JsonNode> iter = query.elements();
        String thisAddress = "";
        String thisPort = "";
        while (iter.hasNext()) {
            JsonNode node = iter.next();
            JsonNode service = node.get("Service");

            if (serviceName.equals(service.asText()))
            {
                thisAddress = node.get("Address").asText();
                thisPort = node.get("Port").asText();
            }
        }

        return RestClientBuilder.newBuilder()
                .baseUri(URI.create("http://" + thisAddress + ":" + thisPort))
                .build(ServiceClient.class);
    }

    @Scheduled(every = "10s")
    public void queryRegistry() {
        if (registry != null) {
            query = registry.getServicesWithFilter("");
        }
    }
}

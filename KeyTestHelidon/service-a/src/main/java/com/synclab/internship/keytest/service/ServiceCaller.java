package com.synclab.internship.keytest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synclab.internship.keytest.KeyPairBean;
import com.synclab.internship.keytest.client.ServiceClient;
import com.synclab.internship.keytest.client.SimpleConsulRegistry;
import com.synclab.internship.keytest.model.Message;
import com.synclab.internship.keytest.model.Register;
import com.synclab.internship.keytest.util.AsyCripto;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Iterator;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class ServiceCaller{

    private final KeyPair keyPair;
    private final AsyCripto asyCripto;
    private String query;

    @Inject
    @RestClient
    SimpleConsulRegistry registry;

    @Inject
    @ConfigProperty(name = "app.name")
    String serviceName;
    @Inject
    @ConfigProperty(name = "server.port")
    int port;

    public ServiceCaller() {
        keyPair = null;
        asyCripto = null;
    }

    @Inject
    public ServiceCaller(KeyPairBean keyPairBean)
            throws NoSuchAlgorithmException, NoSuchPaddingException {
        this.keyPair = keyPairBean.generateKeyPair();
        this.asyCripto = new AsyCripto("RSA");

        System.out.println("start");
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        executor.scheduleAtFixedRate(this::queryRegistry, 0, 10, TimeUnit.SECONDS);
    }

    public boolean register()
            throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException, JsonProcessingException {

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

    public PublicKey getKey(String serviceName) throws InvalidKeySpecException, NoSuchAlgorithmException, JsonProcessingException {

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
            throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException, JsonProcessingException {

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

    private ServiceClient buildClient(String serviceName) throws JsonProcessingException {
        if (query == null) {
            queryRegistry();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode queryNode = objectMapper.readTree(query);

        Iterator<JsonNode> iter = queryNode.elements();
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

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public void queryRegistry() {
        if (registry != null) {
            query = registry.getServicesWithFilter("");
        }
    }
}

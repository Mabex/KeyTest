package com.synclab.internship.keytest.service;

import com.synclab.internship.keytest.KeyPairBean;
import com.synclab.internship.keytest.client.ServiceAcClient;
import com.synclab.internship.keytest.model.Message;
import com.synclab.internship.keytest.util.AsyCripto;
import io.micronaut.context.annotation.Property;
import io.micronaut.discovery.exceptions.NoAvailableServiceException;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.inject.Singleton;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

@Singleton
public class ServiceCaller {

    private final KeyPair keyPair;
    private final AsyCripto asyCripto;
    private final ServiceAcClient serviceAc;

    @Property(name = "micronaut.application.name")
    String serviceName;

    public ServiceCaller(KeyPairBean keyPairBean, ServiceAcClient serviceAc)
            throws NoSuchAlgorithmException, NoSuchPaddingException {
        this.keyPair = keyPairBean.generateKeyPair();
        this.serviceAc = serviceAc;
        this.asyCripto = new AsyCripto("RSA");
    }

    public boolean register()
            throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException {

        Message message = new Message(
                serviceName,
                AsyCripto.keyToString(keyPair.getPublic()),
                asyCripto.encryptText(keyPair.getPrivate(), serviceName));
        try {
            System.out.println(serviceAc.postKey(message).body());
        } catch (Exception exception) {
            if (exception instanceof NoAvailableServiceException) {
                System.out.println("There was a problem with service: " + ((NoAvailableServiceException) exception).getServiceID());
            } else {
                System.out.println(exception.getMessage());
            }
            return false;
        }
        return true;
    }

    public PublicKey getKey(String serviceName) {
        try {
            HttpResponse<String> response = serviceAc.getKey(serviceName);
            if (response.getStatus().equals(HttpStatus.OK)) {
                return asyCripto.stringToKey(response.body());
            } else {
                System.out.println(response.getStatus());
                return null;
            }
        } catch (Exception exception) {
            if (exception instanceof NoAvailableServiceException) {
                System.out.println("There was a problem with service: " + ((NoAvailableServiceException) exception).getServiceID());
            } else {
                System.out.println(exception.getMessage());
            }
            return null;
        }
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }
}

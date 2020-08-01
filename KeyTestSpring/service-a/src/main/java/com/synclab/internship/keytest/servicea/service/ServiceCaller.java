package com.synclab.internship.keytest.servicea.service;

import com.synclab.internship.keytest.model.Message;
import com.synclab.internship.keytest.servicea.util.AsyCripto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

@Service
public class ServiceCaller {

    private final RestTemplate restTemplate;
    private final KeyPair keyPair;
    private final AsyCripto asyCripto;

    @Autowired
    public ServiceCaller(RestTemplate restTemplate, KeyPair keyPair)
            throws NoSuchAlgorithmException, NoSuchPaddingException {
        this.restTemplate = restTemplate;
        this.keyPair = keyPair;
        asyCripto = new AsyCripto("RSA");
    }

    public boolean register(String serviceName) throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException {

        Message message = new Message(
                serviceName,
                AsyCripto.keyToString(keyPair.getPublic()),
                asyCripto.encryptText(keyPair.getPrivate(), serviceName));

        try {
            ResponseEntity<String> response = restTemplate.postForEntity("http://AC/key", message, String.class);
            System.out.println(response);
        } catch (Exception exception) {
            System.out.println("* Failed registration. Exiting. Cause: " + exception.getMessage());
            return false;
        }
        return true;
    }

    public PublicKey getKey(String serviceName) {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity("http://AC/key/" + serviceName, String.class);
            //System.out.println(response);
            return asyCripto.stringToKey(response.getBody());
        } catch (Exception exception) {
            System.out.println("* Failed getting the key. Exiting. Cause: " + exception.getMessage());
            return null;
        }
    }

    public Message postMessage(String from, String to, PublicKey toKey, String text)
            throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException {

        Message message = new Message(
                from,
                asyCripto.encryptText(toKey, text),
                asyCripto.encryptText(keyPair.getPrivate(), from));

        try {
            ResponseEntity<Message> response = restTemplate.postForEntity("http://" + to + "/message", message, Message.class);
            //System.out.println(response);
            return response.getBody();
        } catch (Exception exception) {
            System.out.println("* Failed sending the message. Exiting. Cause: " + exception.getMessage());
            return null;
        }
    }
}

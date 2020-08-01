package com.synclab.internship.keytest.serviceac.controller;

import com.synclab.internship.keytest.model.Message;
import com.synclab.internship.keytest.serviceac.model.KeyEntry;
import com.synclab.internship.keytest.serviceac.util.AsyCripto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;


@RestController
public class ServiceAcController {

    private List<KeyEntry> keyEntries = new ArrayList<>();
    private AsyCripto asyCripto;

    @PostMapping("/key")
    public ResponseEntity<String> postKey(@RequestBody Message message)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException,
            BadPaddingException, InvalidKeyException, IllegalBlockSizeException {

        asyCripto = new AsyCripto("RSA");
        PublicKey publicKey = asyCripto.stringToKey(message.getBody());

        if (!message.getFrom().equals(asyCripto.decryptText(publicKey, message.getSignature()))) {
            return ResponseEntity.ok("Signature is invalid.");
        }

        boolean notFound = true;
        for (KeyEntry e : keyEntries) {
            if (e.getServiceName().equals(message.getFrom())) {
                e.setPublicKey(message.getBody());
                notFound = false;
                break;
            }
        }
        if (notFound) {
            KeyEntry entry = new KeyEntry(message.getFrom(), message.getBody());
            keyEntries.add(entry);
            //System.out.println(entry);
        }

        return ResponseEntity.ok("Registration Complete");
    }

    @GetMapping("/key/{service}")
    public ResponseEntity<String> getKey(@PathVariable String service) {

        for (KeyEntry e : keyEntries) {
            if (e.getServiceName().equals(service)) {
                return ResponseEntity.ok(e.getPublicKey());
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Key for specified service was not found.");
    }
}

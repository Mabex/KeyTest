package com.synclab.internship.keytest.servicea.controller;

import com.synclab.internship.keytest.model.Message;
import com.synclab.internship.keytest.servicea.service.ServiceCaller;
import com.synclab.internship.keytest.servicea.util.AsyCripto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyPair;
import java.security.PublicKey;

@RestController
public class ServiceAController implements CommandLineRunner {

    private final ServiceCaller serviceCaller;
    private final KeyPair keyPair;
    private boolean ready;

    @Autowired
    public ServiceAController(ServiceCaller serviceCaller, KeyPair keyPair) {
        this.serviceCaller = serviceCaller;
        this.keyPair = keyPair;
    }

    @GetMapping("/message/{message}")
    public ResponseEntity<String> sendMessage(@PathVariable String message) {

        if (!ready) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        try {
            PublicKey keyB = serviceCaller.getKey("B");
            if (keyB != null) {
                Message response = serviceCaller.postMessage("A", "B", keyB, message);
                if (response != null) {
                    AsyCripto asyCripto = new AsyCripto("RSA");
                    String received = asyCripto.decryptText(keyPair.getPrivate(), response.getBody());
                    return ResponseEntity.ok(received);
                }
            }
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @Override
    public void run(String... args) throws Exception {

        if (serviceCaller.register("A")) {
            ready = true;
        }
    }
}

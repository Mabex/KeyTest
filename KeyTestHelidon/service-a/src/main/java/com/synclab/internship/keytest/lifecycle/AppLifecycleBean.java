package com.synclab.internship.keytest.lifecycle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.synclab.internship.keytest.model.Message;
import com.synclab.internship.keytest.service.ServiceCaller;
import com.synclab.internship.keytest.util.AsyCripto;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.BeforeDestroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

@ApplicationScoped
public class AppLifecycleBean {

    @Inject
    ServiceCaller serviceCaller;

    void onStart(@Observes @Initialized(ApplicationScoped.class) final Object event)
            throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException,
            InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, JsonProcessingException {

        if (serviceCaller.register()) {/*
            PublicKey keyB = serviceCaller.getKey("B");
            if (keyB != null) {
                Message response = serviceCaller.postMessage("A","B", keyB, "Secret!");
                if (response != null) {
                    AsyCripto asyCripto = new AsyCripto("RSA");
                    System.out.println(asyCripto.decryptText(serviceCaller.getKeyPair().getPrivate(), response.getBody()));
                }
            }*/
        }
    }

    void onStop(@Observes @BeforeDestroyed(ApplicationScoped.class) final Object event) {
        serviceCaller.deRegister();
    }

}

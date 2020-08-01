package com.synclab.internship.keytest;

import com.synclab.internship.keytest.model.Message;
import com.synclab.internship.keytest.service.ServiceCaller;
import com.synclab.internship.keytest.util.AsyCripto;
import io.micronaut.discovery.event.ServiceStartedEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.scheduling.annotation.Async;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.inject.Singleton;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

@Singleton
public class StartupBean {

    private ServiceCaller serviceCaller;

    public StartupBean(ServiceCaller serviceCaller) {
        this.serviceCaller = serviceCaller;
    }

    @EventListener
    @Async
    public void onStartup(final ServiceStartedEvent event)
            throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException,
            NoSuchAlgorithmException, NoSuchPaddingException {

        if (serviceCaller.register()) {/*
            PublicKey keyB = serviceCaller.getKey("service-b");
            if (keyB != null) {
                Message response = serviceCaller.postMessage("service-a","service-b", keyB, "Secret!");
                if (response != null) {
                    AsyCripto asyCripto = new AsyCripto("RSA");
                    System.out.println(asyCripto.decryptText(serviceCaller.getKeyPair().getPrivate(), response.getBody()));
                }
            }*/
        }
    }
}

package com.synclab.internship.keytest.lifecycle;

import com.fasterxml.jackson.databind.JsonNode;
import com.synclab.internship.keytest.model.Message;
import com.synclab.internship.keytest.model.Register;
import com.synclab.internship.keytest.client.SimpleConsulRegistry;
import com.synclab.internship.keytest.service.ServiceCaller;
import com.synclab.internship.keytest.util.AsyCripto;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;

@ApplicationScoped
public class AppLifecycleBean {

    @Inject
    ServiceCaller serviceCaller;

    void onStart(@Observes StartupEvent event)
            throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException,
            InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException {

        if (serviceCaller.register()) {
            /*PublicKey keyB = serviceCaller.getKey("B");
            if (keyB != null) {
                Message response = serviceCaller.postMessage("A","B", keyB, "Secret!");
                if (response != null) {
                    AsyCripto asyCripto = new AsyCripto("RSA");
                    System.out.println(asyCripto.decryptText(serviceCaller.getKeyPair().getPrivate(), response.getBody()));
                }
            }*/
        }
    }

    void onStop(@Observes ShutdownEvent event) {
        serviceCaller.deRegister();
    }

}

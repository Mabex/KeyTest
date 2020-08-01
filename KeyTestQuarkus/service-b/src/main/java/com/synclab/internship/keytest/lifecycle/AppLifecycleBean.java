package com.synclab.internship.keytest.lifecycle;

import com.synclab.internship.keytest.model.Message;
import com.synclab.internship.keytest.service.ServiceCaller;
import com.synclab.internship.keytest.util.AsyCripto;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

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

@ApplicationScoped
public class AppLifecycleBean {

    @Inject
    ServiceCaller serviceCaller;

    void onStart(@Observes StartupEvent event)
            throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException {

        serviceCaller.register();
    }

    void onStop(@Observes ShutdownEvent event) {
        serviceCaller.deRegister();
    }

}

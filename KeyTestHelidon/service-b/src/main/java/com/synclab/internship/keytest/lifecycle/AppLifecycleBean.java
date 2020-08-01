package com.synclab.internship.keytest.lifecycle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.synclab.internship.keytest.service.ServiceCaller;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.BeforeDestroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.security.InvalidKeyException;

@ApplicationScoped
public class AppLifecycleBean {

    @Inject
    ServiceCaller serviceCaller;

    void onStart(@Observes @Initialized(ApplicationScoped.class) final Object event)
            throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException, JsonProcessingException {

        serviceCaller.register();
    }

    void onStop(@Observes @BeforeDestroyed(ApplicationScoped.class) final Object event) {
        serviceCaller.deRegister();
    }

}

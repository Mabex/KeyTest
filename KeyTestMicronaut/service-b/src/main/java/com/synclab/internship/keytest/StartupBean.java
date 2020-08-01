package com.synclab.internship.keytest;

import com.synclab.internship.keytest.service.ServiceCaller;
import io.micronaut.discovery.event.ServiceStartedEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.scheduling.annotation.Async;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.inject.Singleton;
import java.security.InvalidKeyException;

@Singleton
public class StartupBean {

    private ServiceCaller serviceCaller;

    public StartupBean(ServiceCaller serviceCaller) {
        this.serviceCaller = serviceCaller;
    }

    @EventListener
    @Async
    public void onStartup(final ServiceStartedEvent event)
            throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException {
        serviceCaller.register();
    }
}

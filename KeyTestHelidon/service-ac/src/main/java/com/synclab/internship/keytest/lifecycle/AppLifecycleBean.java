package com.synclab.internship.keytest.lifecycle;

import com.synclab.internship.keytest.client.SimpleConsulRegistry;
import com.synclab.internship.keytest.model.Register;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.BeforeDestroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class AppLifecycleBean {

    @Inject
    @RestClient
    SimpleConsulRegistry registry;

    @Inject
    @ConfigProperty(name = "app.name")
    String serviceName;
    @Inject
    @ConfigProperty(name = "server.port")
    int port;

    void onStart(@Observes @Initialized(ApplicationScoped.class) final Object event) {
        registry.putRegistration(new Register(serviceName + port, serviceName, "127.0.0.1", port));
    }

    void onStop(@Observes @BeforeDestroyed(ApplicationScoped.class) final Object event) {
        registry.putDeregistration(serviceName + port);;
    }

}

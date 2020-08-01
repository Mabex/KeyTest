package com.synclab.internship.keytest.lifecycle;

import com.fasterxml.jackson.databind.JsonNode;
import com.synclab.internship.keytest.client.SimpleConsulRegistry;
import com.synclab.internship.keytest.model.Register;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class AppLifecycleBean {

    @Inject
    @RestClient
    SimpleConsulRegistry registry;

    @ConfigProperty(name = "service-name")
    String serviceName;
    @ConfigProperty(name = "quarkus.http.port")
    int port = 8080;

    void onStart(@Observes StartupEvent event) {
        registry.putRegistration(new Register(serviceName + port, serviceName, "127.0.0.1", port));
        JsonNode query = registry.getServicesWithFilter("Service == AC");
        System.out.println(query.findValue("Address"));
        System.out.println(query.findValue("Port"));
    }

    void onStop(@Observes ShutdownEvent event) {
        registry.putDeregistration(serviceName + port);
    }

}

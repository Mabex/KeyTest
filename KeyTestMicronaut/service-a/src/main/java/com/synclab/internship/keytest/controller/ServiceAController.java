package com.synclab.internship.keytest.controller;

import com.synclab.internship.keytest.model.Message;
import com.synclab.internship.keytest.service.ServiceCaller;
import com.synclab.internship.keytest.util.AsyCripto;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;

import java.security.PublicKey;

@Controller
public class ServiceAController {

    private final ServiceCaller serviceCaller;

    public ServiceAController(ServiceCaller serviceCaller) {
        this.serviceCaller = serviceCaller;
    }

    @Get("message/{message}")
    public HttpResponse<String> sendMessage(@PathVariable String message) {

        try {
            PublicKey keyB = serviceCaller.getKey("service-b");
            if (keyB != null) {
                Message response = serviceCaller.postMessage("service-a","service-b", keyB, "Secret!");
                if (response != null) {
                    AsyCripto asyCripto = new AsyCripto("RSA");
                    String received = asyCripto.decryptText(serviceCaller.getKeyPair().getPrivate(), response.getBody());
                    return HttpResponse.ok(received);
                }
            }
        } catch (Exception exception) {
            return HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

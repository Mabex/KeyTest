package com.synclab.internship.keytest.controller;

import com.synclab.internship.keytest.model.Message;
import com.synclab.internship.keytest.service.ServiceCaller;
import com.synclab.internship.keytest.util.AsyCripto;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.security.PublicKey;

@Path("/message")
@ApplicationScoped
public class ServiceAController {

    @Inject
    ServiceCaller serviceCaller;

    @Path("/{message}")
    @GET
    public Response sendMessage(@PathParam("message") String message) {

        try {
            PublicKey keyB = serviceCaller.getKey("B");
            if (keyB != null) {
                Message response = serviceCaller.postMessage("A","B", keyB, "Secret!");
                if (response != null) {
                    AsyCripto asyCripto = new AsyCripto("RSA");
                    String received = asyCripto.decryptText(serviceCaller.getKeyPair().getPrivate(), response.getBody());
                    return Response.ok(received).build();
                }
            }
        } catch (Exception exception) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}

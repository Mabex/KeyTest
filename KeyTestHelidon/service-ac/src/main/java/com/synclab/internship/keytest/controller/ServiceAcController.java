package com.synclab.internship.keytest.controller;

import com.synclab.internship.keytest.model.KeyEntry;
import com.synclab.internship.keytest.model.Message;
import com.synclab.internship.keytest.util.AsyCripto;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

@Path("/key")
@ApplicationScoped
public class ServiceAcController {

    private List<KeyEntry> keyEntries = new ArrayList<>();
    private AsyCripto asyCripto;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postKey(Message message)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException,
            BadPaddingException, InvalidKeyException, IllegalBlockSizeException {

        asyCripto = new AsyCripto("RSA");
        PublicKey publicKey = asyCripto.stringToKey(message.getBody());

        if (!message.getFrom().equals(asyCripto.decryptText(publicKey, message.getSignature()))) {
            //return "Signature is invalid.";
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Signature is invalid.").build();
        }

        boolean notFound = true;
        for (KeyEntry e : keyEntries) {
            if (e.getServiceName().equals(message.getFrom())) {
                e.setPublicKey(message.getBody());
                notFound = false;
                break;
            }
        }
        if (notFound) {
            KeyEntry entry = new KeyEntry(message.getFrom(), message.getBody());
            keyEntries.add(entry);
            //System.out.println(entry);
        }

        return Response.ok("Registration Complete").build();
        //return "Registration Complete";
    }

    @GET
    @Path("/{service}")
    public Response getKey(@PathParam("service") String service) {

        //System.out.println(service);
        for (KeyEntry e : keyEntries) {
            if (e.getServiceName().equals(service)) {
                return Response.ok(e.getPublicKey()).build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity("Key for specified service was not found.").build();
    }
}

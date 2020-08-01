package com.synclab.internship.keytest.controller;

import com.synclab.internship.keytest.model.KeyEntry;
import com.synclab.internship.keytest.model.Message;
import com.synclab.internship.keytest.util.AsyCripto;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

@Controller("/key")
public class ServiceAcController {

    private List<KeyEntry> keyEntries = new ArrayList<>();
    private AsyCripto asyCripto;

    @Post
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<String> postKey(Message message)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException,
            BadPaddingException, InvalidKeyException, IllegalBlockSizeException {

        asyCripto = new AsyCripto("RSA");
        PublicKey publicKey = asyCripto.stringToKey(message.getBody());

        if (!message.getFrom().equals(asyCripto.decryptText(publicKey, message.getSignature()))) {
            return HttpResponse.badRequest("Signature is invalid.");
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

        return HttpResponse.ok("Registration Complete");
    }

    @Get("/{service}")
    public HttpResponse<String> getKey(@PathVariable String service) {

        for (KeyEntry e : keyEntries) {
            if (e.getServiceName().equals(service)) {
                return HttpResponse.ok(e.getPublicKey());
            }
        }
        return HttpResponse.notFound("Key for specified service was not found.");
    }
}

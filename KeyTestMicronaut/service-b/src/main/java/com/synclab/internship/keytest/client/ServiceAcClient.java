package com.synclab.internship.keytest.client;

import com.synclab.internship.keytest.model.Message;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.client.annotation.Client;

@Client(id = "service-ac")
public interface ServiceAcClient {

    @Post("/key")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<String> postKey(Message message);

    @Get("/key/{key}")
    @Produces(MediaType.TEXT_PLAIN)
    public HttpResponse<String> getKey(@PathVariable String key);
}

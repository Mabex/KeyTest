package com.synclab.internship.keytest.client;

import com.synclab.internship.keytest.model.Message;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RegisterRestClient(configKey = "service-api")
public interface ServiceClient {

    @POST
    @Path("/key")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postKey(Message message);

    @GET
    @Path("/key/{key}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getKey(@PathParam("key") String key);

    @POST
    @Path("/message")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postMessage(Message message);
}

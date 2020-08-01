package com.synclab.internship.keytest.client;

import com.synclab.internship.keytest.model.Register;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/v1")
@RegisterRestClient(configKey = "consul-api")
public interface SimpleConsulRegistry {

    @PUT
    @Retry(maxRetries = 3, delay = 5000)
    @Path("/agent/service/register")
    @Consumes(MediaType.APPLICATION_JSON)
    public void putRegistration(Register registration);

    @PUT
    @Retry(maxRetries = 3, delay = 5000)
    @Path("/agent/service/deregister/{id}")
    public void putDeregistration(@PathParam("id") String id);

    @GET
    @Retry(maxRetries = 3, delay = 5000)
    @Path("/agent/services")
    @Produces(MediaType.APPLICATION_JSON)
    public String getServicesWithFilter(@QueryParam("filter") String name);

}

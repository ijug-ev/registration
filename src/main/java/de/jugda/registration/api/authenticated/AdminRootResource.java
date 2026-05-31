package de.jugda.registration.api.authenticated;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Path("admin/{tenant}")
@Produces(MediaType.TEXT_HTML)
@Authenticated
public class AdminRootResource {

    @Context
    UriInfo uriInfo;

    @GET
    public Response get() {
        return Response.status(Response.Status.FOUND)
            .location(uriInfo.getRequestUriBuilder().path("events").build())
            .build();
    }

}

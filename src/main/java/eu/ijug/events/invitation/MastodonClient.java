package eu.ijug.events.invitation;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "mastodon")
@Path("/api/v1/statuses")
public interface MastodonClient {

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    Response createStatus(
        @HeaderParam("Authorization") String authorization,
        @FormParam("status") String status,
        @FormParam("visibility") String visibility
    );
}

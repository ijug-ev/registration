package de.jugda.registration;

import de.jugda.registration.model.EventDto;
import de.jugda.registration.service.EventService;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.runtime.LaunchMode;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Path("webinar/{tenant}")
@Produces(MediaType.TEXT_HTML)
public class WebinarResource {

    @Inject
    EventService eventService;
    @Inject
    LaunchMode launchMode;
    @Inject
    Config config;
    @Location("webinar/webinar")
    Template webinar;
    @Location("webinar/notAvailable")
    Template webinarNotAvailable;

    @Inject
    TenantContext tenantCtx;

    @GET
    @Path("{eventId}")
    public TemplateInstance getWebinar(@PathParam("eventId") String eventId) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        if (!launchMode.isDevOrTest() && !eventId.equals(today)) {
            return webinarNotAvailable.data("tenant", tenantCtx.getTenant());
        }

        EventDto event = eventService.getEvent(eventId);

        return webinar.data("event", event)
            .data("tenant", tenantCtx.getTenant())
            .data("eventData", event)
            .data("helptext", config.page().webinar());
    }

}

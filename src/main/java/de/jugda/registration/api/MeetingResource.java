package de.jugda.registration.api;

import de.jugda.registration.TenantContext;
import de.jugda.registration.domain.Content;
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
import java.util.Map;

@Path("meeting/{tenant}")
@Produces(MediaType.TEXT_HTML)
public class MeetingResource {

    @Inject
    EventService eventService;
    @Inject
    LaunchMode launchMode;
    @Location("meeting/meeting")
    Template meeting;
    @Location("meeting/notAvailable")
    Template meetingNotAvailable;

    @Inject
    TenantContext tenantCtx;

    @GET
    @Path("{eventId}")
    public TemplateInstance getMeeting(@PathParam("eventId") String eventId) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        if (!launchMode.isDevOrTest() && !eventId.equals(today)) {
            return meetingNotAvailable.data("tenant", tenantCtx.getTenant());
        }

        return eventService.getEvent(eventId)
            .map(event -> {
                Map<String, String> eventData = eventService.getEventData(eventId);
                return meeting.data("event", event)
                    .data("tenant", tenantCtx.getTenant())
                    .data("eventData", eventData)
                    .data("helptext", Content.asMap());
            })
            .orElseGet(() -> meetingNotAvailable.data("tenant", tenantCtx.getTenant()));
    }

}

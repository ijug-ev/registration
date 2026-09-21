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

    /** Key of the per-event meeting link in {@code event_data}, written by the admin event page. */
    static final String MEETING_LINK = "meetingLink";

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
            return notAvailable();
        }

        Map<String, String> eventData = eventService.getEventData(eventId);
        // The link is the whole page. Until the orga team pastes it in -- which happens on the day,
        // sometimes minutes before the start -- there is nothing to show, and the handbuch has always
        // promised "nicht verfügbar" for that. The page used to answer 500 instead, because Qute
        // renders strictly and {eventData.meetingLink} has no key to resolve.
        String meetingLink = eventData.get(MEETING_LINK);
        if (meetingLink == null || meetingLink.isBlank()) {
            return notAvailable();
        }

        return eventService.getEvent(eventId)
            .map(event -> meeting.data("event", event)
                .data("tenant", tenantCtx.getTenant())
                .data("eventData", eventData)
                .data("helptext", Content.asMap()))
            .orElseGet(this::notAvailable);
    }

    private TemplateInstance notAvailable() {
        return meetingNotAvailable.data("tenant", tenantCtx.getTenant());
    }

}

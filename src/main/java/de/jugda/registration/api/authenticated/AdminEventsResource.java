package de.jugda.registration.api.authenticated;

import de.jugda.registration.TenantContext;
import de.jugda.registration.model.EventDto;
import de.jugda.registration.model.RegistrationDto;
import de.jugda.registration.service.EmailService;
import de.jugda.registration.service.EventService;
import de.jugda.registration.service.ListService;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Path("admin/{tenant}/events")
@Produces(MediaType.TEXT_HTML)
@Authenticated
public class AdminEventsResource {

    @Inject
    ListService listService;
    @Inject
    EventService eventService;
    @Inject
    EmailService emailService;
    @Location("admin/overview")
    Template overview;
    @Location("admin/list")
    Template list;

    @Inject
    TenantContext tenantCtx;
    @Context
    UriInfo uriInfo;

    @GET
    public TemplateInstance getAllEvents() {
        Map<String, Integer> events = listService.allEvents();
        return overview
            .data("tenant", tenantCtx.getTenant())
            .data("events", events);
    }

    @GET
    @Path("{eventId}")
    public TemplateInstance getEventList(@PathParam("eventId") String eventId) {
        List<RegistrationDto> registrations = listService.singleEventRegistrations(eventId);
        EventDto event = eventService.getEvent(eventId).orElse(null);
        Map<String, String> eventData = eventService.getEventData(eventId);

        return list.data("eventId", eventId)
            .data("event", event)
            .data("eventData", eventData)
            .data("tenant", tenantCtx.getTenant())
            .data("baseUrl", uriInfo.getBaseUri())
            .data("registrations", registrations);
    }

    @GET
    @Path("{eventId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<RegistrationDto> getRegistrationList(@PathParam("eventId") String eventId) {
        return listService.singleEventRegistrations(eventId);
    }

    @PUT
    @Path("{eventId}/data")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response putEventData(@PathParam("eventId") String eventId, Map<String, String> eventData) {
        eventService.putEventData(eventId, eventData);
        return Response.noContent().build();
    }

    @PUT
    @Path("{eventId}/message")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response sendMessage(@PathParam("eventId") String eventId, Map<String, Object> data) {
        String subject = (String) data.get("subject");
        String message = (String) data.get("message");

        //noinspection unchecked
        List<String> registrationIds = (List<String>) data.get("registrationIds");
        if (null == registrationIds) {
            throw new IllegalArgumentException("Data does not contain any registrationIds");
        }

        AtomicInteger index = new AtomicInteger(0);
        Collection<List<RegistrationDto>> chunkedRegistrations = listService.singleEventRegistrations(eventId).stream()
            .filter(registration -> registrationIds.contains(registration.getId()))
            .collect(Collectors.groupingBy(x -> index.getAndIncrement() / 50)).values();

        if (!chunkedRegistrations.isEmpty()) {
            emailService.sendBulkEmail(chunkedRegistrations, subject, message);
        }

        return Response.noContent().build();
    }

}

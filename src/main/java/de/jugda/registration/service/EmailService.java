package de.jugda.registration.service;

import de.jugda.registration.TenantContext;
import de.jugda.registration.model.EventDto;
import de.jugda.registration.model.RegistrationDto;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.qute.Location;
import io.quarkus.qute.Qute;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.UriInfo;

import java.util.Collection;
import java.util.List;

@ApplicationScoped
public class EmailService {

    @Inject
    Mailer mailer;
    @Inject
    EventService eventService;
    @Location("mail/registration")
    Template tplRegistration;
    @Location("mail/waitlist2attendee")
    Template tplWaitlist2attendee;

    @Inject
    TenantContext tenantCtx;
    @Inject
    UriInfo uriInfo;

    void sendRegistrationConfirmation(RegistrationDto registration) {
        EventDto event = eventService.getEvent(registration.eventId);
        String subject = String.format("[%s] Anmeldebestätigung für \"%s\" am %s",
            tenantCtx.getTenant().getName(), event.summary, event.startDate());

        String mailBody = tplRegistration
            .data("tenant", tenantCtx.getTenant())
            .data("registration", registration)
            .data("event", event)
            .data("baseUrl", uriInfo.getBaseUri())
            .render();

        sendEmail(registration, subject, mailBody);
    }

    void sendWaitlistToAttendeeConfirmation(RegistrationDto registration) {
        EventDto event = eventService.getEvent(registration.eventId);
        String subject = String.format("[%s] Dein Wartelisten-Eintrag für \"%s\" am %s",
            tenantCtx.getTenant().getName(), event.summary, event.startDate());

        String mailBody = tplWaitlist2attendee
            .data("tenant", tenantCtx.getTenant())
            .data("registration", registration)
            .data("event", event)
            .data("baseUrl", uriInfo.getBaseUri())
            .render();

        sendEmail(registration, subject, mailBody);
    }

    private void sendEmail(RegistrationDto registration, String subject, String mailBody) {
        Mail mail = Mail.withHtml(registration.email, subject, mailBody);
        sendMail(mail);
    }

    public void sendBulkEmail(Collection<List<RegistrationDto>> chunkedRegistrations, String templateName, String subject, String body) {
        // TODO what about templateName?
        Qute.Fmt messageTemplate = Qute.fmt(body)
            .data("tenant", tenantCtx.getTenant());

        chunkedRegistrations.stream().flatMap(Collection::stream).forEach(registration -> {
            String emailMessage = messageTemplate
                .data("name", registration.getName())
                .data("eventId", registration.eventId).render();
            Mail mail = Mail.withHtml(
                registration.email,
                Qute.fmt(subject).data("name", registration.name).render(),
                emailMessage
            );
            sendMail(mail);
        });
    }

    private void sendMail(Mail mail) {
        mail.setReplyTo(tenantCtx.getTenant().getReplyTo());
        mailer.send(mail);
    }

}

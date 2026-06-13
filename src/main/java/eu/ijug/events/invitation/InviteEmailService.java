package eu.ijug.events.invitation;

import de.jugda.registration.model.EventDto;
import io.quarkus.logging.Log;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
class InviteEmailService {

    private static final List<Integer> MAIL_DAYS = List.of(2, 7, 28);

    @Inject
    Mailer mailer;

    @Location("invites/mail_announcement")
    Template tplAnnouncement;

    @Location("invites/mail_invitation")
    Template tplInvitation;

    void sendIfDue(InviteConfig config, EventDto event, int daysUntilEvent) {
        if (event.isExternalEvent()) {
            return;
        }
        if (!MAIL_DAYS.contains(daysUntilEvent)) {
            return;
        }
        if (daysUntilEvent == 28 && event.isHideRegistration()) {
            return;
        }

        Log.infof("Preparing invite mail for event %s in %d days (tenant: %s)", event.uid, daysUntilEvent, config.getTenantId());

        boolean isAnnouncement = daysUntilEvent == 28;
        String subject = (isAnnouncement ? "Ankündigung für " : "") + event.startDate() + ": " + event.summary;

        Template tpl = isAnnouncement ? tplAnnouncement : tplInvitation;
        String htmlBody = tpl
            .data("event", event)
            .render();

        Mail mail = Mail.withHtml(config.getMailTo(), subject, htmlBody);
        mailer.send(mail);
        Log.infof("Invite mail sent for event %s to %s", event.uid, config.getMailTo());
    }
}

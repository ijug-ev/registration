package eu.ijug.events.invitation;

import de.jugda.registration.TenantContext;
import de.jugda.registration.model.EventDto;
import de.jugda.registration.service.EventService;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@ApplicationScoped
public class InviteJob {

    @Inject
    TenantContext tenantCtx;

    @Inject
    EventService eventService;

    @Inject
    InviteEmailService inviteEmailService;

    @Inject
    InviteMastodonService inviteMastodonService;

    @Scheduled(cron = "0 0 10 * * ?", identity = "invite-job")
    @Transactional
    @ActivateRequestContext
    void run() {
        List<InviteConfig> configs = InviteConfig.listAll();
        if (configs.isEmpty()) {
            Log.info("No tenant invite configs found, skipping invite job");
            return;
        }

        LocalDate today = LocalDate.now();

        for (InviteConfig config : configs) {
            tenantCtx.setTenantId(config.getTenantId());
            tenantCtx.setTenant(null);

            Log.infof("Processing invite job for tenant: %s", config.getTenantId());
            try {
                List<EventDto> events = eventService.getAllEvents();
                for (EventDto event : events) {
                    if (event.start == null) {
                        continue;
                    }
                    int daysUntilEvent = (int) ChronoUnit.DAYS.between(today, event.start.toLocalDate());
                    Log.debugf("Event %s: %d days until event", event.uid, daysUntilEvent);
                    inviteEmailService.sendIfDue(config, event, daysUntilEvent);
                    inviteMastodonService.tootIfDue(config, event, daysUntilEvent);
                }
            } catch (Exception e) {
                Log.errorf(e, "Error processing invite job for tenant %s", config.getTenantId());
            }
        }
    }
}

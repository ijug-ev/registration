package eu.ijug.events.invitation;

import de.jugda.registration.model.EventDto;
import io.quarkus.logging.Log;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import java.net.URI;
import java.util.List;

@ApplicationScoped
class InviteMastodonService {

    private static final List<Integer> TOOT_DAYS = List.of(0, 2, 7, 28);

    @Location("invites/toot_invitation.txt")
    Template tplToot;

    void tootIfDue(InviteConfig config, EventDto event, int daysUntilEvent) {
        if (!TOOT_DAYS.contains(daysUntilEvent)) {
            return;
        }
        if (config.getMastoUrl() == null || config.getMastoToken() == null) {
            return;
        }

        Log.infof("Preparing toot for event %s in %d days (tenant: %s)", event.uid, daysUntilEvent, config.getTenantId());

        String day = daysUntilEvent == 0 ? "!!!HEUTE!!! " : event.startDate();
        String statusText = tplToot
            .data("event", event)
            .data("day", day)
            .render();

        MastodonClient client = RestClientBuilder.newBuilder()
            .baseUri(URI.create(config.getMastoUrl()))
            .build(MastodonClient.class);

        try (var response = client.createStatus("Bearer " + config.getMastoToken(), statusText.trim(), "public")) {
            Log.infof("Tooted for event %s (status: %d)", event.uid, response.getStatus());
        }
    }
}

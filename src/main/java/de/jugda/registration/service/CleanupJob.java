package de.jugda.registration.service;

import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@ApplicationScoped
public class CleanupJob {

    @Inject
    EntityManager em;

    @Scheduled(cron = "0 0 1 * * ?", identity = "cleanup-outdated-registrations")
    @Transactional
    @ActivateRequestContext
    void run() {
        long cutoff = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

        int removed = em.createNativeQuery("DELETE FROM registration WHERE ttl < ?1")
            .setParameter(1, cutoff)
            .executeUpdate();

        Log.infof("Nightly maintenance: removed %d old and outdated registrations (cross-tenant)", removed);
    }
}

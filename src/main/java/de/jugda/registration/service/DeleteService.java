package de.jugda.registration.service;

import de.jugda.registration.domain.Registration;
import de.jugda.registration.model.DeregistrationForm;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.UUID;

@ApplicationScoped
public class DeleteService {

    @Inject
    EmailService emailService;

    @Transactional
    public String deleteFromUi(DeregistrationForm form) {
        Registration registration = Registration
            .find("eventId = ?1 and email = ?2", form.getEventId(), form.getEmail().toLowerCase())
            .firstResult();
        return deleteFromUri(registration.getId());
    }

    @Transactional
    public String deleteFromUri(UUID id) {
        Registration registration = Registration.findById(id);
        if (!registration.isWaitlist()) {
            processWaitlist(registration.getEventId());
        }
        String name = registration.getName();
        Registration.deleteById(id);
        return name;
    }

    @Transactional
    public void delete(UUID id) {
        Registration.deleteById(id);
    }

    private void processWaitlist(String eventId) {
        Registration.find("eventId = ?1 and waitlist = true", eventId)
            .<Registration>stream()
            .forEach(waiter -> {
                waiter.setWaitlist(false);
                waiter.persist();
                emailService.sendWaitlistToAttendeeConfirmation(waiter.toDto());
            });
    }

}

package de.jugda.registration.service;

import de.jugda.registration.domain.Registration;
import de.jugda.registration.model.RegistrationForm;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class RegistrationService {

    @Inject
    EmailService emailService;

    public long getRegistrationCount(String eventId) {
        return Registration.count("eventId", eventId);
    }

    @Transactional
    public RegistrationForm handleRegistration(RegistrationForm form) {
        Registration registration = Registration.find("eventId = ?1 and email = ?2", form.getEventId(), form.getEmail()).firstResult();
        if (registration != null) {
            registration.updateFrom(form);
        } else {
            registration = Registration.of(form);
        }
        registration.persist();

        form.setId(registration.getId().toString());
        emailService.sendRegistrationConfirmation(registration.toDto());

        return form;
    }

}

package de.jugda.registration.service;

import de.jugda.registration.domain.Registration;
import de.jugda.registration.model.RegistrationDto;
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
    public RegistrationDto handleRegistration(RegistrationForm form, int limit) {
        Registration registration = Registration.find("eventId = ?1 and email = ?2", form.getEventId(), form.getEmail()).firstResult();
        if (registration != null) {
            registration.updateFrom(form);
        } else {
            boolean waitlist = getRegistrationCount(form.getEventId()) >= limit;
            registration = Registration.of(form, waitlist);
        }
        registration.persist();

        RegistrationDto dto = registration.toDto();
        emailService.sendRegistrationConfirmation(dto);
        return dto;
    }

}

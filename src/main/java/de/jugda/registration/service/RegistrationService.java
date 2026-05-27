package de.jugda.registration.service;

import de.jugda.registration.dao.RegistrationDao;
import de.jugda.registration.domain.Registration;
import de.jugda.registration.model.RegistrationForm;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Niko Köbler, http://www.n-k.de, @dasniko
 */
@ApplicationScoped
public class RegistrationService {

    @Inject
    RegistrationDao registrationDao;
    @Inject
    EmailService emailService;

    public int getRegistrationCount(String eventId) {
        return registrationDao.getCount(eventId);
    }

    public RegistrationForm handleRegistration(RegistrationForm model) {
        Registration registration = Registration.of(model);

        Registration existingRegistration = registrationDao.findByEventIdAndEmail(registration.getEventId(), registration.getEmail() );
        if (null != existingRegistration) {
            registration.setId(existingRegistration.getId());
        }

        registrationDao.save(registration);

        // registration may be null when retrieved, due to eventual consistency
        // try to get it some time later...
        Registration savedRegistration;
        AtomicInteger counter = new AtomicInteger();
        do {
            savedRegistration = registrationDao.findByEventIdAndEmail(registration.getEventId(), registration.getEmail() );
            if (savedRegistration == null) {
                try {
                    //noinspection BusyWait
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                counter.incrementAndGet();
            }
        } while (savedRegistration == null && counter.intValue() < 3);

        if (savedRegistration != null) {
            model.setId(savedRegistration.getId().toString());
            emailService.sendRegistrationConfirmation(savedRegistration.toDto());
        }

        return model;
    }

}

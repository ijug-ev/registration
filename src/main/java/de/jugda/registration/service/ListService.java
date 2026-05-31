package de.jugda.registration.service;

import de.jugda.registration.domain.Registration;
import de.jugda.registration.model.RegistrationDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ListService {

    @Inject
    EntityManager em;

    public List<RegistrationDto> singleEventRegistrations(String eventId) {
        return Registration.<Registration>stream("eventId", eventId).map(Registration::toDto).toList();
    }

    public Map<String, Integer> allEvents() {
        Map<String, Integer> events = new LinkedHashMap<>();
        em.createQuery(
                "SELECT r.eventId, COUNT(r) FROM Registration r GROUP BY r.eventId ORDER BY r.eventId",
                Object[].class)
            .getResultList()
            .forEach(row -> events.put((String) row[0], ((Long) row[1]).intValue()));
        return events;
    }

}

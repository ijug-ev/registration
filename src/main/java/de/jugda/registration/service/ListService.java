package de.jugda.registration.service;

import de.jugda.registration.domain.Registration;
import de.jugda.registration.model.RegistrationDto;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ListService {

    public List<RegistrationDto> singleEventRegistrations(String eventId) {
        return Registration.<Registration>stream("eventId", eventId).map(Registration::toDto).toList();
    }

    public Map<String, Integer> allEvents() {
        List<RegistrationDto> registrations = Registration.<Registration>streamAll().map(Registration::toDto).toList();

        Map<String, Integer> events = new LinkedHashMap<>();
        registrations.forEach(reg -> events.put(reg.getEventId(), events.getOrDefault(reg.getEventId(), 0) + 1));

        return events;
    }

}

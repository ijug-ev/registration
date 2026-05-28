package de.jugda.registration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.jugda.registration.TenantContext;
import de.jugda.registration.domain.EventData;
import de.jugda.registration.model.EventDto;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@ApplicationScoped
public class EventService {

    @Inject
    ObjectMapper objectMapper;
    @Inject
    TenantContext tenantCtx;
    @Inject
    @CacheName("events")
    Cache cache;

    public List<EventDto> getAllEvents() {
        String key = tenantCtx.getTenant().getEvents();
        return cache.get(key, k -> {
            try (InputStream in = URI.create(tenantCtx.getTenant().getEvents()).toURL().openStream()) {
                return objectMapper.readValue(in, new TypeReference<List<EventDto>>() {
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).await().indefinitely();
    }

    public EventDto getEvent(String eventId) {
        return getAllEvents().stream()
            .filter(event -> event.uid.startsWith(eventId.replace("-", "")))
            .findFirst()
            .orElse(null);
    }

    public Map<String, String> getEventData(String eventId) {
        return EventData.asMap(eventId);
    }

    @Transactional
    public void putEventData(String eventId, Map<String, String> data) {
        Map<String, EventData> existing = EventData.<EventData>stream("eventId = ?1 and key in ?2", eventId, data.keySet())
            .collect(toMap(e -> e.key, e -> e));

        for (Map.Entry<String, String> entry : data.entrySet()) {
            EventData ed = existing.get(entry.getKey());
            if (ed != null) {
                ed.value = entry.getValue();
            } else {
                EventData newEd = new EventData();
                newEd.eventId = eventId;
                newEd.key = entry.getKey();
                newEd.value = entry.getValue();
                newEd.persist();
            }
        }
    }
}

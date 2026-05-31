package de.jugda.registration.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.TenantId;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toMap;

@Entity
@Table(name = "event_data")
@IdClass(EventData.EventDataId.class)
public class EventData extends PanacheEntityBase {
    @Id
    @TenantId
    @Column(nullable = false)
    public String tenant;
    @Id
    @Column(name = "event_id", nullable = false)
    public String eventId;
    @Id
    @Column(name = "`key`", nullable = false)
    public String key;
    @Column(name = "`value`", nullable = false, columnDefinition = "text")
    public String value;

    public static Map<String, String> asMap(String eventId) {
        return EventData.<EventData>stream("eventId", eventId)
            .collect(toMap(c -> c.key, c -> c.value));
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventDataId implements Serializable {
        public String tenant;
        public String eventId;
        public String key;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EventDataId that)) return false;
            return Objects.equals(tenant, that.tenant) && Objects.equals(eventId, that.eventId) && Objects.equals(key, that.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tenant, eventId, key);
        }
    }
}

package de.jugda.registration.model;

import io.quarkus.qute.TemplateData;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * @author Niko Köbler, http://www.n-k.de, @dasniko
 */
@Data
@NoArgsConstructor
@RegisterForReflection
@TemplateData
public class RegistrationDto {
    public String id;
    public String eventId;
    public String name;
    public String email;
    public boolean pub;
    public boolean waitlist;
    public boolean privacy;
    public boolean videoRecording;
    public boolean remote;
    public LocalDateTime created;
    public LocalDateTime confirmationSentAt;
    public Long ttl;

    /**
     * The event date for mails that have no event to name: the eventId is a date by convention
     * ({@code 2026-10-21}), but it is a free-form path parameter, so a value that does not parse is
     * handed back unchanged rather than breaking the mail that is trying to apologise for a missing event.
     */
    public String formattedEventDate() {
        try {
            return LocalDate.parse(eventId).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        } catch (DateTimeParseException e) {
            return eventId;
        }
    }

    // needed by template
    public String formattedCreationDate() {
        return created.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    // needed by template
    public String formattedConfirmationSentAt() {
        return confirmationSentAt == null
            ? "" : confirmationSentAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

}

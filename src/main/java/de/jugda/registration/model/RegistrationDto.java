package de.jugda.registration.model;

import io.quarkus.qute.TemplateData;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    public Long ttl;

    // needed by template
    public String formattedCreationDate() {
        return created.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

}

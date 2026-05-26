package de.jugda.registration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.smallrye.config.ConfigMapping;

/**
 * @author Niko Köbler, https://www.n-k.de, @dasniko
 */
@ConfigMapping(prefix = "app")
public interface Config {

    TenantConfig tenant();

    EmailConfig email();
    EventsConfig events();

    PageConfig page();

    interface EmailConfig {
        String from();
        String subjectPrefix();
    }

    interface EventsConfig {
        String jsonUrl();
        String dataBucket();
        String dataKey();
    }

    interface TenantConfig {
        @JsonProperty("id")
        String id();
        @JsonProperty("name")
        String name();
        @JsonProperty("baseUrl")
        String baseUrl();
        @JsonProperty("privacy")
        String privacy();
        @JsonProperty("imprint")
        String imprint();
        @JsonProperty("logo")
        String logo();
        @JsonProperty("website")
        String website();
        @JsonProperty("youtube")
        String youtube();
    }

    interface PageConfig {
        RegistrationPageConfig registration();
        WebinarPageConfig webinar();
    }

    interface RegistrationPageConfig {
        String name();
        String email();
        String video();
        String disclaimer();
        String waitlist();
    }

    interface WebinarPageConfig {
        String tools();
        String communication();
        String recording();
        String recordingPrivacyHint();
    }
}

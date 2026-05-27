package de.jugda.registration;

import io.smallrye.config.ConfigMapping;

/**
 * @author Niko Köbler, https://www.n-k.de, @dasniko
 */
@ConfigMapping(prefix = "app")
public interface Config {

    PageConfig page();

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

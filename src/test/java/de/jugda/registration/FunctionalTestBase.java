package de.jugda.registration;

import lombok.Value;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public abstract class FunctionalTestBase {

    static final String TENANT = "test";
    static final LocalDate TEST_EVENT_DATE = LocalDate.now().plusMonths(1);
    static final String EVENT_ID = TEST_EVENT_DATE.format(DateTimeFormatter.ISO_LOCAL_DATE);

    static final List<Participant> PARTICIPANTS = List.of(
        new Participant("John Doe", "john.doe@example.com"),
        new Participant("Jane Doe", "jane.doe@example.com"),
        new Participant("Jack Doe", "jack.doe@example.com")
    );

    @Value
    static class Participant {
        String name;
        String email;
    }
}

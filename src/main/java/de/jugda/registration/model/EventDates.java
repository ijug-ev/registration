package de.jugda.registration.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * The event id is a date by convention ({@code 2026-10-21}) -- that is what the JUG's calendar feed is
 * keyed on and what every URL carries. Whenever an event has dropped out of the feed, the id is all
 * that is left to name the event by, on the admin page as well as in the confirmation mail. Formatting
 * it lives here once so those places cannot drift apart on the format.
 */
public final class EventDates {

    private static final DateTimeFormatter GERMAN_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private EventDates() {
    }

    /**
     * @return the id as a German date, or unchanged if it is not one -- the id is a free-form path
     * parameter, and a mail apologising for a missing event must not fail over its format
     */
    public static String display(String eventId) {
        try {
            return LocalDate.parse(eventId).format(GERMAN_DATE);
        } catch (DateTimeParseException e) {
            return eventId;
        }
    }
}

package de.jugda.registration.model;

import io.quarkus.qute.TemplateData;

@TemplateData
public record RegistrationConfig(
    int limit,
    long freeSeats,
    long actualCount,
    boolean showPub,
    boolean hideVideoRecording,
    boolean hybrid,
    boolean waitlist
) {}

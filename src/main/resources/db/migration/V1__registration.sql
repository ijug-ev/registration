CREATE TABLE registration
(
    id             UUID DEFAULT gen_random_uuid() NOT NULL,
    tenant         VARCHAR(255) NOT NULL,
    eventId        VARCHAR(255),
    name           VARCHAR(255),
    email          VARCHAR(255),
    pub            BOOLEAN      NOT NULL,
    waitlist       BOOLEAN      NOT NULL,
    privacy        BOOLEAN      NOT NULL,
    videoRecording BOOLEAN      NOT NULL,
    remote         BOOLEAN      NOT NULL,
    created        TIMESTAMP WITHOUT TIME ZONE,
    ttl            BIGINT,
    CONSTRAINT pk_registration PRIMARY KEY (id)
);

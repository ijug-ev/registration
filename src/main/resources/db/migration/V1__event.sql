CREATE TABLE event
(
    uid         VARCHAR(255) NOT NULL,
    tenant      VARCHAR(255) NOT NULL,
    eventId     VARCHAR(255),
    summary     VARCHAR(255),
    title       VARCHAR(255),
    description TEXT,
    speaker     VARCHAR(255),
    twitter     VARCHAR(255),
    location    VARCHAR(255),
    url         VARCHAR(255),
    startDate   TIMESTAMP WITHOUT TIME ZONE,
    endDate     TIMESTAMP WITHOUT TIME ZONE,
    timezone    VARCHAR(255),
    CONSTRAINT pk_event PRIMARY KEY (uid)
);

INSERT INTO event (eventId, tenant, uid, summary, title, description, speaker, twitter, location, url, startDate, endDate, timezone)
VALUES ('2026-12-31', 'jugda', '20261231@jug-da.de', 'Testtalk (John Doe)', 'Testtalk', 'null', 'John Doe', 'null', 'Online', 'https://www.jug-da.de/2026/12/testtalk/', '2026-12-31T12:40:41', '2026-12-31T14:40:41', 'Europe/Berlin');

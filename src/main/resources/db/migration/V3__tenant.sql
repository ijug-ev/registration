CREATE TABLE tenant
(
    id          VARCHAR(255) NOT NULL,
    name        VARCHAR(255),
    website     VARCHAR(255),
    privacy     VARCHAR(255),
    imprint     VARCHAR(255),
    logo        VARCHAR(255),
    reply_to    VARCHAR(255),
    events      VARCHAR(255),
    CONSTRAINT pk_tenant PRIMARY KEY (id)
);

INSERT INTO tenant (id, name, website, privacy, imprint, logo, reply_to, events)
VALUES ('jugda', 'JUG Darmstadt', 'https://www.jug-da.de', 'https://www.jug-da.de/datenschutz/', 'https://www.jug-da.de/impressum/', 'https://www.jug-da.de/images/jugda_logo_rund.png', 'JUG Darmstadt <info@jug-da.de>', 'https://www.jug-da.de/events.json');
INSERT INTO tenant (id, name, website, privacy, imprint, logo, reply_to, events)
VALUES ('cyberland', 'Cyberland', 'https://cyberland.ijug.eu', 'https://www.ijug.eu/de/datenschutz/', 'https://www.ijug.eu/de/impressum/', 'https://cyberland.ijug.eu/assets/logo/logo-header.png', 'Cyberland <cyberland@ijug.eu>', 'https://cyberland.ijug.eu/events.json');

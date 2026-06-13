CREATE TABLE invite_config
(
    tenant_id   VARCHAR(255) NOT NULL,
    mail_to     VARCHAR(255),
    masto_url   VARCHAR(255),
    masto_token VARCHAR(255),
    CONSTRAINT pk_invite_config PRIMARY KEY (tenant_id)
);

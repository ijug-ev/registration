create table event_data
(
    tenant   varchar(255) not null,
    event_id varchar(255) not null,
    key      varchar(255) not null,
    value    text         not null,
    constraint event_data_pk
        primary key (tenant, event_id, key)
);

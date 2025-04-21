create table if not exists extensions
(
    name    varchar(255) not null,
    data    blob,
    version bigint,
    primary key (name)
);

-- Outbox events table for distributed messaging
create table if not exists outbox_events
(
    id            varchar(36) primary key,
    type          varchar(50) not null,
    entity_id     varchar(255) not null,
    entity_type   varchar(50) not null,
    operation     varchar(50) not null,
    metadata_json text,
    created_at    timestamp not null,
    processed     boolean default false,
    processed_at  timestamp
);

-- Indexes for efficient queries
create index if not exists idx_outbox_events_processed on outbox_events(processed, created_at);
create index if not exists idx_outbox_events_entity on outbox_events(entity_type, entity_id);

-- Schema migration for implementing the Outbox pattern
-- This creates the outbox_events table for reliable message delivery

-- Outbox events table
CREATE TABLE IF NOT EXISTS outbox_events (
    id VARCHAR(36) PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(255) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    operation VARCHAR(50) NOT NULL,
    metadata_json TEXT,
    created_at TIMESTAMP NOT NULL,
    processed BOOLEAN DEFAULT FALSE,
    processed_at TIMESTAMP
);

-- Indexes for efficient queries
CREATE INDEX IF NOT EXISTS idx_outbox_events_processed ON outbox_events(processed, created_at);
CREATE INDEX IF NOT EXISTS idx_outbox_events_entity ON outbox_events(entity_type, entity_id);

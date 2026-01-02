-- V1__init_schema.sql
-- Initial database schema for payment processing system

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Payments table with partitioning support
CREATE TABLE payments (
    payment_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4()::text,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) NOT NULL,
    merchant_id VARCHAR(100) NOT NULL,
    customer_id VARCHAR(100) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    metadata JSONB,
    failure_reason VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

-- Indexes for performance
CREATE INDEX idx_idempotency_key ON payments(idempotency_key);
CREATE INDEX idx_merchant_id ON payments(merchant_id);
CREATE INDEX idx_customer_id ON payments(customer_id);
CREATE INDEX idx_status ON payments(status);
CREATE INDEX idx_created_at ON payments(created_at DESC);
CREATE INDEX idx_merchant_created ON payments(merchant_id, created_at DESC);

-- Composite index for common queries
CREATE INDEX idx_merchant_status_created ON payments(merchant_id, status, created_at DESC);

-- JSONB GIN index for metadata queries
CREATE INDEX idx_metadata_gin ON payments USING GIN (metadata);

-- Payment events table for event sourcing
CREATE TABLE payment_events (
    event_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4()::text,
    payment_id VARCHAR(36) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    previous_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    event_data JSONB,
    event_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for payment events
CREATE INDEX idx_payment_event_payment_id ON payment_events(payment_id);
CREATE INDEX idx_payment_event_timestamp ON payment_events(event_timestamp DESC);

-- Foreign key constraint
ALTER TABLE payment_events 
    ADD CONSTRAINT fk_payment_events_payment 
    FOREIGN KEY (payment_id) 
    REFERENCES payments(payment_id) 
    ON DELETE CASCADE;

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger to automatically update updated_at
CREATE TRIGGER update_payment_updated_at 
    BEFORE UPDATE ON payments
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Comments for documentation
COMMENT ON TABLE payments IS 'Main payments table storing transaction data';
COMMENT ON COLUMN payments.idempotency_key IS 'Unique key to prevent duplicate processing';
COMMENT ON COLUMN payments.version IS 'Optimistic locking version number';
COMMENT ON TABLE payment_events IS 'Event sourcing table for payment state changes';

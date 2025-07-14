CREATE TABLE processed_payments (
    id SERIAL PRIMARY KEY,
    correlation_id UUID NOT NULL UNIQUE,
    amount DECIMAL(10, 2) NOT NULL,
    processed_at TIMESTAMP NOT NULL,
    processor_used VARCHAR(10) NOT NULL
);
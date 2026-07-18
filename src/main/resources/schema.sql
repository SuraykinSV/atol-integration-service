CREATE TABLE receipts (
    id VARCHAR(255) PRIMARY KEY,
    receipt_data JSONB NOT NULL,
    status VARCHAR(20) NOT NULL,
    atol_uuid VARCHAR(255),
    fiscal_data JSONB
);


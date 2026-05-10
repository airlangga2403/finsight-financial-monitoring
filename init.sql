-- =============================================================
-- FinSight — Financial Transaction Intelligence Platform
-- =============================================================

CREATE SCHEMA IF NOT EXISTS transaction_svc;
CREATE SCHEMA IF NOT EXISTS audit_svc;

-- =============================================================
-- TRANSACTION SERVICE SCHEMA
-- =============================================================

CREATE TABLE transaction_svc.accounts (
    id               VARCHAR(36)   PRIMARY KEY DEFAULT gen_random_uuid()::TEXT,
    account_number   VARCHAR(20)   NOT NULL UNIQUE,
    holder_name      VARCHAR(100)  NOT NULL,
    account_type     VARCHAR(20)   NOT NULL CHECK (account_type IN ('SAVINGS','CURRENT','INVESTMENT')),
    balance          NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    currency         VARCHAR(5)    NOT NULL DEFAULT 'IDR',
    status           VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','SUSPENDED','CLOSED')),
    created_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE transaction_svc.transactions (
    id                      VARCHAR(36)   PRIMARY KEY DEFAULT gen_random_uuid()::TEXT,
    reference_number        VARCHAR(50)   NOT NULL UNIQUE,
    source_account_id       VARCHAR(36)   REFERENCES transaction_svc.accounts(id),
    destination_account_id  VARCHAR(36)   REFERENCES transaction_svc.accounts(id),
    amount                  NUMERIC(19,2) NOT NULL,
    transaction_type        VARCHAR(20)   NOT NULL CHECK (transaction_type IN ('CREDIT','DEBIT','TRANSFER')),
    status                  VARCHAR(20)   NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','COMPLETED','FAILED','FLAGGED')),
    description             VARCHAR(255),
    is_suspicious           BOOLEAN       NOT NULL DEFAULT FALSE,
    suspicious_reason       VARCHAR(500),
    transaction_date        TIMESTAMP     NOT NULL DEFAULT NOW(),
    created_at              TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_transactions_source_account   ON transaction_svc.transactions(source_account_id);
CREATE INDEX idx_transactions_dest_account     ON transaction_svc.transactions(destination_account_id);
CREATE INDEX idx_transactions_date             ON transaction_svc.transactions(transaction_date);
CREATE INDEX idx_transactions_status           ON transaction_svc.transactions(status);
CREATE INDEX idx_transactions_suspicious       ON transaction_svc.transactions(is_suspicious);
CREATE INDEX idx_transactions_ref_number       ON transaction_svc.transactions(reference_number);

-- =============================================================
-- AUDIT SERVICE SCHEMA
-- =============================================================

CREATE TABLE audit_svc.reconciliation_records (
    id                    VARCHAR(36)   PRIMARY KEY DEFAULT gen_random_uuid()::TEXT,
    reconciliation_date   DATE          NOT NULL UNIQUE,
    total_accounts        INTEGER       NOT NULL DEFAULT 0,
    accounts_with_gap     INTEGER       NOT NULL DEFAULT 0,
    total_credited        NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    total_debited         NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    net_system_flow       NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    status                VARCHAR(20)   NOT NULL CHECK (status IN ('BALANCED','DISCREPANCY_FOUND','PENDING')),
    notes                 TEXT,
    executed_at           TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE audit_svc.reconciliation_line_items (
    id                       VARCHAR(36)   PRIMARY KEY DEFAULT gen_random_uuid()::TEXT,
    reconciliation_record_id VARCHAR(36)   NOT NULL REFERENCES audit_svc.reconciliation_records(id) ON DELETE CASCADE,
    account_id               VARCHAR(36)   NOT NULL,
    account_number           VARCHAR(20)   NOT NULL,
    holder_name              VARCHAR(100)  NOT NULL,
    account_type             VARCHAR(20)   NOT NULL,
    reported_balance         NUMERIC(19,2) NOT NULL,
    calculated_net_flow      NUMERIC(19,2) NOT NULL,
    total_credited           NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    total_debited            NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    has_discrepancy          BOOLEAN       NOT NULL DEFAULT FALSE,
    discrepancy_amount       NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    created_at               TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_recon_line_items_record_id  ON audit_svc.reconciliation_line_items(reconciliation_record_id);
CREATE INDEX idx_recon_line_items_account_id ON audit_svc.reconciliation_line_items(account_id);
CREATE INDEX idx_recon_records_date          ON audit_svc.reconciliation_records(reconciliation_date);

-- =============================================================
-- SEED DATA
-- =============================================================

INSERT INTO transaction_svc.accounts (id, account_number, holder_name, account_type, balance, currency, status, created_at) VALUES
    ('a1000000-0000-0000-0000-000000000001', 'ACC-0001-SVGS', 'Budi Santoso',    'SAVINGS',    250000000.00,  'IDR', 'ACTIVE',    NOW() - INTERVAL '6 months'),
    ('a1000000-0000-0000-0000-000000000002', 'ACC-0002-CURR', 'Siti Rahayu',     'CURRENT',    850000000.00,  'IDR', 'ACTIVE',    NOW() - INTERVAL '8 months'),
    ('a1000000-0000-0000-0000-000000000003', 'ACC-0003-INVS', 'PT Mitra Jaya',   'INVESTMENT', 5200000000.00, 'IDR', 'ACTIVE',    NOW() - INTERVAL '12 months'),
    ('a1000000-0000-0000-0000-000000000004', 'ACC-0004-SVGS', 'Andi Wijaya',     'SAVINGS',    75000000.00,   'IDR', 'ACTIVE',    NOW() - INTERVAL '3 months'),
    ('a1000000-0000-0000-0000-000000000005', 'ACC-0005-CURR', 'CV Usaha Maju',   'CURRENT',    125000000.00,  'IDR', 'ACTIVE',    NOW() - INTERVAL '10 months'),
    ('a1000000-0000-0000-0000-000000000006', 'ACC-0006-SVGS', 'Dewi Kartika',    'SAVINGS',    45000000.00,   'IDR', 'SUSPENDED', NOW() - INTERVAL '4 months'),
    ('a1000000-0000-0000-0000-000000000007', 'ACC-0007-INVS', 'PT Global Dana',  'INVESTMENT', 12500000000.00,'IDR', 'ACTIVE',    NOW() - INTERVAL '18 months'),
    ('a1000000-0000-0000-0000-000000000008', 'ACC-0008-CURR', 'Reza Firmansyah', 'CURRENT',    310000000.00,  'IDR', 'ACTIVE',    NOW() - INTERVAL '5 months');

INSERT INTO transaction_svc.transactions (reference_number, source_account_id, destination_account_id, amount, transaction_type, status, description, is_suspicious, suspicious_reason, transaction_date) VALUES
    ('TXN-20240901-001', 'a1000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000001', 50000000.00,   'TRANSFER', 'COMPLETED', 'Monthly allocation',         FALSE, NULL, NOW() - INTERVAL '60 days'),
    ('TXN-20240901-002', NULL,                                    'a1000000-0000-0000-0000-000000000003', 200000000.00,  'CREDIT',   'COMPLETED', 'Investment top-up',           FALSE, NULL, NOW() - INTERVAL '59 days'),
    ('TXN-20240902-001', 'a1000000-0000-0000-0000-000000000001', NULL,                                   15000000.00,   'DEBIT',    'COMPLETED', 'Supplier payment',            FALSE, NULL, NOW() - INTERVAL '58 days'),
    ('TXN-20240902-002', 'a1000000-0000-0000-0000-000000000004', 'a1000000-0000-0000-0000-000000000005', 25000000.00,   'TRANSFER', 'COMPLETED', 'B2B settlement',              FALSE, NULL, NOW() - INTERVAL '57 days'),
    ('TXN-20240903-001', NULL,                                    'a1000000-0000-0000-0000-000000000007', 500000000.00,  'CREDIT',   'COMPLETED', 'Capital injection',           FALSE, NULL, NOW() - INTERVAL '56 days'),
    ('TXN-20240903-002', 'a1000000-0000-0000-0000-000000000007', 'a1000000-0000-0000-0000-000000000002', 350000000.00,  'TRANSFER', 'COMPLETED', 'Fund distribution',           FALSE, NULL, NOW() - INTERVAL '55 days'),
    ('TXN-20240910-001', 'a1000000-0000-0000-0000-000000000002', NULL,                                   120000000.00,  'DEBIT',    'COMPLETED', 'Payroll disbursement',        FALSE, NULL, NOW() - INTERVAL '48 days'),
    ('TXN-20240910-002', NULL,                                    'a1000000-0000-0000-0000-000000000001', 10000000.00,   'CREDIT',   'COMPLETED', 'Salary credit',               FALSE, NULL, NOW() - INTERVAL '47 days'),
    ('TXN-20240915-001', 'a1000000-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000007', 800000000.00,  'TRANSFER', 'FLAGGED',   'Large inter-investment move', TRUE, 'LARGE_AMOUNT_ANOMALY: exceeds 3x avg; ROUND_AMOUNT_PATTERN', NOW() - INTERVAL '43 days'),
    ('TXN-20240915-002', 'a1000000-0000-0000-0000-000000000004', NULL,                                   5000000.00,    'DEBIT',    'COMPLETED', 'Utility bill',                FALSE, NULL, NOW() - INTERVAL '42 days'),
    ('TXN-20240920-001', 'a1000000-0000-0000-0000-000000000008', 'a1000000-0000-0000-0000-000000000001', 90000000.00,   'TRANSFER', 'COMPLETED', 'Business transfer',           FALSE, NULL, NOW() - INTERVAL '38 days'),
    ('TXN-20240920-002', NULL,                                    'a1000000-0000-0000-0000-000000000008', 45000000.00,   'CREDIT',   'COMPLETED', 'Revenue deposit',             FALSE, NULL, NOW() - INTERVAL '37 days'),
    ('TXN-20241001-001', 'a1000000-0000-0000-0000-000000000007', NULL,                                   250000000.00,  'DEBIT',    'COMPLETED', 'Asset purchase',              FALSE, NULL, NOW() - INTERVAL '30 days'),
    ('TXN-20241001-002', 'a1000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000004', 30000000.00,   'TRANSFER', 'COMPLETED', 'Departmental reallocation',   FALSE, NULL, NOW() - INTERVAL '29 days'),
    ('TXN-20241005-001', 'a1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000005', 18000000.00,   'TRANSFER', 'COMPLETED', 'Vendor payment',              FALSE, NULL, NOW() - INTERVAL '25 days'),
    ('TXN-20241005-002', NULL,                                    'a1000000-0000-0000-0000-000000000003', 1500000000.00, 'CREDIT',   'FLAGGED',   'Unusual large credit',        TRUE, 'LARGE_AMOUNT_ANOMALY: 1.5B with no prior credit history', NOW() - INTERVAL '24 days'),
    ('TXN-20241010-001', 'a1000000-0000-0000-0000-000000000005', 'a1000000-0000-0000-0000-000000000008', 55000000.00,   'TRANSFER', 'COMPLETED', 'Payment settlement',          FALSE, NULL, NOW() - INTERVAL '18 days'),
    ('TXN-20241010-002', 'a1000000-0000-0000-0000-000000000004', NULL,                                   3500000.00,    'DEBIT',    'COMPLETED', 'Insurance premium',           FALSE, NULL, NOW() - INTERVAL '17 days'),
    ('TXN-20241015-001', 'a1000000-0000-0000-0000-000000000008', NULL,                                   75000000.00,   'DEBIT',    'COMPLETED', 'Equipment procurement',       FALSE, NULL, NOW() - INTERVAL '13 days'),
    ('TXN-20241015-002', NULL,                                    'a1000000-0000-0000-0000-000000000002', 200000000.00,  'CREDIT',   'COMPLETED', 'Quarterly revenue',           FALSE, NULL, NOW() - INTERVAL '12 days');

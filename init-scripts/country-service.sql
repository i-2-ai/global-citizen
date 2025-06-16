-- Country Service Database Initialization Script

-- Create tables for Country Service
CREATE TABLE IF NOT EXISTS citizen_passport (
    id BIGSERIAL PRIMARY KEY,
    citizen_id VARCHAR(255) UNIQUE NOT NULL,
    passport_number VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    date_of_birth DATE NOT NULL,
    nationality VARCHAR(255) NOT NULL,
    country_code VARCHAR(10) NOT NULL,
    date_of_issue DATE NOT NULL,
    date_of_expiry DATE NOT NULL,
    authority VARCHAR(255) NOT NULL,
    jwt_token TEXT,
    qr_code_data TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    is_revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS country_keys (
    id BIGSERIAL PRIMARY KEY,
    key_id VARCHAR(255) UNIQUE NOT NULL,
    public_key TEXT NOT NULL,
    private_key_encrypted TEXT NOT NULL,
    algorithm VARCHAR(50) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_citizen_passport_citizen_id ON citizen_passport(citizen_id);
CREATE INDEX IF NOT EXISTS idx_citizen_passport_passport_number ON citizen_passport(passport_number);
CREATE INDEX IF NOT EXISTS idx_citizen_passport_country_code ON citizen_passport(country_code);
CREATE INDEX IF NOT EXISTS idx_citizen_passport_active ON citizen_passport(is_active);
CREATE INDEX IF NOT EXISTS idx_citizen_passport_expiry ON citizen_passport(date_of_expiry);
CREATE INDEX IF NOT EXISTS idx_country_keys_key_id ON country_keys(key_id);
CREATE INDEX IF NOT EXISTS idx_country_keys_active ON country_keys(is_active);

-- Insert sample country keys
INSERT INTO country_keys (key_id, public_key, private_key_encrypted, algorithm) 
VALUES (
    'US-COUNTRY-KEY-2024-001',
    '-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...\n-----END PUBLIC KEY-----',
    '-----BEGIN ENCRYPTED PRIVATE KEY-----\nMIIFDjBABgkqhkiG9w0BBQ0wMzAbBgkqhkiG9w0BBQwwDgQI...\n-----END ENCRYPTED PRIVATE KEY-----',
    'CRYSTALS-Dilithium'
) ON CONFLICT (key_id) DO NOTHING;

-- Insert sample passports
INSERT INTO citizen_passport (
    citizen_id, 
    passport_number, 
    first_name, 
    last_name, 
    date_of_birth, 
    nationality, 
    country_code, 
    date_of_issue, 
    date_of_expiry, 
    authority
) VALUES 
(
    'CIT-001-2024',
    'P123456789',
    'John',
    'Doe',
    '1990-01-01',
    'American',
    'US',
    '2024-01-15',
    '2034-01-15',
    'Department of State'
),
(
    'CIT-002-2024',
    'P987654321',
    'Jane',
    'Smith',
    '1985-05-15',
    'American',
    'US',
    '2024-01-10',
    '2034-01-10',
    'Department of State'
),
(
    'CIT-003-2024',
    'P456789123',
    'Bob',
    'Wilson',
    '1992-12-20',
    'American',
    'US',
    '2024-01-20',
    '2034-01-20',
    'Department of State'
) ON CONFLICT (citizen_id) DO NOTHING; 
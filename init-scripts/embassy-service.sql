-- Embassy Service Database Initialization Script

-- Create tables for Embassy Service
CREATE TABLE IF NOT EXISTS citizen_visa (
    id BIGSERIAL PRIMARY KEY,
    traveler_id VARCHAR(255) UNIQUE NOT NULL,
    visa_number VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    nationality VARCHAR(255) NOT NULL,
    passport_number VARCHAR(255) NOT NULL,
    visa_type VARCHAR(50) NOT NULL,
    purpose_of_visit TEXT,
    date_of_issue DATE NOT NULL,
    date_of_expiry DATE NOT NULL,
    duration_of_stay INTEGER NOT NULL,
    number_of_entries VARCHAR(50) NOT NULL,
    remaining_entries INTEGER NOT NULL,
    issuing_embassy VARCHAR(255) NOT NULL,
    sponsor_name VARCHAR(255),
    sponsor_contact VARCHAR(255),
    remarks TEXT,
    jwt_token TEXT,
    qr_code_data TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    is_revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS embassy_keys (
    id BIGSERIAL PRIMARY KEY,
    key_id VARCHAR(255) UNIQUE NOT NULL,
    embassy_name VARCHAR(255) NOT NULL,
    embassy_location VARCHAR(255) NOT NULL,
    public_key TEXT NOT NULL,
    private_key_encrypted TEXT NOT NULL,
    algorithm VARCHAR(50) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_citizen_visa_traveler_id ON citizen_visa(traveler_id);
CREATE INDEX IF NOT EXISTS idx_citizen_visa_visa_number ON citizen_visa(visa_number);
CREATE INDEX IF NOT EXISTS idx_citizen_visa_passport_number ON citizen_visa(passport_number);
CREATE INDEX IF NOT EXISTS idx_citizen_visa_visa_type ON citizen_visa(visa_type);
CREATE INDEX IF NOT EXISTS idx_citizen_visa_active ON citizen_visa(is_active);
CREATE INDEX IF NOT EXISTS idx_citizen_visa_expiry ON citizen_visa(date_of_expiry);
CREATE INDEX IF NOT EXISTS idx_embassy_keys_key_id ON embassy_keys(key_id);
CREATE INDEX IF NOT EXISTS idx_embassy_keys_active ON embassy_keys(is_active);

-- Insert sample embassy keys
INSERT INTO embassy_keys (key_id, embassy_name, embassy_location, public_key, private_key_encrypted, algorithm) 
VALUES 
(
    'US-EMB-LON-2024-001',
    'US Embassy - London',
    'London, UK',
    '-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...\n-----END PUBLIC KEY-----',
    '-----BEGIN ENCRYPTED PRIVATE KEY-----\nMIIFDjBABgkqhkiG9w0BBQ0wMzAbBgkqhkiG9w0BBQwwDgQI...\n-----END ENCRYPTED PRIVATE KEY-----',
    'CRYSTALS-Dilithium'
),
(
    'US-EMB-PAR-2024-001',
    'US Embassy - Paris',
    'Paris, France',
    '-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...\n-----END PUBLIC KEY-----',
    '-----BEGIN ENCRYPTED PRIVATE KEY-----\nMIIFDjBABgkqhkiG9w0BBQ0wMzAbBgkqhkiG9w0BBQwwDgQI...\n-----END ENCRYPTED PRIVATE KEY-----',
    'CRYSTALS-Dilithium'
) ON CONFLICT (key_id) DO NOTHING;

-- Insert sample visas
INSERT INTO citizen_visa (
    traveler_id,
    visa_number,
    first_name,
    last_name,
    nationality,
    passport_number,
    visa_type,
    purpose_of_visit,
    date_of_issue,
    date_of_expiry,
    duration_of_stay,
    number_of_entries,
    remaining_entries,
    issuing_embassy,
    sponsor_name,
    sponsor_contact
) VALUES 
(
    'TRAV-001-2024',
    'V123456789',
    'John',
    'Smith',
    'British',
    'P987654321',
    'Tourist',
    'Tourism and sightseeing',
    '2024-01-15',
    '2024-02-15',
    30,
    'Single Entry',
    1,
    'US Embassy - London',
    NULL,
    NULL
),
(
    'TRAV-002-2024',
    'V987654321',
    'Maria',
    'Garcia',
    'Spanish',
    'P456789123',
    'Business',
    'Business meetings and conferences',
    '2024-01-10',
    '2024-04-10',
    90,
    'Multiple Entry',
    3,
    'US Embassy - Paris',
    'TechCorp Inc.',
    '+1-555-0123'
),
(
    'TRAV-003-2024',
    'V456789123',
    'David',
    'Brown',
    'Australian',
    'P789123456',
    'Student',
    'University studies',
    '2024-01-20',
    '2025-01-20',
    365,
    'Single Entry',
    1,
    'US Embassy - London',
    'University of California',
    '+1-555-0456'
) ON CONFLICT (traveler_id) DO NOTHING; 
-- Central Authority Database Initialization Script

-- Create tables for Central Authority
CREATE TABLE IF NOT EXISTS mother_key (
    id BIGSERIAL PRIMARY KEY,
    key_id VARCHAR(255) UNIQUE NOT NULL,
    public_key TEXT NOT NULL,
    private_key_encrypted TEXT NOT NULL,
    algorithm VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS global_key (
    id BIGSERIAL PRIMARY KEY,
    key_id VARCHAR(255) UNIQUE NOT NULL,
    public_key TEXT NOT NULL,
    private_key_encrypted TEXT NOT NULL,
    algorithm VARCHAR(50) NOT NULL,
    mother_key_signature TEXT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS country_certificate (
    id BIGSERIAL PRIMARY KEY,
    country_code VARCHAR(10) NOT NULL,
    country_name VARCHAR(255) NOT NULL,
    public_key TEXT NOT NULL,
    global_key_signature TEXT NOT NULL,
    certificate_data TEXT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_mother_key_key_id ON mother_key(key_id);
CREATE INDEX IF NOT EXISTS idx_global_key_key_id ON global_key(key_id);
CREATE INDEX IF NOT EXISTS idx_global_key_active ON global_key(is_active);
CREATE INDEX IF NOT EXISTS idx_country_certificate_country_code ON country_certificate(country_code);
CREATE INDEX IF NOT EXISTS idx_country_certificate_active ON country_certificate(is_active);
CREATE INDEX IF NOT EXISTS idx_country_certificate_expires ON country_certificate(expires_at);

-- Insert initial mother key (this would be generated securely in production)
INSERT INTO mother_key (key_id, public_key, private_key_encrypted, algorithm) 
VALUES (
    'MOTHER-KEY-2024-001',
    '-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...\n-----END PUBLIC KEY-----',
    '-----BEGIN ENCRYPTED PRIVATE KEY-----\nMIIFDjBABgkqhkiG9w0BBQ0wMzAbBgkqhkiG9w0BBQwwDgQI...\n-----END ENCRYPTED PRIVATE KEY-----',
    'CRYSTALS-Dilithium'
) ON CONFLICT (key_id) DO NOTHING;

-- Insert sample global keys
INSERT INTO global_key (key_id, public_key, private_key_encrypted, algorithm, mother_key_signature) 
VALUES 
(
    'GLOBAL-KEY-2024-001',
    '-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...\n-----END PUBLIC KEY-----',
    '-----BEGIN ENCRYPTED PRIVATE KEY-----\nMIIFDjBABgkqhkiG9w0BBQ0wMzAbBgkqhkiG9w0BBQwwDgQI...\n-----END ENCRYPTED PRIVATE KEY-----',
    'CRYSTALS-Dilithium',
    '-----BEGIN SIGNATURE-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...\n-----END SIGNATURE-----'
),
(
    'GLOBAL-KEY-2024-002',
    '-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...\n-----END PUBLIC KEY-----',
    '-----BEGIN ENCRYPTED PRIVATE KEY-----\nMIIFDjBABgkqhkiG9w0BBQ0wMzAbBgkqhkiG9w0BBQwwDgQI...\n-----END ENCRYPTED PRIVATE KEY-----',
    'CRYSTALS-Dilithium',
    '-----BEGIN SIGNATURE-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...\n-----END SIGNATURE-----'
) ON CONFLICT (key_id) DO NOTHING;

-- Insert sample country certificates
INSERT INTO country_certificate (country_code, country_name, public_key, global_key_signature, certificate_data, expires_at) 
VALUES 
(
    'US',
    'United States',
    '-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...\n-----END PUBLIC KEY-----',
    '-----BEGIN SIGNATURE-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...\n-----END SIGNATURE-----',
    '{"countryCode":"US","countryName":"United States","authority":"Department of State","validFrom":"2024-01-01","validTo":"2024-12-31"}',
    '2024-12-31 23:59:59'
),
(
    'UK',
    'United Kingdom',
    '-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...\n-----END PUBLIC KEY-----',
    '-----BEGIN SIGNATURE-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...\n-----END SIGNATURE-----',
    '{"countryCode":"UK","countryName":"United Kingdom","authority":"Home Office","validFrom":"2024-01-01","validTo":"2024-12-31"}',
    '2024-12-31 23:59:59'
),
(
    'CA',
    'Canada',
    '-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...\n-----END PUBLIC KEY-----',
    '-----BEGIN SIGNATURE-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...\n-----END SIGNATURE-----',
    '{"countryCode":"CA","countryName":"Canada","authority":"Immigration, Refugees and Citizenship Canada","validFrom":"2024-01-01","validTo":"2024-12-31"}',
    '2024-12-31 23:59:59'
) ON CONFLICT (country_code) DO NOTHING; 
CREATE TABLE registration_consents (
    user_id UUID PRIMARY KEY REFERENCES users(id),
    terms_version VARCHAR(30) NOT NULL,
    privacy_version VARCHAR(30) NOT NULL,
    consent_version VARCHAR(30) NOT NULL,
    accepted_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

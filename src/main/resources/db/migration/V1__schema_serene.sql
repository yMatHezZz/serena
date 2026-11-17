-- Banco novo PostgreSQL 16+. Não aplicar automaticamente em banco preexistente do Serene.
CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE roles (
    id UUID PRIMARY KEY, name VARCHAR(50) NOT NULL UNIQUE, description VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE users (
    id UUID PRIMARY KEY, name VARCHAR(100) NOT NULL, email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL, status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE','INACTIVE')),
    created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL
);
CREATE UNIQUE INDEX users_email_lower_key ON users (lower(email));
CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id), role_id UUID NOT NULL REFERENCES roles(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(), PRIMARY KEY(user_id, role_id)
);
CREATE TABLE patient_profiles (
    id UUID PRIMARY KEY, user_id UUID NOT NULL UNIQUE REFERENCES users(id), date_of_birth DATE NOT NULL,
    gender VARCHAR(20), phone VARCHAR(20), address TEXT,
    created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL
);
CREATE TABLE professional_profiles (
    id UUID PRIMARY KEY, user_id UUID NOT NULL UNIQUE REFERENCES users(id), license_number VARCHAR(50) NOT NULL UNIQUE,
    specialty VARCHAR(100) NOT NULL, bio TEXT, phone VARCHAR(20),
    created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL
);
CREATE TABLE availability_slots (
    id UUID PRIMARY KEY, professional_id UUID NOT NULL REFERENCES professional_profiles(id),
    start_at TIMESTAMPTZ NOT NULL, end_at TIMESTAMPTZ NOT NULL, is_available BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL,
    CHECK (end_at > start_at),
    EXCLUDE USING gist (professional_id WITH =, tstzrange(start_at,end_at,'[)') WITH &&) WHERE (is_available)
);
CREATE TABLE consultations (
    id UUID PRIMARY KEY, patient_id UUID NOT NULL REFERENCES patient_profiles(id),
    professional_id UUID NOT NULL REFERENCES professional_profiles(id),
    scheduled_start_at TIMESTAMPTZ NOT NULL, scheduled_end_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('SCHEDULED','CANCELLED','COMPLETED')), notes TEXT,
    created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL,
    CHECK (scheduled_end_at > scheduled_start_at),
    EXCLUDE USING gist (professional_id WITH =, tstzrange(scheduled_start_at,scheduled_end_at,'[)') WITH &&) WHERE (status <> 'CANCELLED'),
    EXCLUDE USING gist (patient_id WITH =, tstzrange(scheduled_start_at,scheduled_end_at,'[)') WITH &&) WHERE (status <> 'CANCELLED')
);
CREATE TABLE consultation_participants (
    id UUID PRIMARY KEY, consultation_id UUID NOT NULL REFERENCES consultations(id),
    user_id UUID NOT NULL REFERENCES users(id), role VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(), UNIQUE(consultation_id,user_id)
);
CREATE TABLE video_sessions (
    id UUID PRIMARY KEY, consultation_id UUID NOT NULL UNIQUE REFERENCES consultations(id),
    provider VARCHAR(20) NOT NULL, room_name VARCHAR(100) NOT NULL, status VARCHAR(20) NOT NULL,
    started_at TIMESTAMPTZ, ended_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE session_events (
    id UUID PRIMARY KEY, video_session_id UUID NOT NULL REFERENCES video_sessions(id),
    event_type VARCHAR(50) NOT NULL, event_data JSONB NOT NULL DEFAULT '{}', created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE activities (
    id UUID PRIMARY KEY, name VARCHAR(100) NOT NULL, description TEXT, activity_type VARCHAR(50) NOT NULL,
    configuration JSONB NOT NULL DEFAULT '{}', is_active BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL
);
CREATE TABLE activity_sessions (
    id UUID PRIMARY KEY, activity_id UUID NOT NULL REFERENCES activities(id), consultation_id UUID NOT NULL REFERENCES consultations(id),
    started_by UUID NOT NULL REFERENCES users(id), status VARCHAR(20) NOT NULL, started_at TIMESTAMPTZ, ended_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE activity_responses (
    id UUID PRIMARY KEY, activity_session_id UUID NOT NULL REFERENCES activity_sessions(id),
    user_id UUID NOT NULL REFERENCES users(id), response_data JSONB NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX consultation_participants_user_idx ON consultation_participants(user_id);
CREATE INDEX session_events_session_idx ON session_events(video_session_id);
CREATE INDEX activity_sessions_consultation_idx ON activity_sessions(consultation_id);
CREATE INDEX activity_sessions_activity_idx ON activity_sessions(activity_id);
CREATE INDEX activity_responses_session_idx ON activity_responses(activity_session_id);
INSERT INTO roles (id,name,description) VALUES
('00000000-0000-0000-0000-000000000001','PATIENT','Paciente'),
('00000000-0000-0000-0000-000000000002','PROFESSIONAL','Profissional');

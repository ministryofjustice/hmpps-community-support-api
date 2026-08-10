-- V01: Create person-related tables

CREATE TABLE IF NOT EXISTS person (
    id UUID NOT NULL PRIMARY KEY,
    identifier VARCHAR(50) NOT NULL UNIQUE,
    first_name VARCHAR(200) NOT NULL,
    last_name VARCHAR(200) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Comments for person table columns
COMMENT ON COLUMN person.id IS 'Unique identifier for the person record';
COMMENT ON COLUMN person.identifier IS 'External identifier for the person (CRN or Prisoner Number)';
COMMENT ON COLUMN person.first_name IS 'First name of the person';
COMMENT ON COLUMN person.last_name IS 'Last name of the person';
COMMENT ON COLUMN person.date_of_birth IS 'Date of birth of the person';
COMMENT ON COLUMN person.gender IS 'Gender of the person';
COMMENT ON COLUMN person.created_at IS 'Timestamp when the record was created';
COMMENT ON COLUMN person.updated_at IS 'Timestamp when the record was last updated';

CREATE TABLE IF NOT EXISTS person_additional_details (
    id UUID NOT NULL PRIMARY KEY,
    person_id UUID NOT NULL UNIQUE,
    ethnicity VARCHAR(100),
    preferred_language VARCHAR(100),
    neurodiverse_conditions TEXT,
    religion_or_belief VARCHAR(100),
    transgender VARCHAR(30),
    sexual_orientation VARCHAR(50),
    address TEXT,
    phone_number VARCHAR(20),
    email_address VARCHAR(255),
    CONSTRAINT fk_person
        FOREIGN KEY(person_id)
            REFERENCES person(id)
            ON DELETE CASCADE
);

-- Comments for person_additional_details table columns
COMMENT ON COLUMN person_additional_details.id IS 'Unique identifier for the additional details record';
COMMENT ON COLUMN person_additional_details.person_id IS 'Foreign key reference to the person table';
COMMENT ON COLUMN person_additional_details.ethnicity IS 'Ethnicity of the person';
COMMENT ON COLUMN person_additional_details.preferred_language IS 'Preferred language of the person';
COMMENT ON COLUMN person_additional_details.neurodiverse_conditions IS 'Any neurodiverse conditions the person may have';
COMMENT ON COLUMN person_additional_details.religion_or_belief IS 'Religion or belief of the person';
COMMENT ON COLUMN person_additional_details.transgender IS 'Transgender status of the person';
COMMENT ON COLUMN person_additional_details.sexual_orientation IS 'Sexual orientation of the person';
COMMENT ON COLUMN person_additional_details.address IS 'Address of the person';
COMMENT ON COLUMN person_additional_details.phone_number IS 'Phone number of the person';
COMMENT ON COLUMN person_additional_details.email_address IS 'Email address of the person';


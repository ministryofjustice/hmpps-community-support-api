CREATE TABLE IF NOT EXISTS person (
    id UUID NOT NULL PRIMARY KEY,
    identifier VARCHAR(50) NOT NULL,
    first_name VARCHAR(200) NOT NULL,
    last_name VARCHAR(200) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

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

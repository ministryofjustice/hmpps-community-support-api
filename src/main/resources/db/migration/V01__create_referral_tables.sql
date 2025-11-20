CREATE TABLE IF NOT EXISTS referral (
    id UUID NOT NULL PRIMARY KEY,
    first_name TEXT,
    last_name TEXT,
    crn TEXT NOT NULL,
    reference_number TEXT NOT NULL,
    sex TEXT,
    date_of_birth DATE,
    ethnicity TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
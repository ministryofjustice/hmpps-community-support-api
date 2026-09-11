-- V044: Create withdrawal reason reference data table

CREATE TABLE IF NOT EXISTS withdrawal_reason (
    id UUID NOT NULL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    group_name VARCHAR(200) NOT NULL,
    CONSTRAINT uk_withdrawal_reason_name UNIQUE (name)
);

COMMENT ON TABLE withdrawal_reason IS 'Static reference data for the reasons a referral can be withdrawn, grouped for display as radio button groups';
COMMENT ON COLUMN withdrawal_reason.id IS 'Unique identifier for the withdrawal reason';
COMMENT ON COLUMN withdrawal_reason.name IS 'Human-readable withdrawal reason text shown to the user';
COMMENT ON COLUMN withdrawal_reason.group_name IS 'Heading under which the withdrawal reason radio button appears';

-- Add foreign key from referral_withdrawal_details.reason_code to withdrawal_reason.name

ALTER TABLE referral_withdrawal_details
    ADD CONSTRAINT fk_referral_withdrawal_details_reason_code
        FOREIGN KEY (reason_code)
            REFERENCES withdrawal_reason(name);

COMMENT ON COLUMN referral_withdrawal_details.reason_code IS 'Reason given for withdrawing the referral; references withdrawal_reason.name';

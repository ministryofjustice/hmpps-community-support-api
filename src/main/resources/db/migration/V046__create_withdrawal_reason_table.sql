-- V046: Create withdrawal reason reference data table

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

-- Replace referral_withdrawal_details.reason_code (free text) with reason_id referencing withdrawal_reason.id

ALTER TABLE referral_withdrawal_details
    DROP COLUMN reason_code;

ALTER TABLE referral_withdrawal_details
    ADD COLUMN reason_id UUID NOT NULL;

ALTER TABLE referral_withdrawal_details
    ADD CONSTRAINT fk_referral_withdrawal_details_reason_id
        FOREIGN KEY (reason_id)
            REFERENCES withdrawal_reason(id);

COMMENT ON COLUMN referral_withdrawal_details.reason_id IS 'Reason given for withdrawing the referral; references withdrawal_reason.id';

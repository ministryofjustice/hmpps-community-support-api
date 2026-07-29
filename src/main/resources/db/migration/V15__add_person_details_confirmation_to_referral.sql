ALTER TABLE referral
    ADD COLUMN person_details_confirmed_at TIMESTAMP;

ALTER TABLE referral
    ADD COLUMN person_details_confirmed_by UUID;

ALTER TABLE referral
    ADD CONSTRAINT fk_referral_person_details_confirmed_by
        FOREIGN KEY (person_details_confirmed_by)
            REFERENCES referral_user(id);
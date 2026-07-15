-- V13: Create risk information table

CREATE TABLE IF NOT EXISTS risk_information (
    id UUID NOT NULL PRIMARY KEY,

    referral_id UUID NOT NULL,

    risk_summary_who_is_at_risk TEXT,
    risk_summary_nature_of_risk TEXT,
    risk_summary_risk_imminence TEXT,

    risk_to_self_suicide TEXT,
    risk_to_self_harm TEXT,
    risk_to_self_hostel_setting TEXT,
    risk_to_self_vulnerability TEXT,

    additional_information TEXT,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID NOT NULL REFERENCES referral_user(id),

    CONSTRAINT fk_risk_information_referral FOREIGN KEY (referral_id) REFERENCES referral(id) ON DELETE CASCADE,

    CONSTRAINT uk_risk_information_referral UNIQUE (referral_id)
);

CREATE INDEX idx_risk_information_referral_id ON risk_information(referral_id);

COMMENT ON TABLE risk_information IS 'Risk information captured for the referee of a referral';
COMMENT ON COLUMN risk_information.risk_summary_who_is_at_risk IS 'Risk summary - who is at risk';
COMMENT ON COLUMN risk_information.risk_summary_nature_of_risk IS 'Risk summary - nature of the risk';
COMMENT ON COLUMN risk_information.risk_summary_risk_imminence IS 'Risk summary - risk imminence';
COMMENT ON COLUMN risk_information.risk_to_self_suicide IS 'Risk to self - suicide';
COMMENT ON COLUMN risk_information.risk_to_self_harm IS 'Risk to self - self harm';
COMMENT ON COLUMN risk_information.risk_to_self_hostel_setting IS 'Risk to self - hostel setting';
COMMENT ON COLUMN risk_information.risk_to_self_vulnerability IS 'Risk to self - vulnerability';
COMMENT ON COLUMN risk_information.additional_information IS 'Free text for any additional risk information';
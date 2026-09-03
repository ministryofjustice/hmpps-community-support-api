CREATE TABLE IF NOT EXISTS referral_withdrawal_details (
	id UUID PRIMARY KEY,
	referral_id UUID NOT NULL,
	reason TEXT NOT NULL,
	reason_details TEXT,
	created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
	created_by UUID NOT NULL,

	CONSTRAINT fk_referral_withdrawal_details_referral
	  FOREIGN KEY (referral_id)
		REFERENCES referral(id)
		ON DELETE CASCADE,

	CONSTRAINT fk_referral_withdrawal_details_created_by
	  FOREIGN KEY (created_by)
		REFERENCES referral_user(id),

	CONSTRAINT uk_referral_withdrawal_details_referral UNIQUE (referral_id)
);

CREATE INDEX idx_referral_withdrawal_details_referral_id ON referral_withdrawal_details(referral_id);

COMMENT ON TABLE referral_withdrawal_details IS 'Withdrawal details captured against a referral';
COMMENT ON COLUMN referral_withdrawal_details.referral_id IS 'Foreign key reference to the referral';
COMMENT ON COLUMN referral_withdrawal_details.reason IS 'Reason for withdrawing the referral';
COMMENT ON COLUMN referral_withdrawal_details.reason_details IS 'Additional detail about the reason for withdrawal';
COMMENT ON COLUMN referral_withdrawal_details.created_at IS 'Timestamp when the withdrawal details were created';
COMMENT ON COLUMN referral_withdrawal_details.created_by IS 'User who created the withdrawal details';

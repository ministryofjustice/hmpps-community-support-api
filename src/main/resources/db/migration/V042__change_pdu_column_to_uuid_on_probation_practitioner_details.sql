-- Change the probation_practitioner_details.pdu column from a free-text PDU name
-- to a UUID foreign key reference to the pdu table.
ALTER TABLE probation_practitioner_details
	ALTER COLUMN pdu TYPE UUID USING NULL,
	ADD CONSTRAINT fk_probation_practitioner_details_pdu
	  FOREIGN KEY (pdu)
		REFERENCES pdu(id);

COMMENT ON COLUMN probation_practitioner_details.pdu IS 'Foreign key reference to the Probation Delivery Unit (pdu) of the Probation Practitioner';

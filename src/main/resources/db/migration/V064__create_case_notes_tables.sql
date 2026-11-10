-- V063: Create case notes tables

CREATE TABLE IF NOT EXISTS reference_data_referral_pathway (
    id UUID NOT NULL PRIMARY KEY,
    title TEXT NOT NULL,
    display_order INTEGER NOT NULL
);

COMMENT ON COLUMN reference_data_referral_pathway.id IS 'Unique identifier for the referral pathway';
COMMENT ON COLUMN reference_data_referral_pathway.title IS 'Title of the pathway';
COMMENT ON COLUMN reference_data_referral_pathway.display_order IS 'The order of the pathways displayed on the page';

CREATE TABLE IF NOT EXISTS reference_data_referral_pathway_need (
    id UUID NOT NULL PRIMARY KEY,
    reference_data_referral_pathway_id UUID NOT NULL,
    reference_data_need_id UUID NOT NULL,
    CONSTRAINT fk_reference_data_pathway_need_need
        FOREIGN KEY (reference_data_need_id) REFERENCES reference_data_need(id) ON DELETE CASCADE,
    CONSTRAINT fk_reference_data_pathway_need_pathway
        FOREIGN KEY (reference_data_referral_pathway_id) REFERENCES reference_data_referral_pathway(id) ON DELETE CASCADE
);

COMMENT ON COLUMN reference_data_referral_pathway_need.id IS 'Unique identifier for the referral pathway need';
COMMENT ON COLUMN reference_data_referral_pathway_need.reference_data_referral_pathway_id IS 'Foreign key reference to reference_data_referral_pathway.id';
COMMENT ON COLUMN reference_data_referral_pathway_need.reference_data_need_id IS 'Foreign key reference to referrence_data_need.is';

CREATE TABLE IF NOT EXISTS referral_notes (
    id UUID NOT NULL PRIMARY KEY,
    referral_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by TEXT NOT NULL DEFAULT 'SYSTEM',
    deleted_at TIMESTAMP,
    title TEXT NOT NULL,
    details TEXT NOT NULL,
    should_notify BOOLEAN NOT NULL,
    CONSTRAINT fk_referral_notes_referral
        FOREIGN KEY (referral_id) REFERENCES referral(id) ON DELETE CASCADE
);

COMMENT ON COLUMN referral_notes.id IS 'Unique identifier for the referral note';
COMMENT ON COLUMN referral_notes.referral_id IS 'Foreign key reference to referral.id';
COMMENT ON COLUMN referral_notes.created_at IS 'Timestamp when the note was created';
COMMENT ON COLUMN referral_notes.created_by IS 'Actor identifier that created the note (UUID or SYSTEM)';
COMMENT ON COLUMN referral_notes.deleted_at IS 'Timestamp when the note was deleted';
COMMENT ON COLUMN referral_notes.title IS 'Title of the note';
COMMENT ON COLUMN referral_notes.details IS 'Details included in the note';
COMMENT ON COLUMN referral_notes.should_notify IS 'Boolean on whether the relevant actor should be notified by email for this notes creation';

CREATE TABLE IF NOT EXISTS referral_notes_reference_data_referral_pathway (
    id UUID NOT NULL PRIMARY KEY,
    reference_data_referral_pathway_id UUID NOT NULL,
    referral_notes_id UUID NOT NULL,
    CONSTRAINT fk_referral_notes_reference_data_referral_pathway_pathway
        FOREIGN KEY (reference_data_referral_pathway_id) REFERENCES reference_data_referral_pathway(id) ON DELETE CASCADE,
    CONSTRAINT fk_referral_notes_reference_data_referral_pathway_notes
        FOREIGN KEY (referral_notes_id) REFERENCES referral_notes(id) ON DELETE CASCADE
);

COMMENT ON COLUMN referral_notes_reference_data_referral_pathway.id IS 'Unique identifier for the referral note reference referral pathway';
COMMENT ON COLUMN referral_notes_reference_data_referral_pathway.reference_data_referral_pathway_id IS 'Foreign key reference to reference_data_referral_pathway.id';
COMMENT ON COLUMN referral_notes_reference_data_referral_pathway.referral_notes_id IS 'Foreign key reference to referral_notes.id';

CREATE TABLE IF NOT EXISTS referral_non_contact_session (
    id UUID NOT NULL PRIMARY KEY,
    referral_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by TEXT NOT NULL DEFAULT 'SYSTEM',
    deleted_at TIMESTAMP,
    start_datetime TIMESTAMP NOT NULL,
    duration_hours INTEGER NOT NULL,
    duration_minutes INTEGER NOT NULL,
    title TEXT NOT NULL,
    details TEXT NOT NULL,
    CONSTRAINT fk_referral_non_contact_session_referral
        FOREIGN KEY (referral_id) REFERENCES referral(id) ON DELETE CASCADE
);

COMMENT ON COLUMN referral_non_contact_session.id IS 'Unique identifier for the referral non contact session';
COMMENT ON COLUMN referral_non_contact_session.referral_id IS 'Foreign key reference to referral.id';
COMMENT ON COLUMN referral_non_contact_session.created_at IS 'Timestamp when the non contact session was created';
COMMENT ON COLUMN referral_non_contact_session.created_by IS 'Actor identifier that created the non contact session (UUID or SYSTEM)';
COMMENT ON COLUMN referral_non_contact_session.deleted_at IS 'Timestamp when the non contact session was deleted';
COMMENT ON COLUMN referral_non_contact_session.start_datetime IS 'Timestamp when the activity of the non contact session took place';
COMMENT ON COLUMN referral_non_contact_session.duration_hours IS 'Number of hours in the non contact session';
COMMENT ON COLUMN referral_non_contact_session.duration_minutes IS 'Number of minutes in the non contact session';
COMMENT ON COLUMN referral_non_contact_session.title IS 'Title of the non contact session';
COMMENT ON COLUMN referral_non_contact_session.details IS 'Details of what happened in the non contact session';

CREATE TABLE IF NOT EXISTS referral_non_contact_session_reference_data_referral_pathway (
    id UUID NOT NULL PRIMARY KEY,
    reference_data_referral_pathway_id UUID NOT NULL,
    referral_non_contact_session_id UUID NOT NULL,
    CONSTRAINT fk_rncs_rdrp_pathway
        FOREIGN KEY (reference_data_referral_pathway_id) REFERENCES reference_data_referral_pathway(id) ON DELETE CASCADE,
    CONSTRAINT fk_rncs_rdrp_non_contact_session
        FOREIGN KEY (referral_non_contact_session_id) REFERENCES referral_non_contact_session(id) ON DELETE CASCADE
);

COMMENT ON COLUMN referral_non_contact_session_reference_data_referral_pathway.id IS 'Unique identifier for the referral non contact session reference referral pathway';
COMMENT ON COLUMN referral_non_contact_session_reference_data_referral_pathway.reference_data_referral_pathway_id IS 'Foreign key reference to reference_data_referral_pathway.id';
COMMENT ON COLUMN referral_non_contact_session_reference_data_referral_pathway.referral_non_contact_session_id IS 'Foreign key reference to referral_non_contact_session.id';

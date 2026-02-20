-- V04: Create case list materialized view

CREATE MATERIALIZED VIEW case_list_view AS
SELECT
    r.id AS referral_id,
    p.last_name || ', ' || p.first_name AS person_name,
    r.person_identifier,
    re.created_at AS date_received,
    MIN(rua.created_at) AS date_assigned,
    rpa.community_service_provider_id,
    csp.service_provider_id,
    COALESCE(
        array_agg(ru.full_name) FILTER (WHERE ru.id IS NOT NULL),
        ARRAY[]::TEXT[]
    ) AS case_workers
FROM referral r
    INNER JOIN person p ON r.person_id = p.id
    INNER JOIN referral_event re ON re.referral_id = r.id AND re.event_type = 'SUBMITTED'
    INNER JOIN referral_provider_assignment rpa ON rpa.referral_id = r.id
    INNER JOIN community_service_provider csp ON csp.id = rpa.community_service_provider_id
    LEFT JOIN referral_user_assignment rua ON rua.referral_id = r.id AND rua.deleted_at IS NULL
    LEFT JOIN referral_user ru ON ru.id = rua.user_id
WHERE r.reference_number IS NOT NULL
GROUP BY
    r.id,
    p.last_name,
    p.first_name,
    r.person_identifier,
    re.created_at,
    rpa.community_service_provider_id,
    csp.service_provider_id;

-- Comments for case_list_view materialized view
COMMENT ON MATERIALIZED VIEW case_list_view IS 'Materialized view for efficient case list queries combining referral, person, and assignment data';
COMMENT ON COLUMN case_list_view.referral_id IS 'Unique identifier for the referral';
COMMENT ON COLUMN case_list_view.person_name IS 'Full name of the person in format: last_name, first_name';
COMMENT ON COLUMN case_list_view.person_identifier IS 'External identifier for the person (CRN or Prisoner Number)';
COMMENT ON COLUMN case_list_view.date_received IS 'Timestamp when the referral was submitted';
COMMENT ON COLUMN case_list_view.date_assigned IS 'Timestamp when the referral was first assigned to a caseworker';
COMMENT ON COLUMN case_list_view.community_service_provider_id IS 'Foreign key reference to the community service provider';
COMMENT ON COLUMN case_list_view.service_provider_id IS 'Foreign key reference to the service provider';
COMMENT ON COLUMN case_list_view.case_workers IS 'Array of full names of caseworkers assigned to the referral';

-- Create indexes for efficient querying
CREATE UNIQUE INDEX idx_case_list_view_referral_id ON case_list_view (referral_id);
CREATE INDEX idx_case_list_view_provider ON case_list_view (community_service_provider_id);
CREATE INDEX idx_case_list_view_service_provider ON case_list_view (service_provider_id);
CREATE INDEX idx_case_list_view_date_assigned ON case_list_view (date_assigned);

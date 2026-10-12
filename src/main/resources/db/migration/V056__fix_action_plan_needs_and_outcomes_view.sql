CREATE OR REPLACE VIEW action_plan_needs_and_outcomes_view AS
SELECT
    ap.referral_id AS referral_id,
    ap.id AS action_plan_id,
    apsqah.id AS action_plan_step_question_answer_header_id,
    apsqad.id AS action_plan_step_question_answer_details_id,
    rdn.id AS need_id,
    rdn.label AS need_name,
    apsqad.content AS outcome_answer_text,
    apsqah.order_number AS display_order
FROM action_plan_step_question_answer_header apsqah
         INNER JOIN action_plan ap ON apsqah.action_plan_id = ap.id
         INNER JOIN (
    SELECT id, d.action_plan_step_question_answer_header_id, revision_number, content, created_at, created_by, free_text_value FROM action_plan_step_question_answer_details d
                                                                                                                                        JOIN (
        SELECT action_plan_step_question_answer_header_id, MAX(revision_number) AS max_revision
        FROM action_plan_step_question_answer_details
        GROUP BY action_plan_step_question_answer_header_id) max_r
                                                                                                                                             ON d.action_plan_step_question_answer_header_id = max_r.action_plan_step_question_answer_header_id AND d.revision_number = max_r.max_revision
) apsqad ON apsqah.id = apsqad.action_plan_step_question_answer_header_id
         INNER JOIN reference_data_action_plan_step_question rdapsq ON apsqah.action_plan_step_question_id = rdapsq.id
         INNER JOIN reference_data_need rdn ON rdapsq.need_id = rdn.id;

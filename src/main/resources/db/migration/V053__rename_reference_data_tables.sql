-- V53: Rename tables that only include static reference data to use the prefix "reference"

ALTER TABLE region
    RENAME TO reference_data_region;

ALTER TABLE contract_area
    RENAME TO reference_data_contract_area;

ALTER TABLE pdu
    RENAME TO reference_data_pdu;

ALTER TABLE service_provider
    RENAME TO reference_data_service_provider;

ALTER TABLE service_category
    RENAME TO reference_data_service_category;

ALTER TABLE community_service_provider
    RENAME TO reference_data_community_service_provider;

ALTER TABLE action_plan_template
    RENAME TO reference_data_action_plan_template;

ALTER TABLE action_plan_step
    RENAME TO reference_data_action_plan_step;

ALTER TABLE action_plan_step_question
    RENAME TO reference_data_action_plan_step_question;

ALTER TABLE action_plan_step_question_choice
    RENAME TO reference_data_action_plan_step_question_choice;

ALTER TABLE need
    RENAME TO reference_data_need;

ALTER TABLE outcome
    RENAME TO reference_data_outcome;

ALTER TABLE withdrawal_reason
    RENAME TO reference_data_withdrawal_reason;





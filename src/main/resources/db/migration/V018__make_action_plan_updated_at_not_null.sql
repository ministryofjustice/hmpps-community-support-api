-- V17 is taken by a seed migration for the action_plan_template table
-- V18: action_plan.updated_at is always populated by the application; enforce this at the database level
-- And create the link between action_plan and action_plan_template

UPDATE action_plan SET updated_at = created_at WHERE updated_at IS NULL;

ALTER TABLE action_plan
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE action_plan
    ADD COLUMN action_plan_template_id UUID REFERENCES action_plan_template(id) ON DELETE SET NULL;

ALTER TABLE action_plan
    ADD CONSTRAINT unique_action_plan_template_id UNIQUE (action_plan_template_id, referral_id);

COMMENT ON COLUMN action_plan.action_plan_template_id IS 'Foreign key reference to action_plan_template.id; allows for associating an action plan with a specific template';

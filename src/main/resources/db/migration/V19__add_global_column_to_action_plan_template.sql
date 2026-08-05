-- V19: Add global column to action_plan_template with constraint ensuring only one row can be true

ALTER TABLE action_plan_template ADD COLUMN active_global BOOLEAN NOT NULL DEFAULT false;

COMMENT ON COLUMN action_plan_template.active_global IS 'Indicates if this is the global default template; only one row can have this set to true';

CREATE UNIQUE INDEX uk_action_plan_template_global
    ON action_plan_template (active_global)
    WHERE active_global = true;

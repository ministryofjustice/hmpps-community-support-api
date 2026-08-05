-- V99: Update action_plan_template seed data to include global column

UPDATE action_plan_template
SET active_global = true
WHERE id = 'c191398c-9661-4983-bafb-be649d877183';

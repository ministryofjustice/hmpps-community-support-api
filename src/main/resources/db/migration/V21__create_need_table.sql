-- V21: Create need reference data table

CREATE TABLE IF NOT EXISTS need (
    id UUID NOT NULL PRIMARY KEY,
    label VARCHAR(200) NOT NULL,
    order_number INTEGER NOT NULL,
    CONSTRAINT uk_need_order_number UNIQUE (order_number)
);

COMMENT ON TABLE need IS 'Static reference data for the needs assessed as part of an Action Plan';
COMMENT ON COLUMN need.id IS 'Unique identifier for the need';
COMMENT ON COLUMN need.label IS 'Human-readable label for the need';
COMMENT ON COLUMN need.order_number IS 'Display order of the need; starts at 1';

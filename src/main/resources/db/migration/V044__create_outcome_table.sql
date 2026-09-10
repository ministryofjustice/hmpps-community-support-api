CREATE TABLE IF NOT EXISTS outcome (
    id UUID NOT NULL PRIMARY KEY,
    need_id UUID NOT NULL,
    text TEXT NOT NULL,
    order_number INTEGER NOT NULL,
    setting VARCHAR(20) NOT NULL,

    CONSTRAINT fk_outcome_need
        FOREIGN KEY (need_id) REFERENCES need(id) ON DELETE CASCADE,

    CONSTRAINT uk_outcome_need_order_number
        UNIQUE (need_id, order_number),

    CONSTRAINT chk_outcome_order_number_min
        CHECK (order_number >= 1),

    CONSTRAINT chk_outcome_setting
        CHECK (setting IN ('CUSTODY', 'COMMUNITY', 'ALL'))
);

CREATE INDEX IF NOT EXISTS idx_outcome_need_id
    ON outcome (need_id);

COMMENT ON TABLE outcome IS 'Reference data for outcomes linked to each need';
COMMENT ON COLUMN outcome.id IS 'Unique identifier for the outcome';
COMMENT ON COLUMN outcome.need_id IS 'Foreign key reference to need.id';
COMMENT ON COLUMN outcome.text IS 'Display text for the outcome';
COMMENT ON COLUMN outcome.order_number IS 'Display order of the outcome within a need; starts at 1';
COMMENT ON COLUMN outcome.setting IS 'Applies to custody, community, or both settings';

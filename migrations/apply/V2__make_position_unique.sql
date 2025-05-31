ALTER TABLE step_position ADD CONSTRAINT step_position_parent_id_position_unique UNIQUE (parent_id, "position");
ALTER TABLE IF EXISTS step_position DROP CONSTRAINT IF EXISTS step_position_position_unique;

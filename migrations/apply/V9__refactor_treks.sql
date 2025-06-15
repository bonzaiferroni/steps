ALTER TABLE trek ADD super_id uuid NULL;
ALTER TABLE trek ADD super_path_step_id uuid NULL;
ALTER TABLE trek ADD progress INT NOT NULL;
ALTER TABLE trek ADD CONSTRAINT fk_trek_super_id__id FOREIGN KEY (super_id) REFERENCES trek(id) ON DELETE CASCADE ON UPDATE RESTRICT;
ALTER TABLE trek ADD CONSTRAINT fk_trek_super_path_step_id__id FOREIGN KEY (super_path_step_id) REFERENCES path_step(id) ON DELETE CASCADE ON UPDATE RESTRICT;
ALTER TABLE trek DROP COLUMN step_index;
ALTER TABLE trek DROP COLUMN step_count;
ALTER TABLE trek DROP COLUMN path_ids;
ALTER TABLE trek DROP COLUMN bread_crumbs;

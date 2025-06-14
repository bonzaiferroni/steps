CREATE TABLE IF NOT EXISTS deletions (id uuid PRIMARY KEY, user_id uuid NOT NULL, recorded_at TIMESTAMP NOT NULL, CONSTRAINT fk_deletions_user_id__id FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE ON UPDATE RESTRICT);
ALTER TABLE path_step ADD user_id uuid NOT NULL;
ALTER TABLE path_step ADD updated_at TIMESTAMP NOT NULL;
ALTER TABLE path_step ADD CONSTRAINT fk_path_step_user_id__id FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE ON UPDATE RESTRICT;

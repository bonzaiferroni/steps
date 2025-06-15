ALTER TABLE question ADD step_id uuid NOT NULL;
ALTER TABLE question ADD CONSTRAINT fk_question_step_id__id FOREIGN KEY (step_id) REFERENCES step(id) ON DELETE CASCADE ON UPDATE RESTRICT;

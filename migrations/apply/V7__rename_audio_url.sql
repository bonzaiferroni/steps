ALTER TABLE step ADD audio_label_url TEXT NULL;
ALTER TABLE step ADD audio_full_url TEXT NULL;
ALTER TABLE step DROP COLUMN short_audio_url;
ALTER TABLE step DROP COLUMN long_audio_url;

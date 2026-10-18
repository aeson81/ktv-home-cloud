ALTER TABLE song_files
    ADD COLUMN IF NOT EXISTS file_role TEXT NOT NULL DEFAULT 'LIBRARY',
    ADD COLUMN IF NOT EXISTS source_path TEXT,
    ADD COLUMN IF NOT EXISTS source_md5 TEXT,
    ADD COLUMN IF NOT EXISTS output_md5 TEXT,
    ADD COLUMN IF NOT EXISTS transcode_required BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS imported_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS source_deleted BOOLEAN NOT NULL DEFAULT false;

CREATE INDEX IF NOT EXISTS idx_song_files_role ON song_files (file_role);
CREATE INDEX IF NOT EXISTS idx_song_files_source_md5 ON song_files (source_md5);
CREATE INDEX IF NOT EXISTS idx_song_files_output_md5 ON song_files (output_md5);

CREATE TABLE IF NOT EXISTS media_import_records (
  id                 BIGSERIAL PRIMARY KEY,
  source_path        TEXT NOT NULL UNIQUE,
  source_filename    TEXT NOT NULL,
  source_md5         TEXT NOT NULL,
  output_path        TEXT,
  output_md5         TEXT,
  output_format      TEXT,
  video_codec        TEXT,
  audio_codec        TEXT,
  action             TEXT NOT NULL,
  reason             TEXT,
  song_id            BIGINT REFERENCES songs(id) ON DELETE SET NULL,
  song_file_id       BIGINT REFERENCES song_files(id) ON DELETE SET NULL,
  transcode_required BOOLEAN NOT NULL DEFAULT false,
  source_deleted     BOOLEAN NOT NULL DEFAULT false,
  created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_media_import_records_action ON media_import_records (action, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_media_import_records_source_md5 ON media_import_records (source_md5);
CREATE INDEX IF NOT EXISTS idx_media_import_records_output_md5 ON media_import_records (output_md5);

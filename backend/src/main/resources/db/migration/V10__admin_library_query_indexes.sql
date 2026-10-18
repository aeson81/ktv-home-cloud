CREATE INDEX IF NOT EXISTS idx_songs_admin_created
    ON songs (created_at DESC);

CREATE INDEX IF NOT EXISTS idx_songs_admin_type_created
    ON songs (media_type, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_songs_admin_status_created
    ON songs (status, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_song_files_admin_primary
    ON song_files (song_id, valid, priority DESC);

CREATE INDEX IF NOT EXISTS idx_media_import_records_source_deleted_created
    ON media_import_records (source_deleted, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_media_import_records_transcode_created
    ON media_import_records (transcode_required, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_media_import_records_filename_trgm
    ON media_import_records USING gin (LOWER(source_filename) gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_media_import_records_title_trgm
    ON media_import_records USING gin (LOWER(parsed_title) gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_media_import_records_artist_trgm
    ON media_import_records USING gin (LOWER(parsed_artist) gin_trgm_ops);

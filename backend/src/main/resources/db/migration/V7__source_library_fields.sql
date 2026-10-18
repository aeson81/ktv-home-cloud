ALTER TABLE media_import_records
    ADD COLUMN IF NOT EXISTS parsed_title TEXT,
    ADD COLUMN IF NOT EXISTS parsed_artist TEXT,
    ADD COLUMN IF NOT EXISTS media_type TEXT,
    ADD COLUMN IF NOT EXISTS source_format TEXT,
    ADD COLUMN IF NOT EXISTS duplicate_flag BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS imported_flag BOOLEAN NOT NULL DEFAULT false;

CREATE INDEX IF NOT EXISTS idx_media_import_records_duplicate_flag
    ON media_import_records (duplicate_flag, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_media_import_records_imported_flag
    ON media_import_records (imported_flag, created_at DESC);

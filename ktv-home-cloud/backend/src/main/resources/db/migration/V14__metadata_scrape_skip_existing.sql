ALTER TABLE music_metadata_scrape_batches
    ADD COLUMN IF NOT EXISTS skipped_existing INTEGER NOT NULL DEFAULT 0;

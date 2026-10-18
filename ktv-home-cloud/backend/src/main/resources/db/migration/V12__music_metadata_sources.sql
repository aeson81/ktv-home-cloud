-- Embedded multi-provider music metadata aggregation.

ALTER TABLE songs
    ADD COLUMN IF NOT EXISTS album VARCHAR(500),
    ADD COLUMN IF NOT EXISTS release_date VARCHAR(32),
    ADD COLUMN IF NOT EXISTS aliases TEXT[] NOT NULL DEFAULT '{}';

CREATE TABLE music_source_tracks (
    id             BIGSERIAL PRIMARY KEY,
    provider       VARCHAR(24) NOT NULL,
    external_id    VARCHAR(160) NOT NULL,
    title          VARCHAR(500) NOT NULL,
    artists        JSONB NOT NULL DEFAULT '[]'::jsonb,
    album          VARCHAR(500),
    duration_ms    INTEGER,
    release_date   VARCHAR(32),
    aliases        JSONB NOT NULL DEFAULT '[]'::jsonb,
    cover_url      TEXT,
    availability   VARCHAR(32) NOT NULL DEFAULT 'UNKNOWN',
    fetched_at     TIMESTAMPTZ NOT NULL,
    expires_at     TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_music_source_track UNIQUE (provider, external_id)
);
CREATE INDEX idx_music_source_tracks_expiry ON music_source_tracks(expires_at);

CREATE TABLE music_source_search_cache (
    cache_key      VARCHAR(96) PRIMARY KEY,
    provider       VARCHAR(24) NOT NULL,
    normalized_query VARCHAR(500) NOT NULL,
    result_json    JSONB NOT NULL,
    fetched_at     TIMESTAMPTZ NOT NULL,
    expires_at     TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_music_source_search_expiry ON music_source_search_cache(expires_at);

CREATE TABLE song_external_matches (
    id             BIGSERIAL PRIMARY KEY,
    song_id        BIGINT NOT NULL REFERENCES songs(id) ON DELETE CASCADE,
    provider       VARCHAR(24) NOT NULL,
    external_id    VARCHAR(160) NOT NULL,
    match_score    DOUBLE PRECISION NOT NULL DEFAULT 0,
    status         VARCHAR(24) NOT NULL DEFAULT 'SUGGESTED',
    field_sources  JSONB NOT NULL DEFAULT '{}'::jsonb,
    confirmed_at   TIMESTAMPTZ,
    applied_at     TIMESTAMPTZ,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_song_external_match UNIQUE (song_id, provider, external_id),
    CONSTRAINT fk_song_external_track FOREIGN KEY (provider, external_id)
        REFERENCES music_source_tracks(provider, external_id) ON DELETE CASCADE
);
CREATE INDEX idx_song_external_matches_song ON song_external_matches(song_id, match_score DESC);

CREATE TABLE music_source_provider_state (
    provider       VARCHAR(24) PRIMARY KEY,
    anonymous_device_id VARCHAR(160),
    healthy        BOOLEAN NOT NULL DEFAULT false,
    last_success_at TIMESTAMPTZ,
    last_error_at  TIMESTAMPTZ,
    last_error     VARCHAR(500),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

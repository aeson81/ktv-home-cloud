ALTER TABLE songs
    ADD COLUMN ai_language VARCHAR(32),
    ADD COLUMN ai_era VARCHAR(32),
    ADD COLUMN ai_genres TEXT[] NOT NULL DEFAULT '{}',
    ADD COLUMN ai_themes TEXT[] NOT NULL DEFAULT '{}',
    ADD COLUMN ai_age_range VARCHAR(32),
    ADD COLUMN ai_vocal_form VARCHAR(32),
    ADD COLUMN ai_analyzed_at TIMESTAMPTZ;

CREATE TABLE ai_analysis_tasks (
    id              BIGSERIAL PRIMARY KEY,
    song_id         BIGINT NOT NULL REFERENCES songs(id) ON DELETE CASCADE,
    status          VARCHAR(20) NOT NULL DEFAULT 'pending',
    attempt_count   INTEGER NOT NULL DEFAULT 0,
    model           VARCHAR(100) NOT NULL,
    result_json     TEXT,
    error_message   TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_ai_tasks_status ON ai_analysis_tasks(status, created_at);
CREATE INDEX idx_ai_tasks_song ON ai_analysis_tasks(song_id, created_at DESC);

CREATE TABLE playlists (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(120) NOT NULL,
    description     TEXT NOT NULL DEFAULT '',
    theme            VARCHAR(80),
    cover_path      TEXT,
    public          BOOLEAN NOT NULL DEFAULT true,
    ai_generated    BOOLEAN NOT NULL DEFAULT false,
    ai_rule         TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE playlist_songs (
    playlist_id     BIGINT NOT NULL REFERENCES playlists(id) ON DELETE CASCADE,
    song_id         BIGINT NOT NULL REFERENCES songs(id) ON DELETE CASCADE,
    sort_order      INTEGER NOT NULL,
    manual          BOOLEAN NOT NULL DEFAULT false,
    PRIMARY KEY (playlist_id, song_id)
);
CREATE INDEX idx_playlist_songs_order ON playlist_songs(playlist_id, sort_order);

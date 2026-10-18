CREATE TABLE favorites (
  id         BIGSERIAL PRIMARY KEY,
  user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  song_id    BIGINT NOT NULL REFERENCES songs(id) ON DELETE CASCADE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uk_favorites_user_song UNIQUE (user_id, song_id)
);

CREATE INDEX idx_favorites_user_created ON favorites (user_id, created_at DESC);

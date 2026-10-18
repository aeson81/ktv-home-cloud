-- 家庭 KTV 点歌系统 · 初始 Schema
-- 严格对应《详细需求设计文档》§10（PostgreSQL 16）

CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- 轻量点歌人标识，非账号体系
CREATE TABLE users (
  id           BIGSERIAL PRIMARY KEY,
  client_token TEXT NOT NULL UNIQUE,       -- H5 localStorage 生成的 UUID
  nickname     TEXT NOT NULL,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE songs (
  id              BIGSERIAL PRIMARY KEY,
  title           TEXT NOT NULL,
  artist          TEXT NOT NULL DEFAULT '未知歌手',
  title_py        TEXT NOT NULL DEFAULT '',  -- 全拼，如 qingtian
  title_init      TEXT NOT NULL DEFAULT '',  -- 首字母，如 qt
  artist_py       TEXT NOT NULL DEFAULT '',
  artist_init     TEXT NOT NULL DEFAULT '',
  language        TEXT NOT NULL DEFAULT '国语',
  tags            TEXT[] NOT NULL DEFAULT '{}',  -- 对唱/儿歌/影视金曲…
  media_type      TEXT NOT NULL,             -- KTV_VIDEO / MV / AUDIO
  has_vocal_track BOOLEAN NOT NULL DEFAULT false,  -- 是否可切伴唱（≥2音轨）
  duration_ms     INT  NOT NULL DEFAULT 0,
  cover_path      TEXT,                      -- 缓存封面路径（内嵌提取）
  lyric_path      TEXT,                      -- 缓存歌词路径
  lyric_type      TEXT NOT NULL DEFAULT 'none',  -- word / line / sub / none
  play_count      INT  NOT NULL DEFAULT 0,
  status          TEXT NOT NULL DEFAULT 'ok',    -- ok / file_missing
  fingerprint     TEXT NOT NULL UNIQUE,      -- md5(lower(artist)|lower(title)|duration_bucket)
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_songs_title_trgm  ON songs USING gin (title gin_trgm_ops);
CREATE INDEX idx_songs_artist_trgm ON songs USING gin (artist gin_trgm_ops);
CREATE INDEX idx_songs_py          ON songs (title_init, title_py);
CREATE INDEX idx_songs_artist_py   ON songs (artist_init, artist_py);  -- 支撑歌手拼音/首字母搜索（zjl→周杰伦）
CREATE INDEX idx_songs_artist      ON songs (artist);

-- 一首歌可对应多个文件源
CREATE TABLE song_files (
  id           BIGSERIAL PRIMARY KEY,
  song_id      BIGINT NOT NULL REFERENCES songs(id) ON DELETE CASCADE,
  file_path    TEXT NOT NULL UNIQUE,       -- 容器内路径 /music/xxx.mkv
  format       TEXT NOT NULL,              -- mkv/mp4/mp3/flac
  audio_tracks INT  NOT NULL DEFAULT 1,
  vocal_track_index INT,                   -- 伴唱轨 index（入库探测，免运行时猜轨）
  resolution   TEXT,
  file_size    BIGINT NOT NULL DEFAULT 0,
  file_mtime   TIMESTAMPTZ NOT NULL,
  priority     INT  NOT NULL DEFAULT 0     -- 多文件源时优先选大值（KTV版优先）
);
CREATE INDEX idx_song_files_song ON song_files (song_id);

CREATE TABLE queue (
  id          BIGSERIAL PRIMARY KEY,
  song_id     BIGINT NOT NULL REFERENCES songs(id),
  ordered_by  BIGINT REFERENCES users(id),
  order_index DOUBLE PRECISION NOT NULL,   -- 顶歌用分数插入，避免整列重排
  status      TEXT NOT NULL DEFAULT 'waiting', -- waiting/playing/done/skipped
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  played_at   TIMESTAMPTZ
);
CREATE INDEX idx_queue_status ON queue (status, order_index);

-- 单行表，房间播放器状态
CREATE TABLE player_state (
  id               SMALLINT PRIMARY KEY DEFAULT 1 CHECK (id = 1),
  current_queue_id BIGINT REFERENCES queue(id),
  state            TEXT NOT NULL DEFAULT 'idle',  -- idle/playing/paused
  volume           INT  NOT NULL DEFAULT 60,
  muted            BOOLEAN NOT NULL DEFAULT false,
  vocal_mode       TEXT NOT NULL DEFAULT 'accompaniment', -- original/accompaniment
  updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);
-- 初始化单行播放状态
INSERT INTO player_state (id) VALUES (1);

CREATE TABLE play_history (
  id         BIGSERIAL PRIMARY KEY,
  song_id    BIGINT NOT NULL REFERENCES songs(id),
  played_by  BIGINT REFERENCES users(id),
  played_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_play_history_played_at ON play_history (played_at);
CREATE INDEX idx_play_history_song ON play_history (song_id);

-- 心愿单（缺歌反馈）
CREATE TABLE wishes (
  id         BIGSERIAL PRIMARY KEY,
  keyword    TEXT NOT NULL,
  created_by BIGINT REFERENCES users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE settings (
  key   TEXT PRIMARY KEY,
  value JSONB NOT NULL
);

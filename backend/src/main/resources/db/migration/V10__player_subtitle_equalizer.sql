-- V10: 播放器状态新增字幕与均衡器字段
ALTER TABLE player_state ADD COLUMN IF NOT EXISTS subtitle_visible BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE player_state ADD COLUMN IF NOT EXISTS equalizer_preset VARCHAR(32) NOT NULL DEFAULT 'standard';

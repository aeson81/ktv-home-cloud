-- 伴奏轨判定置信度（第 1 层元数据判定，供后台筛选人工复核）
-- HIGH/MEDIUM/LOW/NONE；LOW 表示回落默认轨、建议人工确认原伴唱
ALTER TABLE song_files ADD COLUMN IF NOT EXISTS vocal_confidence TEXT;

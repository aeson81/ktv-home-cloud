-- Keep only the newest active AI task for each song before enforcing idempotency.
WITH ranked AS (
    SELECT id,
           row_number() OVER (PARTITION BY song_id ORDER BY created_at DESC, id DESC) AS row_number
    FROM ai_analysis_tasks
    WHERE status IN ('pending', 'processing', 'review')
)
UPDATE ai_analysis_tasks
SET status = 'failed',
    error_message = 'Superseded by a newer active task during idempotency migration',
    updated_at = now()
WHERE id IN (SELECT id FROM ranked WHERE row_number > 1);

CREATE UNIQUE INDEX uk_ai_tasks_one_active_per_song
    ON ai_analysis_tasks(song_id)
    WHERE status IN ('pending', 'processing', 'review');

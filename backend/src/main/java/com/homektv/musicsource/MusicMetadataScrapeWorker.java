package com.homektv.musicsource;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@Service
public class MusicMetadataScrapeWorker {
    private static final Set<String> APPLY_FIELDS = Set.of("title", "artist", "album", "releaseDate", "aliases", "cover");
    private final JdbcTemplate jdbc;
    private final MusicSourceSearchService searchService;
    private final MusicMetadataApplyService applyService;
    private final MusicSourceConfigService configService;
    private final ObjectMapper mapper;

    public MusicMetadataScrapeWorker(JdbcTemplate jdbc, MusicSourceSearchService searchService,
                                     MusicMetadataApplyService applyService, MusicSourceConfigService configService,
                                     ObjectMapper mapper) {
        this.jdbc = jdbc;
        this.searchService = searchService;
        this.applyService = applyService;
        this.configService = configService;
        this.mapper = mapper;
    }

    @Async("metadataScrapeExecutor")
    public void process(String batchId) {
        if (!isRunning(batchId)) return;
        jdbc.update("UPDATE music_metadata_scrape_batches SET started_at=COALESCE(started_at,now()),updated_at=now() WHERE id=?", batchId);
        List<Long> itemIds = jdbc.query("SELECT id FROM music_metadata_scrape_items WHERE batch_id=? AND status='PENDING' ORDER BY id",
                (rs, index) -> rs.getLong(1), batchId);
        int concurrency = Math.max(1, Math.min(configService.getConfig().concurrencyLimit(), itemIds.size()));
        if (!itemIds.isEmpty()) {
            try (var executor = Executors.newFixedThreadPool(concurrency)) {
                List<CompletableFuture<Void>> futures = itemIds.stream()
                        .map(id -> CompletableFuture.runAsync(() -> processItem(batchId, id), executor)).toList();
                CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
            }
        }
        finishIfDone(batchId);
    }

    private void processItem(String batchId, long itemId) {
        if (!isRunning(batchId)) return;
        int claimed = jdbc.update("""
                UPDATE music_metadata_scrape_items SET status='PROCESSING',started_at=now(),error_message=NULL,updated_at=now()
                WHERE id=? AND batch_id=? AND status='PENDING'
                """, itemId, batchId);
        if (claimed == 0) return;
        ItemTarget target = jdbc.query("SELECT song_id FROM music_metadata_scrape_items WHERE id=?",
                rs -> rs.next() ? new ItemTarget((Long) rs.getObject(1)) : null, itemId);
        if (target == null || target.songId() == null) {
            fail(itemId, "歌曲已删除");
            return;
        }
        try {
            List<MusicSourceSearchService.SongMatch> matches = searchService.matches(target.songId(), false);
            MusicSourceSearchService.SongMatch best = matches.isEmpty() ? null : matches.getFirst();
            if (best == null) {
                review(itemId, null, "未找到可用的元数据候选");
                return;
            }
            ExternalTrack track = best.track();
            String json = mapper.writeValueAsString(best);
            jdbc.update("""
                    UPDATE music_metadata_scrape_items SET provider=?,external_id=?,match_score=?,result_json=CAST(? AS jsonb),updated_at=now()
                    WHERE id=?
                    """, track.provider().name(), track.externalId(), best.score(), json, itemId);
            if (!isRunning(batchId)) {
                jdbc.update("UPDATE music_metadata_scrape_items SET status='PENDING',updated_at=now() WHERE id=?", itemId);
                return;
            }
            double threshold = jdbc.queryForObject(
                    "SELECT auto_apply_threshold FROM music_metadata_scrape_batches WHERE id=?", Double.class, batchId);
            if (best.score() < threshold) {
                review(itemId, best, "匹配度低于自动写入阈值，等待人工审核");
                return;
            }
            try {
                applyService.apply(target.songId(), track.provider(), track.externalId(),
                        new MusicMetadataApplyService.ApplyRequest(APPLY_FIELDS));
                terminal(itemId, "AUTO_APPLIED", null);
            } catch (RuntimeException ex) {
                review(itemId, best, "自动写入未执行：" + safe(ex));
            }
        } catch (Exception ex) {
            fail(itemId, safe(ex));
        }
    }

    private boolean isRunning(String batchId) {
        List<String> values = jdbc.query("SELECT status FROM music_metadata_scrape_batches WHERE id=?",
                (rs, index) -> rs.getString(1), batchId);
        return !values.isEmpty() && "RUNNING".equals(values.getFirst());
    }

    private void review(long itemId, MusicSourceSearchService.SongMatch match, String message) {
        if (match == null) {
            terminal(itemId, "REVIEW", message);
            return;
        }
        terminal(itemId, "REVIEW", message);
    }

    private void fail(long itemId, String message) { terminal(itemId, "FAILED", message); }

    private void terminal(long itemId, String status, String message) {
        jdbc.update("""
                UPDATE music_metadata_scrape_items SET status=?,error_message=?,finished_at=now(),updated_at=now() WHERE id=?
                """, status, ProviderJson.clean(message, 1000), itemId);
    }

    private void finishIfDone(String batchId) {
        Integer active = jdbc.queryForObject("""
                SELECT COUNT(*) FROM music_metadata_scrape_items WHERE batch_id=? AND status IN ('PENDING','PROCESSING')
                """, Integer.class, batchId);
        if (active != null && active == 0) {
            jdbc.update("""
                    UPDATE music_metadata_scrape_batches SET status='COMPLETED',finished_at=now(),updated_at=now()
                    WHERE id=? AND status='RUNNING'
                    """, batchId);
        }
    }

    private static String safe(Throwable ex) {
        String value = ex.getMessage();
        if ((value == null || value.isBlank()) && ex.getCause() != null) value = ex.getCause().getMessage();
        return ProviderJson.clean(value == null || value.isBlank() ? ex.getClass().getSimpleName() : value, 1000);
    }

    private record ItemTarget(Long songId) {}
}

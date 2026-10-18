package com.homektv.library;

import com.homektv.media.MediaProbe;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * 媒体类型判定与指纹去重（P1.3，详设§9.3）。
 */
public final class MediaClassifier {

    public static final String KTV_VIDEO = "KTV_VIDEO";
    public static final String MV = "MV";
    public static final String AUDIO = "AUDIO";

    /** 时长分桶粒度（毫秒）：±2s 视为同一首，用 2000ms 桶 */
    private static final long DURATION_BUCKET_MS = 2000;

    private MediaClassifier() {}

    /**
     * 类型判定（详设§9.3）：
     * - ≥2 音轨且含视频 → KTV_VIDEO（原唱/伴奏双轨）
     * - 单音轨视频 → MV
     * - 纯音频（无视频） → AUDIO
     */
    public static String classify(MediaProbe probe) {
        if (probe.hasVideo()) {
            return probe.audioTracks() >= 2 ? KTV_VIDEO : MV;
        }
        return AUDIO;
    }

    /** 是否可切伴唱：含独立伴奏音轨（≥2 音轨） */
    public static boolean hasVocalTrack(MediaProbe probe) {
        return probe.audioTracks() >= 2;
    }

    /**
     * 指纹：md5(lower(artist)|lower(title)|durationBucket)。
     * 时长按 2s 分桶，容忍不同文件源的轻微时长差异。
     */
    public static String fingerprint(String artist, String title, long durationMs) {
        long bucket = durationMs / DURATION_BUCKET_MS;
        String raw = safeLower(artist) + "|" + safeLower(title) + "|" + bucket;
        return md5(raw);
    }

    private static String safeLower(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    private static String md5(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(s.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 不可用", e);
        }
    }
}

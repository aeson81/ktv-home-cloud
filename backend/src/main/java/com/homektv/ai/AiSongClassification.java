package com.homektv.ai;

import java.util.List;

/**
 * AI 歌曲分类结果记录，包含语言、年代、风格、主题、年龄范围、演唱形式、推荐歌单、分类理由和置信度等信息。
 * 紧凑构造函数会自动对可空集合字段做防御性拷贝，并将置信度钳位到 [0.0, 1.0] 区间。
 *
 * An AI song classification result record, containing language, era, genres, themes,
 * age range, vocal form, recommended playlists, classification reason, and confidence.
 * The compact constructor automatically performs defensive copies on nullable collection
 * fields and clamps confidence to the [0.0, 1.0] range.
 */
public record AiSongClassification(
        String title,
        String artist,
        String language,
        String era,
        List<String> genres,
        List<String> themes,
        String ageRange,
        String vocalForm,
        List<String> recommendedPlaylists,
        String reason,
        Double confidence,
        Double titleConfidence,
        Double artistConfidence,
        Double languageConfidence,
        Double vocalFormConfidence,
        String artistGender,
        java.util.Map<String, Object> evidence
) {
    /**
     * 紧凑构造函数：对 {@code genres}、{@code themes}、{@code recommendedPlaylists} 做不可变防御性拷贝，
     * 并将 {@code confidence} 钳位到 [0.0, 1.0] 范围。
     *
     * Compact constructor: makes immutable defensive copies of {@code genres},
     * {@code themes}, and {@code recommendedPlaylists}, and clamps
     * {@code confidence} to the [0.0, 1.0] range.
     */
    public AiSongClassification {
        genres = genres == null ? List.of() : List.copyOf(genres);
        themes = themes == null ? List.of() : List.copyOf(themes);
        recommendedPlaylists = recommendedPlaylists == null ? List.of() : List.copyOf(recommendedPlaylists);
        confidence = confidence == null ? 0.0 : Math.max(0.0, Math.min(1.0, confidence));
        titleConfidence = clamp(titleConfidence, confidence);
        artistConfidence = clamp(artistConfidence, confidence);
        languageConfidence = clamp(languageConfidence, confidence);
        vocalFormConfidence = clamp(vocalFormConfidence, confidence);
        evidence = evidence == null ? java.util.Map.of() : java.util.Map.copyOf(evidence);
    }

    public AiSongClassification(String language, String era, List<String> genres, List<String> themes,
                                String ageRange, String vocalForm, List<String> recommendedPlaylists,
                                String reason, Double confidence) {
        this(null, null, language, era, genres, themes, ageRange, vocalForm, recommendedPlaylists,
                reason, confidence, confidence, confidence, confidence, confidence, "未知", java.util.Map.of());
    }

    private static double clamp(Double value, Double fallback) {
        double selected = value == null ? (fallback == null ? 0 : fallback) : value;
        return Math.max(0, Math.min(1, selected));
    }
}

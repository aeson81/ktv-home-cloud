package com.homektv.ai;

import com.homektv.domain.MediaImportRecord;
import com.homektv.domain.Song;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 本地分类降级方案。只使用扫描阶段已经得到的标签、文件名解析结果和歌曲现有字段，
 * 不对语种、演唱形式或曲风做无证据推断；结果统一进入人工审核。
 */
@Service
public class LocalClassificationService {
    public AiSongClassification fromSong(Song song) {
        String title = text(song.getTitle());
        String artist = text(song.getArtist());
        double identityConfidence = known(title) && knownArtist(artist) && "ok".equalsIgnoreCase(song.getStatus()) ? 0.86 : 0.35;
        String language = knownLanguage(song.getLanguage()) ? song.getLanguage() : "未知";
        String vocalForm = knownVocal(song.getVocalForm()) ? song.getVocalForm() : "未知";
        double languageConfidence = knownLanguage(song.getLanguage()) ? 0.86 : 0.0;
        double vocalConfidence = knownVocal(song.getVocalForm()) ? 0.86 : 0.0;
        Map<String, Object> evidence = new LinkedHashMap<>();
        evidence.put("source", "local_song_fields");
        evidence.put("status", text(song.getStatus()));
        evidence.put("metadataProvenance", text(song.getMetadataProvenance()));
        return new AiSongClassification(title, artist, language, song.getAiEra(),
                list(song.getAiGenres()), list(song.getAiThemes()), song.getAiAgeRange(), vocalForm,
                List.of(), "未配置 AI，使用本地标签和扫描结果；请人工确认分类字段",
                Math.min(identityConfidence, Math.min(languageConfidence == 0 ? identityConfidence : languageConfidence,
                        vocalConfidence == 0 ? identityConfidence : vocalConfidence)),
                identityConfidence, identityConfidence, languageConfidence, vocalConfidence,
                knownGender(song.getArtistGender()) ? song.getArtistGender() : "未知", evidence);
    }

    public AiSongClassification fromImport(MediaImportRecord record) {
        String title = text(record.getParsedTitle());
        String artist = text(record.getParsedArtist());
        double confidence = known(title) && knownArtist(artist) ? 0.86 : 0.35;
        Map<String, Object> evidence = new LinkedHashMap<>();
        evidence.put("source", "local_import_record");
        evidence.put("filename", text(record.getSourceFilename()));
        evidence.put("reason", text(record.getReason()));
        return new AiSongClassification(title, artist, "未知", "未知", List.of(), List.of(),
                "未知", "未知", List.of(), "未配置 AI，使用本地文件名/标签解析；请人工确认分类字段",
                confidence, confidence, confidence, 0.0, 0.0, "未知", evidence);
    }

    private static boolean known(String value) { return value != null && !value.isBlank() && !"未知".equals(value); }
    private static boolean knownArtist(String value) { return known(value) && !"未知歌手".equals(value); }
    private static boolean knownLanguage(String value) { return value != null && !value.isBlank() && !"未知".equals(value); }
    private static boolean knownVocal(String value) { return value != null && !value.isBlank() && !"未知".equals(value); }
    private static boolean knownGender(String value) { return value != null && !value.isBlank() && !"未知".equals(value); }
    private static String text(String value) { return value == null ? "" : value.trim(); }
    private static List<String> list(String[] values) { return values == null ? List.of() : java.util.Arrays.stream(values).filter(v -> v != null && !v.isBlank()).toList(); }
}

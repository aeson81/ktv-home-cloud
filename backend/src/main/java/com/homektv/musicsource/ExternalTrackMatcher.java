package com.homektv.musicsource;

import com.homektv.domain.Song;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class ExternalTrackMatcher {
    private static final Pattern DISPLAY_MARKER = Pattern.compile(
            "[（(]\\s*(?:mtv|mv|ktv|演|原唱)\\s*[)）]", Pattern.CASE_INSENSITIVE);
    private static final Pattern CATALOG_SUFFIX = Pattern.compile(
            "(?:[-_—–]+\\s*(?:国语|粤语|闽南语|英语|日语|韩语|纯音乐|其他|未知|流行(?:歌曲)?|摇滚|民谣|电子|舞曲|经典|影视原声|儿歌|戏曲|说唱|r&b|爵士|古典))+$",
            Pattern.CASE_INSENSITIVE);

    public double score(Song song, ExternalTrack track) {
        double title = similarity(normalizeTitle(song.getTitle()), normalizeTitle(track.title()));
        String externalArtists = String.join(" ", track.artists());
        double artist = similarity(normalize(song.getArtist()), normalize(externalArtists));
        double duration = 0.5;
        if (song.getDurationMs() > 0 && track.durationMs() != null && track.durationMs() > 0) {
            int difference = Math.abs(song.getDurationMs() - track.durationMs());
            duration = Math.max(0, 1 - difference / 30_000.0);
        }
        double score = title * 0.55 + artist * 0.35 + duration * 0.10;
        return Math.round(score * 1000) / 1000.0;
    }

    public String groupKey(ExternalTrack track) {
        int bucket = track.durationMs() == null ? -1 : (track.durationMs() + 15_000) / 30_000;
        return normalizeTitle(track.title()) + "|" + normalize(String.join(" ", track.artists())) + "|" + bucket;
    }

    private static String normalizeTitle(String value) {
        if (value == null) return "";
        String cleaned = DISPLAY_MARKER.matcher(Normalizer.normalize(value, Normalizer.Form.NFKC)).replaceAll("");
        cleaned = CATALOG_SUFFIX.matcher(cleaned).replaceAll("");
        return normalize(cleaned);
    }

    private static String normalize(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT)
                .replaceAll("[\\p{Punct}\\s·•]+", "");
    }

    private static double similarity(String left, String right) {
        if (left.equals(right)) return left.isEmpty() ? 0 : 1;
        if (left.isEmpty() || right.isEmpty()) return 0;
        if (left.contains(right) || right.contains(left)) return (double) Math.min(left.length(), right.length()) / Math.max(left.length(), right.length());
        int[] previous = new int[right.length() + 1];
        for (int j = 0; j <= right.length(); j++) previous[j] = j;
        for (int i = 1; i <= left.length(); i++) {
            int[] current = new int[right.length() + 1]; current[0] = i;
            for (int j = 1; j <= right.length(); j++) current[j] = Math.min(Math.min(current[j - 1] + 1, previous[j] + 1), previous[j - 1] + (left.charAt(i - 1) == right.charAt(j - 1) ? 0 : 1));
            previous = current;
        }
        return Math.max(0, 1.0 - (double) previous[right.length()] / Math.max(left.length(), right.length()));
    }
}

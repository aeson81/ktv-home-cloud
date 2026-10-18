package com.homektv.library;

/**
 * 从标签或文件名解析出的元数据（P1.1/P1.2）。
 *
 * @param title      歌名（可能为空 → 未识别）
 * @param artist     歌手（可能为空）
 * @param recognized 是否成功识别（标签或文件名解析出有效歌名）
 */
public record ParsedMeta(String title, String artist, boolean recognized) {

    public static ParsedMeta unrecognized(String fallbackTitle) {
        return new ParsedMeta(fallbackTitle, "", false);
    }

    public static ParsedMeta of(String title, String artist) {
        boolean ok = title != null && !title.isBlank();
        return new ParsedMeta(
                ok ? title.trim() : "",
                artist == null ? "" : artist.trim(),
                ok
        );
    }
}

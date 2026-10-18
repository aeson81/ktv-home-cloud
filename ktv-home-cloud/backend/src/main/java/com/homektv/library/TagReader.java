package com.homektv.library;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.logging.Level;

/**
 * 内嵌标签解析（P1.1）：ID3(MP3) / Vorbis Comment(FLAC) 等音频格式。
 * 读取标题/歌手/语种/内嵌封面/内嵌歌词。视频容器(mkv/mp4)标签有限，
 * 解析失败时返回空 TagInfo，由文件名兜底（P1.2）。
 */
@Component
public class TagReader {

    private static final Logger log = LoggerFactory.getLogger(TagReader.class);

    static {
        // jaudiotagger 默认打印大量 INFO 日志，降噪
        java.util.logging.Logger.getLogger("org.jaudiotagger").setLevel(Level.WARNING);
    }

    public TagInfo read(File file) {
        TagInfo info = new TagInfo();
        try {
            AudioFile af = AudioFileIO.read(file);
            Tag tag = af.getTag();
            if (tag == null) return info;

            info.setTitle(firstOrNull(tag, FieldKey.TITLE));
            info.setArtist(firstOrNull(tag, FieldKey.ARTIST));
            info.setLanguage(firstOrNull(tag, FieldKey.LANGUAGE));
            info.setEmbeddedLyric(firstOrNull(tag, FieldKey.LYRICS));

            Artwork art = tag.getFirstArtwork();
            if (art != null && art.getBinaryData() != null) {
                info.setCoverImage(art.getBinaryData());
                info.setCoverExt(mimeToExt(art.getMimeType()));
            }
        } catch (Exception e) {
            // 视频容器或损坏文件解析失败属正常，交由文件名兜底
            log.debug("标签解析失败（将走文件名兜底）：{} - {}", file.getName(), e.getMessage());
        }
        return info;
    }

    private static String firstOrNull(Tag tag, FieldKey key) {
        try {
            String v = tag.getFirst(key);
            return (v == null || v.isBlank()) ? null : v.trim();
        } catch (Exception e) {
            return null;
        }
    }

    private static String mimeToExt(String mime) {
        if (mime == null) return "jpg";
        String m = mime.toLowerCase();
        if (m.contains("png")) return "png";
        if (m.contains("webp")) return "webp";
        return "jpg";
    }
}

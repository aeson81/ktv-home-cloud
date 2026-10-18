package com.homektv.library;

/**
 * 内嵌标签解析结果（P1.1）。封面/歌词以字节/文本形式返回，由扫描服务落盘。
 */
public class TagInfo {
    private String title;
    private String artist;
    private String language;
    private byte[] coverImage;      // 内嵌封面（可空）
    private String coverExt;        // 封面扩展名，如 jpg/png
    private String embeddedLyric;   // 内嵌歌词文本（可空）

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public byte[] getCoverImage() { return coverImage; }
    public void setCoverImage(byte[] coverImage) { this.coverImage = coverImage; }
    public String getCoverExt() { return coverExt; }
    public void setCoverExt(String coverExt) { this.coverExt = coverExt; }
    public String getEmbeddedLyric() { return embeddedLyric; }
    public void setEmbeddedLyric(String embeddedLyric) { this.embeddedLyric = embeddedLyric; }

    public boolean hasTitle() { return title != null && !title.isBlank(); }
}

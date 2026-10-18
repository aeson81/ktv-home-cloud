package com.homektv.media;

/**
 * 单条音频流的元数据（ffprobe -show_streams 采集），供入库时判定伴奏/原唱轨。
 *
 * @param index      音频相对序号（0-based，仅在音频流中计数，与 song_files.vocalTrackIndex 语义一致）
 * @param title      音轨标题标签（如「伴奏」「原唱」「Instrumental」），可空
 * @param language   语言标签（如 zho/chi/eng），可空
 * @param channels   声道数（1=mono，2=stereo）
 * @param karaoke    ffmpeg karaoke disposition 标志位（1 表示该轨被标记为卡拉OK/伴奏轨）
 * @param isDefault  是否为默认音轨（disposition.default）
 */
public record AudioStreamInfo(
        int index,
        String title,
        String language,
        int channels,
        boolean karaoke,
        boolean isDefault
) {}

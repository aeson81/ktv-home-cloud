package com.homektv.media;

import java.util.List;

/**
 * FFprobe 探测结果（P0.7）。
 *
 * @param durationMs   时长（毫秒）
 * @param audioTracks  音轨数
 * @param subtitleTracks 字幕流数
 * @param hasVideo     是否含视频流
 * @param resolution   视频分辨率（如 1920x1080），无视频为 null
 * @param audioStreams 各音频流元数据（按音频相对序号 0-based 排序），供伴奏轨判定使用
 */
public record MediaProbe(
        long durationMs,
        int audioTracks,
        int subtitleTracks,
        boolean hasVideo,
        String resolution,
        List<AudioStreamInfo> audioStreams,
        String videoCodec,
        String audioCodec,
        String title,
        String artist,
        String language
) {
    /** 兼容旧调用（测试/无音轨明细场景）：音频流列表默认空。 */
    public MediaProbe(long durationMs, int audioTracks, int subtitleTracks, boolean hasVideo, String resolution) {
        this(durationMs, audioTracks, subtitleTracks, hasVideo, resolution, List.of(), null, null, null, null, null);
    }

    /** 兼容旧调用：保留仅传音频流列表的构造器。 */
    public MediaProbe(long durationMs, int audioTracks, int subtitleTracks, boolean hasVideo, String resolution,
                      List<AudioStreamInfo> audioStreams) {
        this(durationMs, audioTracks, subtitleTracks, hasVideo, resolution, audioStreams, null, null, null, null, null);
    }

    /** Compatibility constructor retaining codec metadata without container tags. */
    public MediaProbe(long durationMs, int audioTracks, int subtitleTracks, boolean hasVideo, String resolution,
                      List<AudioStreamInfo> audioStreams, String videoCodec, String audioCodec) {
        this(durationMs, audioTracks, subtitleTracks, hasVideo, resolution, audioStreams, videoCodec, audioCodec, null, null, null);
    }
}

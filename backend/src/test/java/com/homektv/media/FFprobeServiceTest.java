package com.homektv.media;

import com.homektv.config.AppProperties;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

/**
 * FFprobe JSON 解析单测（不依赖外部进程）。
 */
class FFprobeServiceTest {

    private final FFprobeService service = new FFprobeService(new AppProperties());

    @Test
    void parseKtvVideoWithDualAudio() {
        // 模拟 A 类 KTV 视频：1 视频流 + 2 音轨（原唱/伴奏）+ 1 字幕流
        String json = """
            {
              "streams": [
                {"codec_type": "video", "width": 1920, "height": 1080, "disposition": {"attached_pic": 0}},
                {"codec_type": "audio"},
                {"codec_type": "audio"},
                {"codec_type": "subtitle"}
              ],
              "format": {"duration": "269.500000"}
            }
            """;
        MediaProbe p = service.parse(json);
        assertThat(p.hasVideo()).isTrue();
        assertThat(p.audioTracks()).isEqualTo(2);
        assertThat(p.subtitleTracks()).isEqualTo(1);
        assertThat(p.resolution()).isEqualTo("1920x1080");
        assertThat(p.durationMs()).isEqualTo(269500L);
    }

    @Test
    void parseAudioOnly() {
        // 纯音频（B 类 AUDIO）：仅 1 音轨，无视频
        String json = """
            {
              "streams": [
                {"codec_type": "audio"}
              ],
              "format": {"duration": "241.000000"}
            }
            """;
        MediaProbe p = service.parse(json);
        assertThat(p.hasVideo()).isFalse();
        assertThat(p.audioTracks()).isEqualTo(1);
        assertThat(p.resolution()).isNull();
        assertThat(p.durationMs()).isEqualTo(241000L);
    }

    @Test
    void parseAudioStreamMetadata() {
        // 双音轨：track0 原唱、track1 伴奏（karaoke 标志 + 标题），验证元数据采集
        String json = """
            {
              "streams": [
                {"codec_type": "video", "width": 1920, "height": 1080, "disposition": {"attached_pic": 0}},
                {"codec_type": "audio", "channels": 2,
                 "disposition": {"default": 1, "karaoke": 0}, "tags": {"title": "原唱", "language": "zho"}},
                {"codec_type": "audio", "channels": 2,
                 "disposition": {"default": 0, "karaoke": 1}, "tags": {"title": "伴奏"}}
              ],
              "format": {"duration": "269.5"}
            }
            """;
        MediaProbe p = service.parse(json);
        assertThat(p.audioStreams()).hasSize(2);
        AudioStreamInfo a0 = p.audioStreams().get(0);
        assertThat(a0.index()).isEqualTo(0);
        assertThat(a0.title()).isEqualTo("原唱");
        assertThat(a0.language()).isEqualTo("zho");
        assertThat(a0.channels()).isEqualTo(2);
        assertThat(a0.isDefault()).isTrue();
        assertThat(a0.karaoke()).isFalse();
        AudioStreamInfo a1 = p.audioStreams().get(1);
        assertThat(a1.index()).isEqualTo(1);
        assertThat(a1.title()).isEqualTo("伴奏");
        assertThat(a1.karaoke()).isTrue();
    }

    @Test
    void attachedCoverImageNotCountedAsVideo() {
        // MP3 内嵌封面图会有一个 attached_pic 视频流，不应判为有视频
        String json = """
            {
              "streams": [
                {"codec_type": "audio"},
                {"codec_type": "video", "width": 500, "height": 500, "disposition": {"attached_pic": 1}}
              ],
              "format": {"duration": "180.0"}
            }
            """;
        MediaProbe p = service.parse(json);
        assertThat(p.hasVideo()).isFalse();
        assertThat(p.audioTracks()).isEqualTo(1);
        assertThat(p.resolution()).isNull();
    }
}

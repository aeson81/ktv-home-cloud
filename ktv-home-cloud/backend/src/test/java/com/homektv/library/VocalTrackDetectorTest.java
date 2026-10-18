package com.homektv.library;

import com.homektv.media.AudioStreamInfo;
import com.homektv.media.MediaProbe;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 伴奏轨判定单测（第 1 层：音轨元数据）。
 * 约定：vocalTrackIndex = 伴奏轨（无人声）的 0-based 音频相对序号。
 */
class VocalTrackDetectorTest {

    private static MediaProbe probe(AudioStreamInfo... audio) {
        return new MediaProbe(200000, audio.length, 0, true, "1920x1080", List.of(audio));
    }

    private static AudioStreamInfo track(int index, String title) {
        return new AudioStreamInfo(index, title, null, 2, false, false);
    }

    @Test
    void dispositionKaraokeIsHighest() {
        MediaProbe p = probe(
                new AudioStreamInfo(0, "原唱", null, 2, false, true),
                new AudioStreamInfo(1, null, null, 2, true, false)); // karaoke 标志
        VocalTrackDetector.Result r = VocalTrackDetector.detect(p);
        assertThat(r.accompanimentIndex()).isEqualTo(1);
        assertThat(r.confidence()).isEqualTo(VocalTrackDetector.Confidence.HIGH);
    }

    @Test
    void accompanimentTitleChinese() {
        VocalTrackDetector.Result r = VocalTrackDetector.detect(
                probe(track(0, "原唱"), track(1, "伴奏")));
        assertThat(r.accompanimentIndex()).isEqualTo(1);
        assertThat(r.confidence()).isEqualTo(VocalTrackDetector.Confidence.HIGH);
    }

    @Test
    void accompanimentTitleEnglish() {
        VocalTrackDetector.Result r = VocalTrackDetector.detect(
                probe(track(0, "Instrumental"), track(1, "Vocal")));
        assertThat(r.accompanimentIndex()).isEqualTo(0);
        assertThat(r.confidence()).isEqualTo(VocalTrackDetector.Confidence.HIGH);
    }

    @Test
    void offVocalVariants() {
        assertThat(VocalTrackDetector.detect(probe(track(0, "Off Vocal"), track(1, "Lead")))
                .accompanimentIndex()).isEqualTo(0);
        assertThat(VocalTrackDetector.detect(probe(track(0, "Guide"), track(1, "no vocal")))
                .accompanimentIndex()).isEqualTo(1);
    }

    @Test
    void onlyVocalTitledInferOther() {
        // 只标了原唱轨，双轨场景推断另一条为伴奏
        VocalTrackDetector.Result r = VocalTrackDetector.detect(
                probe(track(0, "原唱"), track(1, null)));
        assertThat(r.accompanimentIndex()).isEqualTo(1);
        assertThat(r.confidence()).isEqualTo(VocalTrackDetector.Confidence.MEDIUM);
    }

    @Test
    void noMetadataHintsReturnsLow() {
        VocalTrackDetector.Result r = VocalTrackDetector.detect(
                probe(track(0, null), track(1, null)));
        assertThat(r.accompanimentIndex()).isNull();
        assertThat(r.confidence()).isEqualTo(VocalTrackDetector.Confidence.LOW);
    }

    @Test
    void singleTrackIsNone() {
        VocalTrackDetector.Result r = VocalTrackDetector.detect(
                probe(track(0, "原唱")));
        assertThat(r.accompanimentIndex()).isNull();
        assertThat(r.confidence()).isEqualTo(VocalTrackDetector.Confidence.NONE);
    }

    @Test
    void twoTracksWithoutStreamDetailIsNone() {
        // 有 2 音轨但无明细（旧探测路径 / 空列表）：判不出
        MediaProbe p = new MediaProbe(200000, 2, 0, true, "1920x1080");
        VocalTrackDetector.Result r = VocalTrackDetector.detect(p);
        assertThat(r.accompanimentIndex()).isNull();
        assertThat(r.confidence()).isEqualTo(VocalTrackDetector.Confidence.NONE);
    }
}

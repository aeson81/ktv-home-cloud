package com.homektv.ai;

import com.homektv.domain.Song;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocalClassificationServiceTest {
    @Test
    void keepsLocalIdentityButDoesNotInventUnknownClassification() {
        Song song = new Song();
        song.setTitle("晴天");
        song.setArtist("周杰伦");
        song.setStatus("ok");
        song.setLanguage("未知");
        song.setVocalForm("未知");
        song.setMetadataProvenance("{\"title\":{\"source\":\"filename\"}}");

        AiSongClassification result = new LocalClassificationService().fromSong(song);

        assertThat(result.title()).isEqualTo("晴天");
        assertThat(result.artist()).isEqualTo("周杰伦");
        assertThat(result.language()).isEqualTo("未知");
        assertThat(result.vocalForm()).isEqualTo("未知");
        assertThat(result.languageConfidence()).isZero();
        assertThat(result.vocalFormConfidence()).isZero();
        assertThat(result.evidence()).containsEntry("source", "local_song_fields");
    }
}

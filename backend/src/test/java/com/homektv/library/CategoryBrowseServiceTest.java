package com.homektv.library;

import com.homektv.domain.Song;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryBrowseServiceTest {

    @Test
    void dominantArtistGenderUsesKnownMajority() {
        assertThat(CategoryBrowseService.dominantArtistGender(List.of(
                song("男歌手"), song("男歌手"), song("女歌手"), song("未知"))))
                .isEqualTo("男歌手");
    }

    @Test
    void dominantArtistGenderFallsBackToUnknown() {
        assertThat(CategoryBrowseService.dominantArtistGender(List.of(song("未知"), song(null))))
                .isEqualTo("未知");
    }

    private Song song(String gender) {
        Song song = new Song();
        song.setArtistGender(gender);
        return song;
    }
}

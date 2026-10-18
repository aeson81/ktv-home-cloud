package com.homektv.musicsource;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MusicSourceSearchServiceTest {
    @Test
    void usesConfiguredProvidersWhenCallerDoesNotOverrideThem() {
        Set<MusicProvider> selected = MusicSourceSearchService.selectProviders(Set.of(), Set.of(MusicProvider.QQ));

        selected.retainAll(Set.of(MusicProvider.QQ));

        assertThat(selected).containsExactly(MusicProvider.QQ);
    }
}

package com.homektv.musicsource;

import java.util.Set;

public record MusicSourceConfig(boolean enabled, Set<MusicProvider> providers, int resultLimit,
                                int timeoutSeconds, int searchCacheHours, int concurrencyLimit,
                                int requestIntervalMs, double autoApplyThreshold) {
    public static MusicSourceConfig defaults() {
        return new MusicSourceConfig(false, Set.of(), 20, 5, 6, 1, 1500, 0.95);
    }
}

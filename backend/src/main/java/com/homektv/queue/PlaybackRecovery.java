package com.homektv.queue;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PlaybackRecovery {

    private final PlaybackService playbackService;

    public PlaybackRecovery(PlaybackService playbackService) {
        this.playbackService = playbackService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void recover() {
        playbackService.recoverAfterRestart();
    }
}

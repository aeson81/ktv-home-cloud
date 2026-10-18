package com.homektv.musicsource;

public class MusicSourceException extends RuntimeException {
    private final MusicProvider provider;

    public MusicSourceException(MusicProvider provider, String message) { super(message); this.provider = provider; }
    public MusicSourceException(MusicProvider provider, String message, Throwable cause) { super(message, cause); this.provider = provider; }
    public MusicProvider provider() { return provider; }
}

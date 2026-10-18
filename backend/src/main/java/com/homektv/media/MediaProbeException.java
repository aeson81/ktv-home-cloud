package com.homektv.media;

/**
 * FFprobe 探测异常（P0.7）。
 */
public class MediaProbeException extends RuntimeException {

    public MediaProbeException(String message) {
        super(message);
    }

    public MediaProbeException(String message, Throwable cause) {
        super(message, cause);
    }
}

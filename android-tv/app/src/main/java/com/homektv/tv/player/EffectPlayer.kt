package com.homektv.tv.player

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper
import android.util.Log

/**
 * 音效播放器，使用 AudioTrack 在 TV 端异步播放预生成音效。
 *
 * Effect player that asynchronously plays generated sounds through AudioTrack on the TV.
 */
class EffectPlayer {
    private val main = Handler(Looper.getMainLooper())
    private val activeTracks = mutableSetOf<AudioTrack>()
    private var released = false

    /**
     * 按当前系统音量和静音状态播放指定音效。
     *
     * Plays an effect using the current player volume and mute state.
     */
    fun play(effectId: String, playerVolume: Int, muted: Boolean) {
        if (released || muted || playerVolume <= 0) return
        val sound = EffectSounds.find(effectId) ?: run {
            Log.w(TAG, "unknown effect id=$effectId")
            return
        }
        val track = runCatching { createTrack(sound) }.getOrElse {
            Log.w(TAG, "create effect track failed: ${it.message}")
            return
        }
        val volume = (playerVolume.coerceIn(0, 100) / 100f * MAX_EFFECT_VOLUME)
        track.setVolume(volume)
        synchronized(activeTracks) { activeTracks += track }
        val written = track.write(sound.samples, 0, sound.samples.size, AudioTrack.WRITE_BLOCKING)
        if (written <= 0) {
            releaseTrack(track)
            return
        }
        track.play()
        val durationMs = sound.samples.size * 1_000L / sound.sampleRate
        main.postDelayed({ releaseTrack(track) }, durationMs + RELEASE_PADDING_MS)
    }

    fun release() {
        released = true
        main.removeCallbacksAndMessages(null)
        val tracks = synchronized(activeTracks) {
            activeTracks.toList().also { activeTracks.clear() }
        }
        tracks.forEach { track ->
            runCatching { track.stop() }
            track.release()
        }
    }

    private fun createTrack(sound: EffectSound): AudioTrack = AudioTrack.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .setAudioFormat(
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sound.sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
        )
        .setTransferMode(AudioTrack.MODE_STATIC)
        .setBufferSizeInBytes(sound.samples.size * Short.SIZE_BYTES)
        .build()

    private fun releaseTrack(track: AudioTrack) {
        val removed = synchronized(activeTracks) { activeTracks.remove(track) }
        if (!removed) return
        runCatching { track.stop() }
        track.release()
    }

    companion object {
        private const val TAG = "EffectPlayer"
        private const val MAX_EFFECT_VOLUME = 0.42f
        private const val RELEASE_PADDING_MS = 150L
    }
}

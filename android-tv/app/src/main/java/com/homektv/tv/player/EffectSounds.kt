package com.homektv.tv.player

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/** Generated PCM effect sound. / 生成的 PCM 音效数据。 */
data class EffectSound(
    val sampleRate: Int,
    val samples: ShortArray,
)

/** Procedural sound-effect catalog. / 程序化生成的音效目录。 */
object EffectSounds {
    const val SAMPLE_RATE = 22_050

    private val sounds by lazy {
        mapOf(
            "clap" to noiseBursts(seed = 0x43A9, bursts = 7, durationMs = 900, brightness = 0.68),
            "cheer" to cheer(),
            "boo" to boo(),
            "toast" to toast(),
        )
    }

    val ids: Set<String> get() = sounds.keys

    /**
     * 按 ID 查找预生成音效，未知 ID 返回 null。
     *
     * Looks up a generated effect by ID and returns null for unknown IDs.
     */
    fun find(effectId: String): EffectSound? = sounds[effectId.lowercase()]

    private fun noiseBursts(seed: Int, bursts: Int, durationMs: Int, brightness: Double): EffectSound {
        val samples = buffer(durationMs)
        var random = seed
        repeat(bursts) { burst ->
            val center = ((burst + 0.4) * samples.size / bursts).toInt()
            val width = (SAMPLE_RATE * (0.035 + (burst % 3) * 0.008)).toInt()
            for (offset in -width..width) {
                val index = center + offset
                if (index !in samples.indices) continue
                random = random xor (random shl 13)
                random = random xor (random ushr 17)
                random = random xor (random shl 5)
                val noise = ((random and 0xffff) / 32767.5) - 1.0
                val envelope = exp(-4.5 * kotlin.math.abs(offset).toDouble() / width)
                mix(samples, index, noise * envelope * brightness)
            }
        }
        return EffectSound(SAMPLE_RATE, samples)
    }

    private fun cheer(): EffectSound {
        val samples = buffer(1_350)
        val voices = doubleArrayOf(410.0, 485.0, 565.0, 650.0, 735.0)
        voices.forEachIndexed { voice, baseFrequency ->
            val delay = voice * SAMPLE_RATE / 28
            for (index in delay until samples.size) {
                val time = (index - delay).toDouble() / SAMPLE_RATE
                val rise = (time / 0.12).coerceAtMost(1.0)
                val fade = exp(-time * 1.65)
                val wobble = 1.0 + 0.055 * sin(2.0 * PI * (5.2 + voice * 0.3) * time)
                val tone = sin(2.0 * PI * baseFrequency * wobble * time)
                mix(samples, index, tone * rise * fade * 0.13)
            }
        }
        return EffectSound(SAMPLE_RATE, samples)
    }

    private fun boo(): EffectSound {
        val samples = buffer(1_250)
        val voices = doubleArrayOf(115.0, 132.0, 151.0)
        voices.forEachIndexed { voice, frequency ->
            for (index in samples.indices) {
                val time = index.toDouble() / SAMPLE_RATE
                val attack = (time / 0.18).coerceAtMost(1.0)
                val release = ((1.25 - time) / 0.32).coerceIn(0.0, 1.0)
                val vibrato = 1.0 + 0.025 * sin(2.0 * PI * (3.1 + voice * 0.4) * time)
                val tone = sin(2.0 * PI * frequency * vibrato * time)
                mix(samples, index, tone * attack * release * 0.18)
            }
        }
        return EffectSound(SAMPLE_RATE, samples)
    }

    private fun toast(): EffectSound {
        val samples = buffer(850)
        addBell(samples, startMs = 0, frequency = 1_480.0, gain = 0.42)
        addBell(samples, startMs = 105, frequency = 1_920.0, gain = 0.36)
        addBell(samples, startMs = 175, frequency = 1_660.0, gain = 0.28)
        return EffectSound(SAMPLE_RATE, samples)
    }

    private fun addBell(samples: ShortArray, startMs: Int, frequency: Double, gain: Double) {
        val start = startMs * SAMPLE_RATE / 1_000
        for (index in start until samples.size) {
            val time = (index - start).toDouble() / SAMPLE_RATE
            val envelope = exp(-time * 7.0)
            val tone = sin(2.0 * PI * frequency * time) +
                0.45 * sin(2.0 * PI * frequency * 2.03 * time)
            mix(samples, index, tone * envelope * gain)
        }
    }

    private fun buffer(durationMs: Int) = ShortArray(durationMs * SAMPLE_RATE / 1_000)

    private fun mix(samples: ShortArray, index: Int, value: Double) {
        val mixed = samples[index].toInt() + (value * Short.MAX_VALUE).toInt()
        samples[index] = mixed.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
    }
}

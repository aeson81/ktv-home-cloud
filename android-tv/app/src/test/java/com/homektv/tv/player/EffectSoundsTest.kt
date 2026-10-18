package com.homektv.tv.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EffectSoundsTest {
    @Test
    fun exposesAllRemoteEffectIds() {
        assertEquals(setOf("clap", "cheer", "boo", "toast"), EffectSounds.ids)
        EffectSounds.ids.forEach { assertNotNull(EffectSounds.find(it)) }
        assertNull(EffectSounds.find("unknown"))
    }

    @Test
    fun generatedSoundsHaveSafeUsefulWaveforms() {
        EffectSounds.ids.forEach { id ->
            val sound = EffectSounds.find(id)!!
            val durationMs = sound.samples.size * 1_000L / sound.sampleRate
            assertTrue("$id duration", durationMs in 700L..1_500L)
            assertTrue("$id must contain audio", sound.samples.any { it.toInt() != 0 })
            assertFalse("$id must not be fully clipped", sound.samples.all {
                it == Short.MIN_VALUE || it == Short.MAX_VALUE
            })
        }
    }

    @Test
    fun eachEffectUsesADistinctWaveform() {
        val signatures = EffectSounds.ids.map { id ->
            EffectSounds.find(id)!!.samples.fold(1L) { hash, sample -> hash * 31 + sample }
        }

        assertEquals(signatures.size, signatures.distinct().size)
        assertNotEquals(0L, signatures.first())
    }
}

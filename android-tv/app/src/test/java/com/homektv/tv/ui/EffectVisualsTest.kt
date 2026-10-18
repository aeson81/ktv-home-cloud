package com.homektv.tv.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EffectVisualsTest {
    @Test
    fun mapsEveryRemoteEffectToAVisual() {
        assertEquals(setOf("clap", "cheer", "boo", "toast"), EffectVisuals.ids)
        EffectVisuals.ids.forEach { assertNotNull(EffectVisuals.find(it)) }
    }

    @Test
    fun visualEffectsHaveVisibleDurationAndParticles() {
        EffectVisuals.ids.mapNotNull(EffectVisuals::find).forEach { spec ->
            assertTrue(spec.durationMs in 1_500L..2_500L)
            assertTrue(spec.particleCount >= 30)
            assertTrue(spec.colors.isNotEmpty())
            assertTrue(spec.title.isNotBlank())
        }
    }
}

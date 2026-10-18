package com.homektv.tv.player

import org.junit.Assert.assertEquals
import org.junit.Test

class LyricSweepCalculatorTest {
    private val anchors = listOf(
        LyricSweepCalculator.Anchor(1_000, 0f),
        LyricSweepCalculator.Anchor(2_000, 40f),
        LyricSweepCalculator.Anchor(3_000, 100f),
    )

    @Test fun interpolatesContinuouslyBetweenAnchors() {
        assertEquals(20f, LyricSweepCalculator.offsetAt(1_500, anchors, 100f), 0.001f)
        assertEquals(70f, LyricSweepCalculator.offsetAt(2_500, anchors, 100f), 0.001f)
    }

    @Test fun clampsBeforeAndAfterLine() {
        assertEquals(0f, LyricSweepCalculator.offsetAt(0, anchors, 100f), 0.001f)
        assertEquals(100f, LyricSweepCalculator.offsetAt(4_000, anchors, 100f), 0.001f)
    }

    @Test fun handlesDuplicateAndOutOfRangeAnchors() {
        val irregular = listOf(
            LyricSweepCalculator.Anchor(2_000, 20f),
            LyricSweepCalculator.Anchor(1_000, -5f),
            LyricSweepCalculator.Anchor(2_000, 60f),
            LyricSweepCalculator.Anchor(3_000, 120f),
        )
        assertEquals(30f, LyricSweepCalculator.offsetAt(1_500, irregular, 100f), 0.001f)
        assertEquals(80f, LyricSweepCalculator.offsetAt(2_500, irregular, 100f), 0.001f)
    }

    @Test fun emptyTimelineReturnsZero() {
        assertEquals(0f, LyricSweepCalculator.offsetAt(1_000, emptyList(), 100f), 0.001f)
    }
}

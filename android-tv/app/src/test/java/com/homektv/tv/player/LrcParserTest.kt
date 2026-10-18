package com.homektv.tv.player

import org.junit.Assert.assertEquals
import org.junit.Test

class LrcParserTest {

    @Test
    fun parsesLineLyricsAndSortsByTime() {
        val lines = LrcParser.parse("[00:12.00]后一句\n[00:05.50]先一句")

        assertEquals(listOf(5500L, 12000L), lines.map { it.startMs })
        assertEquals(listOf("先一句", "后一句"), lines.map { it.text })
    }

    @Test
    fun parsesEnhancedWordTiming() {
        val line = LrcParser.parse("[00:10.00]<00:10.00>晴<00:10.30>天").single()

        assertEquals("晴天", line.text)
        assertEquals(listOf(10000L, 10300L), line.words.map { it.startMs })
        assertEquals(listOf("晴", "天"), line.words.map { it.text })
    }

    @Test
    fun expandsMultipleLineTimestamps() {
        val lines = LrcParser.parse("[00:01.00][00:05.00]副歌")

        assertEquals(listOf(1000L, 5000L), lines.map { it.startMs })
        assertEquals(listOf("副歌", "副歌"), lines.map { it.text })
    }
}

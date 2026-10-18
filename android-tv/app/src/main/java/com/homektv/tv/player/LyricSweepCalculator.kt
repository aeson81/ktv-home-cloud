package com.homektv.tv.player

/**
 * 根据歌词时间锚点计算逐字高亮的水平偏移量。
 *
 * Calculates the horizontal reveal offset for word-by-word lyric highlighting.
 */
object LyricSweepCalculator {
    /** A timestamp-to-pixel interpolation anchor. / 时间戳到像素位置的插值锚点。 */
    data class Anchor(val timeMs: Long, val offset: Float)

    /** Interpolates the reveal offset at the requested playback position. / 计算指定播放位置的高亮偏移量。 */
    fun offsetAt(positionMs: Long, anchors: List<Anchor>, totalWidth: Float): Float {
        if (totalWidth <= 0f || anchors.isEmpty()) return 0f
        val normalized = anchors
            .sortedBy { it.timeMs }
            .fold(mutableListOf<Anchor>()) { result, anchor ->
                val clamped = anchor.copy(offset = anchor.offset.coerceIn(0f, totalWidth))
                if (result.lastOrNull()?.timeMs == clamped.timeMs) {
                    result[result.lastIndex] = clamped.copy(offset = maxOf(result.last().offset, clamped.offset))
                } else {
                    result += clamped
                }
                result
            }
        if (positionMs <= normalized.first().timeMs) return normalized.first().offset
        if (positionMs >= normalized.last().timeMs) return normalized.last().offset
        val rightIndex = normalized.indexOfFirst { it.timeMs > positionMs }
        val left = normalized[rightIndex - 1]
        val right = normalized[rightIndex]
        val fraction = (positionMs - left.timeMs).toFloat() / (right.timeMs - left.timeMs).toFloat()
        return left.offset + (right.offset - left.offset) * fraction
    }
}

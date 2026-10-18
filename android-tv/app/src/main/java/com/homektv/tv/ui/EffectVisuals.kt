package com.homektv.tv.ui

/** Supported visual effect categories. / 支持的视觉特效类别。 */
enum class EffectVisualKind { CLAP, CHEER, BOO, TOAST }

/** Visual effect configuration. / 视觉特效配置。 */
data class EffectVisualSpec(
    val id: String,
    val kind: EffectVisualKind,
    val title: String,
    val emoji: String,
    val durationMs: Long,
    val particleCount: Int,
    val colors: List<Long>,
)

/** Catalog and lookup helper for TV visual effects. / TV 视觉特效目录与查询工具。 */
object EffectVisuals {
    private val specs = listOf(
        EffectVisualSpec("clap", EffectVisualKind.CLAP, "掌声响起来！", "👏", 1_900L, 42,
            listOf(0xffffd54f, 0xffffa726, 0xffffffff)),
        EffectVisualSpec("cheer", EffectVisualKind.CHEER, "太棒了！", "🎉", 2_200L, 90,
            listOf(0xffff4d6d, 0xffffd166, 0xff06d6a0, 0xff4cc9f0, 0xffa78bfa)),
        EffectVisualSpec("boo", EffectVisualKind.BOO, "倒彩时间！", "😜", 1_900L, 36,
            listOf(0xffa78bfa, 0xff34d399, 0xffff7aa2)),
        EffectVisualSpec("toast", EffectVisualKind.TOAST, "干杯！", "🍻", 2_100L, 48,
            listOf(0xffffd166, 0xffffa62b, 0xfffff3b0)),
    )

    val ids: Set<String> = specs.mapTo(linkedSetOf()) { it.id }

    /**
     * 按 ID 查找视觉特效配置，未知 ID 返回 null。
     *
     * Looks up a visual effect specification by ID and returns null when absent.
     */
    fun find(effectId: String): EffectVisualSpec? = specs.firstOrNull { it.id == effectId.lowercase() }
}

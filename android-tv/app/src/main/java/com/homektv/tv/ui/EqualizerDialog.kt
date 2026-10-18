package com.homektv.tv.ui

import android.app.Dialog
import android.content.Context
import android.media.audiofx.Equalizer
import android.os.Bundle
import android.view.Window
import android.widget.SeekBar
import android.widget.TextView
import com.homektv.tv.R

/**
 * 五段均衡器对话框（科技风）。
 * 使用 Android Equalizer 音频特效，需传入 ExoPlayer 的 audioSessionId。
 */
class EqualizerDialog(
    context: Context,
    private val audioSessionId: Int,
    private val onPresetApplied: (String) -> Unit = {}
) : Dialog(context) {

    private var equalizer: Equalizer? = null

    // 预设：5段增益 (dB * 100, Android 用毫分贝)
    private val presets = mapOf(
        "标准" to intArrayOf(0, 0, 0, 0, 0),
        "流行" to intArrayOf(-100, 200, 300, 200, -100),
        "摇滚" to intArrayOf(300, 200, -100, 200, 300),
        "爵士" to intArrayOf(200, 100, -100, 200, 300),
        "人声" to intArrayOf(-200, -100, 200, 300, 200),
        "低音" to intArrayOf(500, 300, 100, -100, -200)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_equalizer)

        try {
            equalizer = Equalizer(0, audioSessionId).apply { enabled = true }
        } catch (e: Exception) {
            // 设备不支持均衡器
            dismiss()
            return
        }

        val eq = equalizer ?: return
        val bandCount = eq.numberOfBands.coerceAtMost(5)
        val range = eq.getBandLevelRange() // [min, max] in millibels

        // 绑定5个 SeekBar
        val seekIds = intArrayOf(
            R.id.seekBand0, R.id.seekBand1, R.id.seekBand2,
            R.id.seekBand3, R.id.seekBand4
        )
        val labelIds = intArrayOf(
            R.id.lblBand0, R.id.lblBand1, R.id.lblBand2,
            R.id.lblBand3, R.id.lblBand4
        )

        for (i in 0 until bandCount) {
            val seek = findViewById<SeekBar>(seekIds[i])
            val freq = eq.getCenterFreq(i.toShort()) / 1000 // Hz
            findViewById<TextView>(labelIds[i]).text = formatFreq(freq)

            seek.max = (range[1] - range[0]) / 100
            seek.progress = (eq.getBandLevel(i.toShort()) - range[0]) / 100
            seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                    if (fromUser) {
                        val level = (range[0] + p * 100).toShort()
                        eq.setBandLevel(i.toShort(), level)
                    }
                }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
        }

        // 预设按钮
        val presetContainer = findViewById<android.widget.LinearLayout>(R.id.presetRow)
        presets.keys.forEach { name ->
            val btn = android.widget.Button(context).apply {
                text = name
                setOnClickListener { applyPreset(name) }
            }
            presetContainer.addView(btn)
        }

        findViewById<android.widget.Button>(R.id.btnEqClose).setOnClickListener { dismiss() }
    }

    private fun applyPreset(name: String) {
        val eq = equalizer ?: return
        val gains = presets[name] ?: return
        val bandCount = eq.numberOfBands.coerceAtMost(5)
        for (i in 0 until bandCount) {
            val g = gains.getOrNull(i) ?: 0
            try { eq.setBandLevel(i.toShort(), (g * 10).toShort()) } catch (_: Exception) {}
        }
        onPresetApplied(name)
        // 刷新 UI
        dismiss(); show()
    }

    private fun formatFreq(hz: Int): String {
        return if (hz >= 1000) String.format("%.1fk", hz / 1000f) else "${hz}Hz"
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        try { equalizer?.release() } catch (_: Exception) {}
    }
}

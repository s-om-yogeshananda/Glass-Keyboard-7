package com.glasskb.keyboard

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView

/** সহজ রঙ বাছাইয়ের ডায়ালগ: তৈরি রঙের তালিকা + হিউ / সম্পৃক্তি / উজ্জ্বলতা / স্বচ্ছতা স্লাইডার */
object ColorPicker {

    private val PALETTE = listOf(
        "#FF000000", "#FF1B1B1F", "#FF3C3C42", "#FF8E8E93", "#FFFFFFFF", "#FFF44336",
        "#FFFF5722", "#FFFF9800", "#FFFFC107", "#FFFFEB3B", "#FF8BC34A", "#FF4CAF50",
        "#FF009688", "#FF00BCD4", "#FF03A9F4", "#FF2196F3", "#FF3F51B5", "#FF673AB7",
        "#FF7F5AF0", "#FF9C27B0", "#FFE91E63", "#FFFF5E9C", "#FF795548", "#FF607D8B"
    )

    fun show(ctx: Context, title: String, initial: Int, onPick: (Int) -> Unit) {
        val d = ctx.resources.displayMetrics.density
        fun dp(v: Float): Int = (v * d + 0.5f).toInt()
        val ink = Color.parseColor("#FFF2F4FF")
        val sub = Color.parseColor("#FFA9B0D6")

        val hsv = FloatArray(3)
        Color.colorToHSV(initial, hsv)
        var alpha = Color.alpha(initial)

        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20f), dp(10f), dp(20f), dp(4f))
        }
        val preview = View(ctx)
        val hexTv = TextView(ctx).apply {
            setTextColor(sub)
            textSize = 12f
            gravity = Gravity.CENTER
        }
        root.addView(preview, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54f)))
        root.addView(hexTv, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(4f)
        })

        fun current(): Int = Color.HSVToColor(alpha, hsv)

        fun refresh() {
            val c = current()
            preview.background = GradientDrawable().apply {
                cornerRadius = dp(12f).toFloat()
                setColor(c)
                setStroke(dp(1.5f), Color.parseColor("#66FFFFFF"))
            }
            hexTv.text = String.format("#%08X", c)
        }

        val bars = ArrayList<SeekBar>()
        fun bar(label: String, max: Int, start: Int, onChange: (Int) -> Unit) {
            root.addView(TextView(ctx).apply {
                text = label
                setTextColor(ink)
                textSize = 13f
                setPadding(0, dp(8f), 0, 0)
            })
            val sb = SeekBar(ctx).apply {
                this.max = max
                progress = start
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                        onChange(p)
                        refresh()
                    }

                    override fun onStartTrackingTouch(s: SeekBar?) {}
                    override fun onStopTrackingTouch(s: SeekBar?) {}
                })
            }
            bars.add(sb)
            root.addView(sb, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }

        bar("রঙ (Hue)", 360, hsv[0].toInt().coerceIn(0, 360)) { hsv[0] = it.toFloat() }
        bar("গাঢ়তা (Saturation)", 100, Math.round(hsv[1] * 100f)) { hsv[1] = it / 100f }
        bar("উজ্জ্বলতা (Brightness)", 100, Math.round(hsv[2] * 100f)) { hsv[2] = it / 100f }
        bar("স্বচ্ছতা (Opacity)", 255, alpha) { alpha = it }

        root.addView(TextView(ctx).apply {
            text = "তৈরি রঙ"
            setTextColor(ink)
            textSize = 13f
            setPadding(0, dp(12f), 0, dp(4f))
        })
        val perRow = 8
        var row: LinearLayout? = null
        PALETTE.forEachIndexed { i, hex ->
            if (i % perRow == 0) {
                val nr = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL }
                row = nr
                root.addView(nr, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            }
            val col = Color.parseColor(hex)
            val sw = View(ctx).apply {
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(col)
                    setStroke(dp(1.2f), Color.parseColor("#66FFFFFF"))
                }
                setOnClickListener {
                    Color.colorToHSV(col, hsv)
                    bars[0].progress = hsv[0].toInt().coerceIn(0, 360)
                    bars[1].progress = Math.round(hsv[1] * 100f)
                    bars[2].progress = Math.round(hsv[2] * 100f)
                    refresh()
                }
            }
            row?.addView(sw, LinearLayout.LayoutParams(0, dp(32f), 1f).apply {
                setMargins(dp(3f), dp(3f), dp(3f), dp(3f))
            })
        }
        refresh()

        AlertDialog.Builder(ctx)
            .setTitle(title)
            .setView(root)
            .setNegativeButton("বাতিল", null)
            .setPositiveButton("ঠিক আছে") { _, _ -> onPick(current()) }
            .show()
    }
}

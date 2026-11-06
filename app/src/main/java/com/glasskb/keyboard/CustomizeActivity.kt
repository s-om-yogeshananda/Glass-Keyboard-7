package com.glasskb.keyboard

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/** থিম → কাস্টমাইজ: কীবোর্ডের প্রতিটি অংশের রঙ নিজের পছন্দে বদলানো যায় */
class CustomizeActivity : Activity() {

    private lateinit var prefs: Prefs
    private val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
    private val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT
    private val INK = Color.parseColor("#FFF2F4FF")
    private val SUB = Color.parseColor("#FFA9B0D6")
    private val ACC1 = Color.parseColor("#FF7F5AF0")
    private val ACC2 = Color.parseColor("#FF2CB1BC")

    private var previewSlot: FrameLayout? = null
    private val swatches = HashMap<String, View>()

    private fun dp(v: Float): Int = (v * resources.displayMetrics.density + 0.5f).toInt()

    private fun tv(s: String, size: Float, bold: Boolean = false, color: Int = INK): TextView =
        TextView(this).apply {
            text = s
            textSize = size
            setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }

    private fun glass(radius: Float, fill: String = "#1AFFFFFF", stroke: String = "#33FFFFFF"): GradientDrawable =
        GradientDrawable().apply {
            cornerRadius = dp(radius).toFloat()
            setColor(Color.parseColor(fill))
            setStroke(dp(1.2f), Color.parseColor(stroke))
        }

    private fun accentBg(radius: Float): GradientDrawable =
        GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(ACC1, ACC2)).apply {
            cornerRadius = dp(radius).toFloat()
        }

    private fun refreshPreview() {
        val slot = previewSlot ?: return
        slot.removeAllViews()
        slot.addView(ThemePreview(this, Themes.custom(prefs.sp)), FrameLayout.LayoutParams(MATCH, MATCH))
        for ((key, v) in swatches) {
            v.background = GradientDrawable().apply {
                cornerRadius = dp(10f).toFloat()
                setColor(Themes.customColor(prefs.sp, key))
                setStroke(dp(1.5f), Color.parseColor("#88FFFFFF"))
            }
        }
    }

    private fun useCustom() {
        if (prefs.theme != Themes.customIndex) prefs.theme = Themes.customIndex
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)

        val root = FrameLayout(this).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.parseColor("#FF0B1020"), Color.parseColor("#FF1A1038"), Color.parseColor("#FF081C2A"))
            )
        }
        root.addView(BlobBg(this), FrameLayout.LayoutParams(MATCH, MATCH))

        val main = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(main, FrameLayout.LayoutParams(MATCH, MATCH))

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14f), dp(16f), dp(20f), dp(8f))
        }
        header.addView(tv("←", 24f, true).apply {
            setPadding(dp(8f), 0, dp(14f), 0)
            setOnClickListener { finish() }
        })
        header.addView(tv("🖌️ কাস্টমাইজ", 22f, true), LinearLayout.LayoutParams(0, WRAP, 1f))
        main.addView(header, LinearLayout.LayoutParams(MATCH, WRAP))

        val sv = ScrollView(this).apply { isVerticalScrollBarEnabled = false }
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16f), dp(4f), dp(16f), dp(24f))
        }
        sv.addView(col, ViewGroup.LayoutParams(MATCH, WRAP))
        main.addView(sv, LinearLayout.LayoutParams(MATCH, 0, 1f))

        // প্রিভিউ
        val pv = FrameLayout(this).apply { background = glass(20f) ; setPadding(dp(8f), dp(8f), dp(8f), dp(8f)) }
        val slot = FrameLayout(this)
        previewSlot = slot
        pv.addView(slot, FrameLayout.LayoutParams(MATCH, MATCH))
        col.addView(pv, LinearLayout.LayoutParams(MATCH, dp(190f)).apply { topMargin = dp(8f) })
        col.addView(tv("নিচের যেকোনো অংশে ট্যাপ করে রঙ বদলান। বদলালেই কাস্টম থিম চালু হয়ে যাবে।", 12.5f, false, SUB).apply {
            setPadding(dp(2f), dp(8f), dp(2f), dp(2f))
        })

        // তৈরি থিম থেকে শুরু
        col.addView(tv("🎯 তৈরি থিম থেকে শুরু করুন", 14.5f, true).apply { setPadding(dp(2f), dp(12f), 0, dp(6f)) })
        val chipRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        Themes.all.forEach { t ->
            chipRow.addView(tv(t.name, 13f, true).apply {
                gravity = Gravity.CENTER
                setPadding(dp(14f), dp(9f), dp(14f), dp(9f))
                background = glass(14f)
                setOnClickListener {
                    Themes.copyToCustom(prefs.sp, t)
                    useCustom()
                    refreshPreview()
                }
            }, LinearLayout.LayoutParams(WRAP, WRAP).apply { setMargins(0, 0, dp(6f), 0) })
        }
        val hs = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            addView(chipRow, FrameLayout.LayoutParams(WRAP, WRAP))
        }
        col.addView(hs, LinearLayout.LayoutParams(MATCH, WRAP))

        // রঙের তালিকা
        col.addView(tv("🎨 প্রতিটি অংশের রঙ", 14.5f, true).apply { setPadding(dp(2f), dp(16f), 0, dp(6f)) })
        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = glass(20f)
            setPadding(dp(14f), dp(6f), dp(14f), dp(6f))
        }
        for ((key, label, _) in Themes.customFields) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(9f), 0, dp(9f))
            }
            row.addView(tv(label, 14f), LinearLayout.LayoutParams(0, WRAP, 1f))
            val sw = View(this)
            swatches[key] = sw
            row.addView(sw, LinearLayout.LayoutParams(dp(46f), dp(30f)))
            row.setOnClickListener {
                ColorPicker.show(this, label, Themes.customColor(prefs.sp, key)) { c ->
                    prefs.sp.edit().putInt("c_$key", c).apply()
                    useCustom()
                    refreshPreview()
                }
            }
            list.addView(row, LinearLayout.LayoutParams(MATCH, WRAP))
        }
        col.addView(list, LinearLayout.LayoutParams(MATCH, WRAP))

        col.addView(tv("↩️ ডিফল্ট রঙে ফিরুন", 14.5f, true, Color.WHITE).apply {
            gravity = Gravity.CENTER
            setPadding(dp(14f), dp(13f), dp(14f), dp(13f))
            background = accentBg(14f)
            setOnClickListener {
                Themes.resetCustom(prefs.sp)
                refreshPreview()
                Toast.makeText(this@CustomizeActivity, "কাস্টম রঙ ডিফল্ট করা হয়েছে", Toast.LENGTH_SHORT).show()
            }
        }, LinearLayout.LayoutParams(MATCH, WRAP).apply { topMargin = dp(16f) })

        setContentView(root)
        refreshPreview()
    }
}

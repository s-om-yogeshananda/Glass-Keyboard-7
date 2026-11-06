package com.glasskb.keyboard

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

/** পেছনের নরম রঙিন আলো — গ্লাস ইফেক্টের জন্য */
class BlobBg(ctx: Context) : View(ctx) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(c: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        blob(c, w * 0.12f, h * 0.10f, w * 0.60f, 0x667F5AF0)
        blob(c, w * 0.98f, h * 0.45f, w * 0.65f, 0x552CB1BC)
        blob(c, w * 0.15f, h * 0.92f, w * 0.60f, 0x55FF7EB3)
    }

    private fun blob(c: Canvas, cx: Float, cy: Float, r: Float, color: Int) {
        p.shader = RadialGradient(cx, cy, r, color, color and 0x00FFFFFF, Shader.TileMode.CLAMP)
        c.drawCircle(cx, cy, r, p)
    }
}

/** থিমের ছোট প্রিভিউ — আসল কীবোর্ডের মতো দেখায় */
class ThemePreview(ctx: Context, private val t: KbTheme) : View(ctx) {
    private val bgP = Paint(Paint.ANTI_ALIAS_FLAG)
    private val keyP = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokeP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = resources.displayMetrics.density
    }
    private val rect = RectF()

    override fun onDraw(c: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val d = resources.displayMetrics.density
        bgP.shader = LinearGradient(0f, 0f, w, h, t.bg, null, Shader.TileMode.CLAMP)
        rect.set(0f, 0f, w, h)
        c.drawRoundRect(rect, 14f * d, 14f * d, bgP)

        val pad = 8f * d
        val gap = 3f * d
        // সাজেশন বার
        val sh = h * 0.13f
        keyP.shader = null
        keyP.color = t.keyTop
        for (i in 0..2) {
            val pw = (w - pad * 2 - gap * 2) / 3f
            rect.set(pad + i * (pw + gap), pad, pad + i * (pw + gap) + pw, pad + sh)
            c.drawRoundRect(rect, 4f * d, 4f * d, keyP)
        }
        // কী সারি
        val rows = intArrayOf(7, 6, 7)
        val top0 = pad + sh + gap * 1.5f
        val kh = (h - top0 - pad - gap * 3f) / 4f
        for (r in 0..2) {
            val cols = rows[r]
            val offset = (7 - cols) * 0.5f
            val kw = (w - pad * 2 - gap * 6) / 7f
            for (i in 0 until cols) {
                val x = pad + (i + offset) * (kw + gap)
                val y = top0 + r * (kh + gap)
                rect.set(x, y, x + kw, y + kh)
                keyP.shader = LinearGradient(0f, y, 0f, y + kh, t.keyTop, t.keyBottom, Shader.TileMode.CLAMP)
                c.drawRoundRect(rect, 4f * d, 4f * d, keyP)
                strokeP.color = t.keyStroke
                c.drawRoundRect(rect, 4f * d, 4f * d, strokeP)
            }
        }
        // স্পেস ও এন্টার
        val y = top0 + 3 * (kh + gap)
        val sw = (w - pad * 2) * 0.72f
        rect.set(pad, y, pad + sw, y + kh)
        keyP.shader = LinearGradient(0f, y, 0f, y + kh, t.keyTop, t.keyBottom, Shader.TileMode.CLAMP)
        c.drawRoundRect(rect, 4f * d, 4f * d, keyP)
        strokeP.color = t.keyStroke
        c.drawRoundRect(rect, 4f * d, 4f * d, strokeP)
        rect.set(pad + sw + gap, y, w - pad, y + kh)
        keyP.shader = LinearGradient(0f, y, w, y, t.accentStart, t.accentEnd, Shader.TileMode.CLAMP)
        c.drawRoundRect(rect, 4f * d, 4f * d, keyP)
    }
}

class SettingsActivity : Activity() {

    private lateinit var prefs: Prefs
    private lateinit var clips: ClipStore
    private var enabledTv: TextView? = null
    private var selectedTv: TextView? = null
    private var clipCountTv: TextView? = null
    private var learnedTv: TextView? = null
    private var customPreviewBox: FrameLayout? = null
    private var themeRefresh: (() -> Unit)? = null

    private val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
    private val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT

    private val INK = Color.parseColor("#FFF2F4FF")
    private val SUB = Color.parseColor("#FFA9B0D6")
    private val ACC1 = Color.parseColor("#FF7F5AF0")
    private val ACC2 = Color.parseColor("#FF2CB1BC")

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

    private fun card(parent: LinearLayout, title: String = "", sub: String = ""): LinearLayout {
        val c = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16f), dp(14f), dp(16f), dp(14f))
            background = glass(20f)
            elevation = dp(2f).toFloat()
        }
        parent.addView(c, LinearLayout.LayoutParams(MATCH, WRAP).apply { topMargin = dp(12f) })
        if (title.isNotEmpty()) c.addView(tv(title, 16f, true))
        if (sub.isNotEmpty()) c.addView(tv(sub, 12.5f, false, SUB).apply { setPadding(0, dp(2f), 0, dp(4f)) })
        return c
    }

    private fun primaryBtn(label: String, onClick: () -> Unit): TextView =
        TextView(this).apply {
            text = label
            textSize = 14.5f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(dp(14f), dp(12f), dp(14f), dp(12f))
            background = accentBg(14f)
            setOnClickListener { onClick() }
        }

    private fun softBtn(label: String, onClick: () -> Unit): TextView =
        TextView(this).apply {
            text = label
            textSize = 14f
            setTextColor(INK)
            gravity = Gravity.CENTER
            setPadding(dp(14f), dp(12f), dp(14f), dp(12f))
            background = glass(14f, "#22FFFFFF", "#33FFFFFF")
            setOnClickListener { onClick() }
        }

    private fun btnLp(): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(MATCH, WRAP).apply { topMargin = dp(8f) }

    private fun toggleRow(parent: LinearLayout, title: String, desc: String, initial: Boolean, onChange: (Boolean) -> Unit) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(8f), 0, dp(8f))
        }
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        col.addView(tv(title, 14.5f, true))
        if (desc.isNotEmpty()) col.addView(tv(desc, 12f, false, SUB))
        row.addView(col, LinearLayout.LayoutParams(0, WRAP, 1f))
        val sw = Switch(this).apply {
            isChecked = initial
            setOnCheckedChangeListener { _, c -> onChange(c) }
        }
        row.addView(sw, LinearLayout.LayoutParams(WRAP, WRAP))
        parent.addView(row, LinearLayout.LayoutParams(MATCH, WRAP))
    }

    /** ট্যাপ করলে খোলে/বন্ধ হয় এমন কার্ড */
    private fun expandCard(parent: LinearLayout, title: String, body: String) {
        val c = card(parent)
        val head = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        head.addView(tv(title, 15.5f, true), LinearLayout.LayoutParams(0, WRAP, 1f))
        val arrow = tv("﹀", 15f, true, SUB)
        head.addView(arrow)
        c.addView(head)
        val b = tv(body, 13.5f, false, SUB).apply {
            setPadding(0, dp(8f), 0, 0)
            setLineSpacing(0f, 1.15f)
            visibility = View.GONE
        }
        c.addView(b)
        c.setOnClickListener {
            val open = b.visibility == View.VISIBLE
            b.visibility = if (open) View.GONE else View.VISIBLE
            arrow.text = if (open) "﹀" else "︿"
        }
    }

    // ---- গাইডের জন্য: খোলা-বন্ধ হওয়া বক্স ও রেফারেন্স টেবিল

    private fun expandBox(parent: LinearLayout, title: String): LinearLayout {
        val c = card(parent)
        val head = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        head.addView(tv(title, 15.5f, true), LinearLayout.LayoutParams(0, WRAP, 1f))
        val arrow = tv("﹀", 15f, true, SUB)
        head.addView(arrow)
        c.addView(head)
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            setPadding(0, dp(8f), 0, 0)
        }
        c.addView(body)
        head.setOnClickListener {
            val open = body.visibility == View.VISIBLE
            body.visibility = if (open) View.GONE else View.VISIBLE
            arrow.text = if (open) "﹀" else "︿"
        }
        return body
    }

    private fun bodyText(parent: LinearLayout, text: String) {
        parent.addView(tv(text, 13.5f, false, SUB).apply {
            setLineSpacing(0f, 1.15f)
            setPadding(0, dp(4f), 0, dp(2f))
        })
    }

    private fun subTitle(parent: LinearLayout, text: String) {
        parent.addView(tv(text, 14.5f, true).apply { setPadding(0, dp(12f), 0, dp(4f)) })
    }

    private fun tableCell(text: String, size: Float, bold: Boolean, color: Int): TextView =
        tv(text, size, bold, color).apply {
            gravity = Gravity.CENTER
            setPadding(dp(4f), dp(7f), dp(4f), dp(7f))
        }

    /** দুই জোড়া (বর্ণ | লেখার নিয়ম) করে প্রতি সারিতে — অভ্রর রেফারেন্স চার্টের মতো */
    private fun refTable(parent: LinearLayout, hl: String, hr: String, rows: List<Pair<String, String>>, perRow: Int = 2) {
        val table = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(4f), 0, dp(2f))
        }
        val head = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            background = glass(8f, "#33FFFFFF", "#00FFFFFF")
        }
        for (j in 0 until perRow) {
            head.addView(tableCell(hl, 12.5f, true, INK), LinearLayout.LayoutParams(0, WRAP, 1f))
            head.addView(tableCell(hr, 12.5f, true, INK), LinearLayout.LayoutParams(0, WRAP, 1.15f))
        }
        table.addView(head, LinearLayout.LayoutParams(MATCH, WRAP))
        var i = 0
        var n = 0
        while (i < rows.size) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                if (n % 2 == 0) background = glass(6f, "#14FFFFFF", "#00FFFFFF")
            }
            for (j in 0 until perRow) {
                val p = rows.getOrNull(i + j)
                row.addView(tableCell(p?.first ?: "", 17f, true, INK), LinearLayout.LayoutParams(0, WRAP, 1f))
                row.addView(tableCell(p?.second ?: "", 14f, false, Color.parseColor("#FF8FD3FF")), LinearLayout.LayoutParams(0, WRAP, 1.15f))
            }
            table.addView(row, LinearLayout.LayoutParams(MATCH, WRAP))
            i += perRow
            n++
        }
        parent.addView(table, LinearLayout.LayoutParams(MATCH, WRAP))
    }


    private fun scrollPage(): Pair<ScrollView, LinearLayout> {
        val sv = ScrollView(this).apply {
            isVerticalScrollBarEnabled = false
            visibility = View.GONE
        }
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16f), dp(4f), dp(16f), dp(20f))
        }
        sv.addView(col, ViewGroup.LayoutParams(MATCH, WRAP))
        return Pair(sv, col)
    }

    // ------------------------------------------------------------------ পেজ: হোম

    private fun buildHome(col: LinearLayout) {
        val hero = card(col)
        hero.gravity = Gravity.CENTER_HORIZONTAL
        hero.setPadding(dp(16f), dp(22f), dp(16f), dp(22f))
        hero.addView(TextView(this).apply {
            text = "⌨️"
            textSize = 40f
            gravity = Gravity.CENTER
            background = accentBg(26f)
            setPadding(dp(18f), dp(10f), dp(18f), dp(10f))
        }, LinearLayout.LayoutParams(WRAP, WRAP))
        hero.addView(tv("গ্লাস কীবোর্ড", 26f, true).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(10f), 0, 0)
        })
        hero.addView(tv("অভ্র  •  প্রভাত / জাতীয়  •  हिन्दी  •  English", 14f, false, SUB).apply { gravity = Gravity.CENTER })

        val setup = card(col, "🚀 শুরু করুন", "দুটো ধাপে কীবোর্ড চালু করুন")
        enabledTv = tv("", 14f).apply { setPadding(0, dp(6f), 0, 0) }
        selectedTv = tv("", 14f).apply { setPadding(0, dp(2f), 0, 0) }
        setup.addView(enabledTv)
        setup.addView(selectedTv)
        setup.addView(primaryBtn("১. কীবোর্ড চালু করুন") {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }, btnLp())
        setup.addView(primaryBtn("২. এই কীবোর্ড বেছে নিন") {
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
        }, btnLp())

        val test = card(col, "✍️ টাইপ করে দেখুন")
        val et = EditText(this).apply {
            hint = "এখানে ট্যাপ করে লিখুন…"
            setHintTextColor(Color.parseColor("#99A9B0D6"))
            setTextColor(INK)
            minLines = 3
            gravity = Gravity.TOP
            background = glass(14f, "#22FFFFFF", "#33FFFFFF")
            setPadding(dp(12f), dp(10f), dp(12f), dp(10f))
        }
        test.addView(et, LinearLayout.LayoutParams(MATCH, WRAP).apply { topMargin = dp(8f) })

        val tip = card(col, "💡 টিপ")
        tip.addView(tv(
            "স্পেসবার ডানে বা বামে স্লাইড করলে English → অভ্র → প্রভাত (বা জাতীয়) → হিন্দি ঘুরে ঘুরে বদলায়। কীবোর্ডের ওপরের বাঁ দিকের ⊞ বাটনে ক্লিপবোর্ড, নাম্বার ও সেটিংস আছে।",
            13.5f, false, SUB
        ).apply { setPadding(0, dp(4f), 0, 0) })
    }

    // ------------------------------------------------------------------ পেজ: থিম

    private fun buildThemes(col: LinearLayout) {
        col.addView(tv("🎨 থিম বেছে নিন", 20f, true).apply { setPadding(dp(2f), dp(6f), 0, dp(2f)) })
        col.addView(tv("ট্যাপ করলেই কীবোর্ডে সাথে সাথে বদলে যাবে", 12.5f, false, SUB).apply {
            setPadding(dp(2f), 0, 0, dp(4f))
        })
        val cz = card(col, "🖌️ কাস্টমাইজ", "কীবোর্ডের প্রতিটি অংশের রঙ — ব্যাকগ্রাউন্ড, কী, বিশেষ কী, অ্যাকসেন্ট, লেখা, বর্ডার — নিজের মতো বদলান")
        cz.addView(primaryBtn("🎨 রঙ কাস্টমাইজ করুন") {
            startActivity(Intent(this, CustomizeActivity::class.java))
        }, btnLp())
        val cards = ArrayList<LinearLayout>()
        fun refresh() {
            cards.forEachIndexed { i, c ->
                val sel = i == prefs.theme
                c.background = if (sel) {
                    GradientDrawable().apply {
                        cornerRadius = dp(18f).toFloat()
                        setColor(Color.parseColor("#33FFFFFF"))
                        setStroke(dp(2.5f), ACC1)
                    }
                } else glass(18f)
            }
        }
        var rowLayout: LinearLayout? = null
        (Themes.all + listOf(Themes.custom(prefs.sp))).forEachIndexed { i, t ->
            if (i % 2 == 0) {
                rowLayout = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
                col.addView(rowLayout, LinearLayout.LayoutParams(MATCH, WRAP).apply { topMargin = dp(10f) })
            }
            val item = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(8f), dp(8f), dp(8f), dp(8f))
                setOnClickListener {
                    prefs.theme = i
                    refresh()
                }
            }
            val pvBox = FrameLayout(this)
            pvBox.addView(ThemePreview(this, t), FrameLayout.LayoutParams(MATCH, MATCH))
            if (i == Themes.customIndex) customPreviewBox = pvBox
            item.addView(pvBox, LinearLayout.LayoutParams(MATCH, dp(96f)))
            item.addView(tv(t.name, 13.5f, true).apply {
                gravity = Gravity.CENTER
                setPadding(0, dp(6f), 0, dp(2f))
            })
            cards.add(item)
            rowLayout?.addView(item, LinearLayout.LayoutParams(0, WRAP, 1f).apply {
                setMargins(if (i % 2 == 0) 0 else dp(5f), 0, if (i % 2 == 0) dp(5f) else 0, 0)
            })
        }
        if ((Themes.all.size + 1) % 2 == 1) {
            rowLayout?.addView(View(this), LinearLayout.LayoutParams(0, WRAP, 1f))
        }
        themeRefresh = { refresh() }
        refresh()
    }

    // ------------------------------------------------------------------ পেজ: অপশন

    private fun buildOptions(col: LinearLayout) {
        col.addView(tv("⚙️ অপশন", 20f, true).apply { setPadding(dp(2f), dp(6f), 0, dp(6f)) })

        val groups = ArrayList<LinearLayout>()
        val segLabels = listOf("⌨️ টাইপিং", "🎛️ কন্ট্রোল", "🗂️ ডেটা")
        val segViews = ArrayList<TextView>()
        val seg = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            background = glass(16f, "#1FFFFFFF", "#33FFFFFF")
            setPadding(dp(4f), dp(4f), dp(4f), dp(4f))
        }
        fun select(n: Int) {
            groups.forEachIndexed { i, g -> g.visibility = if (i == n) View.VISIBLE else View.GONE }
            segViews.forEachIndexed { i, v ->
                if (i == n) {
                    v.background = accentBg(12f)
                    v.setTextColor(Color.WHITE)
                } else {
                    v.background = null
                    v.setTextColor(INK)
                }
            }
        }
        segLabels.forEachIndexed { i, l ->
            val v = tv(l, 13.5f, true).apply {
                gravity = Gravity.CENTER
                setPadding(0, dp(10f), 0, dp(10f))
                setOnClickListener { select(i) }
            }
            segViews.add(v)
            seg.addView(v, LinearLayout.LayoutParams(0, WRAP, 1f))
        }
        col.addView(seg, LinearLayout.LayoutParams(MATCH, WRAP))

        // --- টাইপিং
        val g1 = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val c1 = card(g1, "✨ স্মার্ট টাইপিং")
        toggleRow(c1, "শব্দ সাজেশন", "টাইপ করা শব্দ ও পরের শব্দের অনুমান", prefs.suggest) { prefs.suggest = it }
        toggleRow(c1, "আমার টাইপ থেকে শেখা", "নতুন শব্দ শুধু এই ফোনেই জমা থাকে", prefs.learn) { prefs.learn = it }
        val cl = card(g1, "🅱️ ফিক্সড বাংলা লেআউট", "স্লাইড করলে English ও অভ্রর পাশে এই দুটির মধ্যে শুধু যেটি চালু থাকবে সেটি আসবে (এরপর হিন্দি)")
        val kindBtns = ArrayList<TextView>()
        fun refreshKind() {
            kindBtns.forEachIndexed { i, b ->
                val sel = prefs.fixedKind == i + 1
                b.background = if (sel) accentBg(14f) else glass(14f, "#22FFFFFF", "#33FFFFFF")
                b.setTextColor(if (sel) Color.WHITE else INK)
            }
        }
        val kindRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf("প্রভাত", "জাতীয়").forEachIndexed { i, name ->
            val b = tv(name, 15f, true).apply {
                gravity = Gravity.CENTER
                setPadding(0, dp(12f), 0, dp(12f))
                setOnClickListener {
                    prefs.fixedKind = i + 1
                    refreshKind()
                }
            }
            kindBtns.add(b)
            kindRow.addView(b, LinearLayout.LayoutParams(0, WRAP, 1f).apply {
                setMargins(if (i == 0) 0 else dp(4f), 0, if (i == 0) dp(4f) else 0, 0)
            })
        }
        cl.addView(kindRow, LinearLayout.LayoutParams(MATCH, WRAP).apply { topMargin = dp(8f) })
        cl.addView(tv("প্রভাত = ওপরের সারিতে ১২টি কী, শিফটে দ্বিতীয় অক্ষর\nজাতীয় = বর্ণমালার ক্রমে সাজানো (ক খ গ…, স্বর ও কার আলাদা পেজে)", 12f, false, SUB).apply {
            setPadding(0, dp(8f), 0, 0)
        })
        refreshKind()
        val ch = card(g1, "🇮🇳 হিন্দি মোড (हिन्दी)", "চালু থাকলে স্পেসবার স্লাইডে English → অভ্র → প্রভাত/জাতীয় → হিন্দি ঘুরে আসবে")
        val hiBtns = ArrayList<TextView>()
        fun refreshHi() {
            hiBtns.forEachIndexed { i, b ->
                val sel = (i == 0) == prefs.hindiOn
                b.background = if (sel) accentBg(14f) else glass(14f, "#22FFFFFF", "#33FFFFFF")
                b.setTextColor(if (sel) Color.WHITE else INK)
            }
        }
        val hiRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf("✅ চালু", "⛔ বন্ধ").forEachIndexed { i, name ->
            val b = tv(name, 15f, true).apply {
                gravity = Gravity.CENTER
                setPadding(0, dp(12f), 0, dp(12f))
                setOnClickListener {
                    prefs.hindiOn = i == 0
                    refreshHi()
                }
            }
            hiBtns.add(b)
            hiRow.addView(b, LinearLayout.LayoutParams(0, WRAP, 1f).apply {
                setMargins(if (i == 0) 0 else dp(4f), 0, if (i == 0) dp(4f) else 0, 0)
            })
        }
        ch.addView(hiRow, LinearLayout.LayoutParams(MATCH, WRAP).apply { topMargin = dp(8f) })
        toggleRow(ch, "হিন্দি মোডে দেবনাগরী সংখ্যা", "123 এর বদলে १२३ বসবে", prefs.hiDigits) { prefs.hiDigits = it }
        ch.addView(tv("হিন্দি লিখতে রোমান অক্ষরে উচ্চারণ লিখুন, যেমন namaste → नमस्ते। পুরো নিয়ম গাইডে আছে।", 12f, false, SUB).apply {
            setPadding(0, dp(6f), 0, 0)
        })
        refreshHi()
        val c1b = card(g1, "🔢 নাম্বার")
        toggleRow(c1b, "বাংলা মোডে বাংলা সংখ্যা", "123 এর বদলে ১২৩ বসবে", prefs.bnDigits) { prefs.bnDigits = it }
        groups.add(g1)

        // --- কন্ট্রোল
        val g2 = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val c2 = card(g2, "🎛️ আচরণ")
        toggleRow(c2, "ভলিউম বাটন = কার্সর", "− চাপলে কার্সর পিছনে, + চাপলে সামনে", prefs.volCursor) { prefs.volCursor = it }
        toggleRow(c2, "কী পপ-আপ", "কী চাপলে বড় করে অক্ষর দেখায়", prefs.popup) { prefs.popup = it }
        toggleRow(c2, "ভাইব্রেশন", "", prefs.vibrate) { prefs.vibrate = it }
        toggleRow(c2, "কী-এর শব্দ", "", prefs.sound) { prefs.sound = it }
        val c3 = card(g2, "📏 কীবোর্ডের সাইজ")
        val sizeLabel = tv("", 13.5f, false, SUB)
        fun setLabel(scale: Float) {
            sizeLabel.text = "বর্তমান: ${Math.round(scale * 100f)}%  (ছোট ← → বড়)"
        }
        setLabel(prefs.scale)
        c3.addView(sizeLabel)
        val sb = SeekBar(this).apply {
            max = 70
            progress = Math.round((prefs.scale - 0.7f) * 100f).coerceIn(0, 70)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                    setLabel(0.7f + p / 100f)
                }

                override fun onStartTrackingTouch(s: SeekBar?) {}

                override fun onStopTrackingTouch(s: SeekBar?) {
                    val p = s?.progress ?: return
                    prefs.scale = 0.7f + p / 100f
                }
            })
        }
        c3.addView(sb, LinearLayout.LayoutParams(MATCH, WRAP).apply { topMargin = dp(8f) })
        val c4 = card(g2, "🎙️ ভয়েস টাইপিং", "🎤 বাটন চেপে বললেই লেখা হবে")
        c4.addView(primaryBtn("মাইক্রোফোনের অনুমতি দিন") {
            startActivity(Intent(this, VoicePermissionActivity::class.java))
        }, btnLp())
        groups.add(g2)

        // --- ডেটা
        val g3 = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val c5 = card(g3, "📋 ক্লিপবোর্ড ইতিহাস")
        clipCountTv = tv("", 13.5f, false, SUB).apply { setPadding(0, dp(4f), 0, 0) }
        c5.addView(clipCountTv)
        c5.addView(primaryBtn("👁 ইতিহাস দেখুন ও এডিট করুন") {
            startActivity(Intent(this, DataActivity::class.java).putExtra(DataActivity.KIND, DataActivity.CLIP))
        }, btnLp())
        c5.addView(softBtn("ইতিহাস মুছে ফেলুন") {
            clips.clear()
            updateCounts()
            Toast.makeText(this, "ক্লিপবোর্ড ইতিহাস মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
        }, btnLp())
        val c6 = card(g3, "🧠 শেখা শব্দ")
        learnedTv = tv("", 13.5f, false, SUB).apply { setPadding(0, dp(4f), 0, 0) }
        c6.addView(learnedTv)
        c6.addView(primaryBtn("👁 শেখা শব্দ দেখুন ও এডিট করুন") {
            startActivity(Intent(this, DataActivity::class.java).putExtra(DataActivity.KIND, DataActivity.WORDS))
        }, btnLp())
        c6.addView(softBtn("শেখা শব্দ মুছে ফেলুন") {
            UserDict(this).clear()
            updateCounts()
            Toast.makeText(this, "শেখা শব্দ মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
        }, btnLp())
        groups.add(g3)

        for (g in groups) col.addView(g, LinearLayout.LayoutParams(MATCH, WRAP))
        select(0)
    }

    // ------------------------------------------------------------------ পেজ: গাইড

    private fun buildGuide(col: LinearLayout) {
        col.addView(tv("📖 ব্যবহার গাইড", 20f, true).apply { setPadding(dp(2f), dp(6f), 0, dp(2f)) })
        col.addView(tv("যেকোনো অংশে ট্যাপ করলে বিস্তারিত খুলবে", 12.5f, false, SUB).apply { setPadding(dp(2f), 0, 0, dp(2f)) })

        expandCard(col, "↔️ ভাষা বদলানো",
            "• স্পেসবার ডানে বা বামে স্লাইড করুন।\n" +
                "• English → বাংলা (অভ্র) → বাংলা (প্রভাত অথবা জাতীয়) → हिन्दी (হিন্দি) → English — এভাবে ঘুরে ঘুরে বদলায়। অপশন → হিন্দি থেকে হিন্দি বন্ধ করলে সেটা বাদ যায়।\n" +
                "• স্লাইডের সময় ভাষার নাম আঙুলের সাথে সরে যায়, ছাড়লে নতুন নামটা মসৃণভাবে মাঝে এসে বসে এবং কী-গুলো পাশ থেকে স্লাইড করে ঢোকে।\n" +
                "• প্রভাত না জাতীয় আসবে, তা অপশন → টাইপিং থেকে ঠিক করুন (দুটো একসাথে আসে না)।\n" +
                "• স্পেসবারে ◀ ▶ তীর মানে স্লাইড করা যায়।\n" +
                "• পাসওয়ার্ড, ইমেইল, নাম্বারের ঘরে শুধু ইংরেজি চলে।")

        expandCard(col, "🔝 ওপরের সারি (টুলবার)",
            "• বাঁ দিকের ⊞ বাটন চাপলে সাজেশনের জায়গায় তিনটি আইকন আসে: 📋 ক্লিপবোর্ড, 🔢 নাম্বার প্যাড, ⚙ সেটিংস। বাটনটা তখন ✕ হয়ে যায় — আবার চাপলে সাজেশন ফিরে আসে।\n" +
                "• 🎤 ভয়েস বাটন সবসময় ডানে থাকে।\n" +
                "• মাঝের তিন ঘরে শব্দের সাজেশন। ট্যাপ করলেই বসে যায়।")

        val av = expandBox(col, "🔠 অভ্র ফোনেটিক — বাংলা বর্ণ লেখার নিয়ম")
        bodyText(av, "অভ্র কী বোর্ডে বাংলা বর্ণ লেখার নিয়ম। বাঁ পাশে বাংলা বর্ণ, ডান পাশে সেটা লিখতে ইংরেজি অক্ষরে কী চাপতে হবে। বড় হাতের আর ছোট হাতের অক্ষর আলাদা — বড় হাতের অক্ষরের জন্য আগে শিফট ⇧ একবার চাপুন।")
        subTitle(av, "১. স্বরবর্ণ (পূর্ণ রূপ, কার রূপ)")
        refTable(av, "বাংলা বর্ণ", "ইংরেজি বর্ণ", listOf(
            Pair("অ", "o"), Pair("আ, া", "a"), Pair("ই, ি", "i"), Pair("ঈ, ী", "I"),
            Pair("উ, ু", "u"), Pair("ঊ, ূ", "U"), Pair("ঋ, ৃ", "rri"), Pair("এ, ে", "e"),
            Pair("ঐ, ৈ", "OI, oi"), Pair("ও, ো", "O"), Pair("ঔ, ৌ", "OU")
        ))
        subTitle(av, "২. ব্যঞ্জনবর্ণ (ক থেকে ন)")
        refTable(av, "বাংলা বর্ণ", "ইংরেজি বর্ণ", listOf(
            Pair("ক", "k"), Pair("খ", "kh"), Pair("গ", "g"), Pair("ঘ", "gh"), Pair("ঙ", "Ng"),
            Pair("চ", "c"), Pair("ছ", "ch"), Pair("জ", "j"), Pair("ঝ", "jh"), Pair("ঞ", "NG"),
            Pair("ট", "T"), Pair("ঠ", "Th"), Pair("ড", "D"), Pair("ঢ", "Dh"), Pair("ণ", "N"),
            Pair("ত", "t"), Pair("থ", "th"), Pair("দ", "d"), Pair("ধ", "dh"), Pair("ন", "n")
        ))
        subTitle(av, "৩. ব্যঞ্জনবর্ণ (প থেকে ঁ) ও চিহ্ন")
        refTable(av, "বাংলা বর্ণ", "ইংরেজি বর্ণ", listOf(
            Pair("প", "p"), Pair("ফ", "ph, f"), Pair("ব", "b"), Pair("ভ", "bh, v"), Pair("ম", "m"),
            Pair("য", "z"), Pair("র", "r"), Pair("ল", "l"), Pair("শ", "sh, S"), Pair("ষ", "Sh"),
            Pair("স", "s"), Pair("হ", "h"), Pair("ড়", "R"), Pair("ঢ়", "Rh"), Pair("য়", "y"),
            Pair("ৎ", "t``"), Pair("ং", "ng"), Pair("ঃ", ":"), Pair("ঁ", "^ বা qq")
        ))
        subTitle(av, "৪. যুক্তবর্ণ ও ফলা")
        refTable(av, "বাংলা বর্ণ", "ইংরেজি বর্ণ", listOf(
            Pair("ক্ষ", "kkh, kSh"), Pair("হ্ম", "hm"), Pair("ঞ্জ", "NGj, nj"), Pair("ঞ্ছ", "NGch, nch"),
            Pair("ঞ্চ", "NGc, nc"), Pair("ঞ্ঝ", "NGjh"), Pair("ঙ্ক", "nk"), Pair("ঙ্গ", "Ngg"),
            Pair("ণ্ড", "ND"), Pair("ষ্ণ", "ShN"), Pair("জ্ঞ", "gg, jNG"), Pair("ক্স", "x"),
            Pair("ব ফলা", "w"), Pair("য ফলা", "y, Z"), Pair("র ফলা", "(C)r"), Pair("রেফ", "(v)rr(c)")
        ))
        bodyText(av, "(C) = যেকোনো ব্যঞ্জনবর্ণ, (v) = যেকোনো স্বরবর্ণ, (c) = পরের ব্যঞ্জনবর্ণ।\nর ফলা: ব্যঞ্জনের পরে r দিন — kr = ক্র, pr = প্র। রেফ: স্বরের পরে rr আর তারপর ব্যঞ্জন — dorrsho = দর্শ। ব ফলা: kw = ক্ব। য ফলা: ky বা kZ = ক্য।")
        subTitle(av, "৫. জরুরি নিয়ম")
        bodyText(av,
            "• o = অ। ব্যঞ্জনের পরে o দিলে কোনো কার বসে না, তাই kotha = কথা। a = আ/া, তাই kaj = কাজ।\n" +
                "• ii বা ee = ঈ, oo = উ, uu = ঊ।\n" +
                "• যুক্তাক্ষর নিজে নিজেই হয়: kt = ক্ত, ShT = ষ্ট, ndh = ন্ধ।\n" +
                "• ` (ব্যাকটিক) দিলে যুক্ত ভাঙে: k`t = কত। ৎ লিখতে t দিয়ে দু'বার `: t`` = ৎ।\n" +
                "• ^ = ঁ, : = ঃ। শব্দের শেষে সাধারণ কোলন চাইলে আগে ` দিন (ami`: = আমি:)।\n" +
                "• ` ^ : সহজে পেতে কমা (,) কী চেপে ধরুন — পপ-আপে ` ^ : আসে।\n" +
                "• . চাপলে ‘।’ বসে (সংখ্যার পরে ইংরেজি ডট)। চেপে ধরলে ইংরেজি ডট।\n" +
                "• শিফট ⇧ একবার চাপলে পরের অক্ষরটি বড় হাতের হয় (T, D, N, S, R, O, I, U…), তারপর নিজে বন্ধ হয়ে যায়।")
        subTitle(av, "৬. উদাহরণ")
        bodyText(av,
            "ami → আমি · tumi → তুমি · bangla → বাংলা · kotha → কথা · valO → ভালো\n" +
                "caqqd বা ca^d → চাঁদ · bondhu → বন্ধু · prem → প্রেম · bhokti → ভক্তি\n" +
                "songge → সঙ্গে · dorrsho → দর্শ · shikkha → শিক্ষা · biggan → বিজ্ঞান\n" +
                "du:kh → দুঃখ · krriShok → কৃষক · baRi → বাড়ি")

        expandCard(col, "🅿️ প্রভাত লেআউট",
            "কী-র সাজানো:\n" +
                "• ওপরের সারি: দ ূ ী র ট এ ু ি ও প ে ো\n" +
                "• মাঝের সারি: া স ড ত গ হ জ ক ল\n" +
                "• নিচের সারি: য় শ চ আ ব ন ম ৃ ়  (ৃ = ঋ-ফলা; হসন্ত ্ শিফটে বা ৃ চেপে ধরলে)\n\n" +
                "শিফট ⇧ একবার চাপলে পরের একটি অক্ষর শিফটের বসে (ধ ঊ ঈ ড় ঠ ঐ উ ই ঔ ফ ৈ ৌ · অ ষ ঢ থ ঘ ঃ ঝ খ ং · য ঢ় ছ ঋ ভ ণ ঙ ্ ঁ), তারপর শিফট নিজে বন্ধ হয়। বাংলায় ক্যাপস-লক নেই।\n\n" +
                "কী চেপে ধরলে পপ-আপ (আঙুল না তুলে স্লাইড করে বেছে ছাড়ুন):\n" +
                "• র → র-ফলা (্র), রেফ (র্), ড়\n" +
                "• এ → ঐ, ঁ (চন্দ্রবিন্দু)\n" +
                "• ু → উ, ৎ\n" +
                "• ত → ৎ\n" +
                "• য় → য, য-ফলা (্য)\n\n" +
                "শিফট চাপার পরের কী-তে চেপে ধরলে:\n" +
                "• ড় → রেফ (র্), র-ফলা (্র)\n" +
                "• ঐ → ঞ\n" +
                "• ৃ → ্ (হসন্ত)\n" +
                "• উ → ৎ\n" +
                "• য → য-ফলা (্য)\n\n" +
                "বাকি কী-তে চেপে ধরলে কোণে লেখা ছোট অক্ষরটি বসে। র-ফলা/য-ফলা ব্যঞ্জনের পরে বসালে নিজেই যুক্ত হয়ে যায় (যেমন প + ্র = প্র)।")

        expandCard(col, "🇧🇩 জাতীয় লেআউট",
            "বর্ণমালার ক্রমে সাজানো। প্রথম পেজে ব্যঞ্জনবর্ণ; “অ আ” কী চাপলে স্বরবর্ণ ও কার, “ক খ” চাপলে আবার ব্যঞ্জন। কী চেপে ধরলে যুক্তাক্ষর (ক → ক্ষ, ক্ত, ক্র…), ত → ৎ, ড → ড়, ং → ঃ ইত্যাদি পপ-আপে আসে।")

        val hd = expandBox(col, "🇮🇳 हिन्दी / হিন্দি লেখার গাইড")
        bodyText(hd, "কীবোর্ডে হিন্দি চালু করতে স্পেসবার ডানে-বামে স্লাইড করে “हिन्दी” আনুন (অপশন → হিন্দি মোড চালু থাকতে হবে)। হিন্দি লিখতে ইংরেজি অক্ষরে উচ্চারণ লিখুন — দেবনাগরী নিজে নিজেই তৈরি হবে। প্রতিটি কী-এর কোণে সেই কী দিয়ে কোন দেবনাগরী অক্ষর হয় তা ছোট করে দেখা যায়।")
        bodyText(hd, "कीबोर्ड पर हिन्दी चालू करने के लिए स्पेसबार को दाएँ-बाएँ स्लाइड करें और “हिन्दी” चुनें। हिन्दी लिखने के लिए रोमन अक्षरों में उच्चारण लिखिए — देवनागरी अपने-आप बन जाएगी। हर कुंजी के कोने में उससे बनने वाला देवनागरी अक्षर छोटा करके दिखता है।")
        subTitle(hd, "১. স্বর / स्वर (পূর্ণ রূপ, মাত্রা)")
        refTable(hd, "देवनागरी", "রোমান", listOf(
            Pair("अ", "a"), Pair("आ, ा", "aa, A"), Pair("इ, ि", "i"), Pair("ई, ी", "ee, ii, I"),
            Pair("उ, ु", "u"), Pair("ऊ, ू", "oo, uu, U"), Pair("ऋ, ृ", "rri"), Pair("ए, े", "e"),
            Pair("ऐ, ै", "ai, E"), Pair("ओ, ो", "o"), Pair("औ, ौ", "au, ou, O")
        ))
        subTitle(hd, "২. অনুস্বার, চন্দ্রবিন্দু, বিসর্গ / अनुस्वार, चन्द्रबिन्दु, विसर्ग")
        refTable(hd, "देवनागरी", "রোমান", listOf(
            Pair("ं", "M"), Pair("ँ", "^"), Pair("ः", "H বা :"), Pair("् हलन्त", "মেনু থেকে")
        ))
        subTitle(hd, "৩. ব্যঞ্জন / व्यंजन")
        refTable(hd, "देवनागरी", "রোমান", listOf(
            Pair("क", "k"), Pair("ख", "kh"), Pair("ग", "g"), Pair("घ", "gh"), Pair("ङ", "Ng"),
            Pair("च", "c"), Pair("छ", "ch, chh"), Pair("ज", "j"), Pair("झ", "jh"), Pair("ञ", "NG"),
            Pair("ट", "T"), Pair("ठ", "Th"), Pair("ड", "D"), Pair("ढ", "Dh"), Pair("ण", "N"),
            Pair("त", "t"), Pair("थ", "th"), Pair("द", "d"), Pair("ध", "dh"), Pair("न", "n"),
            Pair("प", "p"), Pair("फ", "ph"), Pair("ब", "b"), Pair("भ", "bh"), Pair("म", "m"),
            Pair("य", "y"), Pair("र", "r"), Pair("ल", "l"), Pair("व", "v, w"), Pair("श", "sh"),
            Pair("ष", "Sh, S"), Pair("स", "s"), Pair("ह", "h")
        ))
        subTitle(hd, "৪. নুক্তা অক্ষর / नुक्ता वाले अक्षर")
        refTable(hd, "देवनागरी", "রোমান", listOf(
            Pair("क़", "q"), Pair("ख़", "K"), Pair("ग़", "G"), Pair("ज़", "z, Z"),
            Pair("ड़", "R"), Pair("ढ़", "Rh"), Pair("फ़", "f")
        ))
        subTitle(hd, "৫. যুক্তবর্ণ / संयुक्ताक्षर")
        refTable(hd, "देवनागरी", "রোমান", listOf(
            Pair("क्ष", "ksh, kSh, kkh"), Pair("ज्ञ", "gy, jny, GY"), Pair("त्र", "tr"), Pair("श्र", "shr"),
            Pair("प्र", "pr"), Pair("क्र", "kr"), Pair("द्य", "dy"), Pair("स्त", "st"),
            Pair("ट्र", "Tr"), Pair("द्ध", "ddh"), Pair("त्त", "tt"), Pair("क्स", "x")
        ))
        bodyText(hd, "ব্যঞ্জনের পর ব্যঞ্জন লিখলেই যুক্তবর্ণ নিজে নিজে হয় (হলন্ত বসে): pyaar = प्यार, prem = प्रेम, namaste = नमस्ते। যুক্ত ভাঙতে ` ব্যবহার করুন।\nव्यंजन के बाद व्यंजन लिखने पर संयुक्ताक्षर अपने-आप बनता है (हलन्त लग जाता है)। संयुक्त तोड़ने के लिए ` का प्रयोग करें।")
        subTitle(hd, "৬. সংখ্যা ও চিহ্ন / अंक और चिह्न")
        refTable(hd, "देवनागरी", "কী", listOf(
            Pair("०", "0"), Pair("१", "1"), Pair("२", "2"), Pair("३", "3"), Pair("४", "4"),
            Pair("५", "5"), Pair("६", "6"), Pair("७", "7"), Pair("८", "8"), Pair("९", "9"),
            Pair("।", ". (ডট)"), Pair("ॐ", "মেনু থেকে")
        ))
        bodyText(hd, "• হিন্দি মোডে নাম্বার কী চাপলে দেবনাগরী সংখ্যা (१२३) বসে — অপশন → হিন্দি থেকে বন্ধ করা যায়।\n• কমা (,) কী চেপে ধরলে পপ-আপ আসে: ` (যুক্ত ভাঙা) ্ (হলন্ত) ँ ं ः ॐ।\n• . চাপলে ‘।’ বসে, চেপে ধরলে ইংরেজি ডট।\n• अंक: नंबर कुंजी दबाने पर देवनागरी अंक (१२३) लिखे जाते हैं।\n• कॉमा (,) कुंजी को दबाकर रखने पर विकल्प खुलते हैं: ` ् ँ ं ः ॐ।")
        subTitle(hd, "৭. বিশেষ নিয়ম / विशेष नियम")
        bodyText(hd,
            "• a: ব্যঞ্জনের মাঝখানে a দিলে কোনো মাত্রা বসে না (kal = कल), লম্বা আ-কারের জন্য aa দিন (kaam = काम)। শব্দের একদম শেষে একটি a দিলে ा বসে (mera = मेरा, kya = क्या)।\n" +
                "• হ্রস্ব/দীর্ঘ: i = ि, ee/ii/I = ी; u = ु, oo/uu/U = ू। তাই hindi = हिन्दि নয়, hindee = हिन्दी।\n" +
                "• ङ/ञ আলাদা: Ng, NG। ं এর জন্য M: haiM = हैं, aap = आप।\n" +
                "• ज्ञ এর জন্য gy (gyaan = ज्ञान); ग्य লিখতে ग লিখে হলন্ত মেনু থেকে ् দিয়ে य লিখুন।\n" +
                "• शब्द के अंत में अकेला a लिखने पर ा लगता है (mera = मेरा)। शब्द के बीच में लंबे आ के लिए aa लिखिए (kaam = काम)।")
        subTitle(hd, "৮. উদাহরণ / उदाहरण")
        bodyText(hd,
            "namaste → नमस्ते · bhaarat → भारत · pyaar → प्यार · dhanyavaad → धन्यवाद\n" +
                "hindee → हिन्दी · gyaan → ज्ञान · shree → श्री · kshamaa → क्षमा\n" +
                "vidyaalay → विद्यालय · mera → मेरा · tumhaara → तुम्हारा · kya → क्या\n" +
                "krriShN → कृष्ण · aap → आप · haiM → हैं · aa^kh → आँख\n" +
                "duHkh → दुःख · fauj → फ़ौज · zindagee → ज़िन्दगी · sundar → सुन्दर")

        expandCard(col, "🔤 ইংরেজি",
            "• শিফট একবার = পরের অক্ষর বড় হাতের। দু'বার চাপলে ক্যাপস-লক (CAPS লেখা জ্বলে), আবার চাপলে বন্ধ।\n" +
                "• বাক্যের শুরুতে (যেখানে ঘর চায়) বড় হাতের অক্ষর নিজে থেকেই হয়।\n" +
                "• লং-প্রেস: a s d f g h j k l z → @ # & * - ! ? ( ) \"\n" +
                "• q w e r t y u i o p → ১ ২ ৩ … ০ (ইংরেজিতে 1 2 3 … 0)\n" +
                "• b n m → ' : /")

        expandCard(col, "✂️ কাট / কপি / পেস্ট",
            "• x চেপে ধরুন = কাট ✂️ (সিলেক্ট না থাকলে সব লেখা)\n" +
                "• c চেপে ধরুন = কপি 📋\n" +
                "• v চেপে ধরুন = পেস্ট 📥\n" +
                "• প্রতিবার আলাদা পপ-আপ আসে না — সাজেশনের জায়গাতেই ১.৫ সেকেন্ড লেখাটা দেখায়।\n" +
                "• কপি করা সব লেখা ⊞ → 📋 ক্লিপবোর্ডে জমা থাকে; ট্যাপ করলে বসে, ✕ চাপলে মোছে।\n" +
                "• অপশন → ডেটা থেকে ইতিহাস দেখা ও এডিট করা যায়।")

        expandCard(col, "🔢 নাম্বার প্যাড ও সিম্বল",
            "• কোনো নাম্বার / ফোন / তারিখের ঘরে ক্লিক করলে নিজেই নাম্বার প্যাড খুলে যায় — শুধু নাম্বার আর দরকারি দু-একটা চিহ্ন থাকে।\n" +
                "• সাধারণ ঘরে ⊞ → 🔢 চাপলে নাম্বার ও + − * / প্যাড আসে। ফিরতে ABC চাপুন।\n" +
                "• ?123 চাপলে নাম্বার সারি ও সব সিম্বল; “=\\<” চাপলে আরও সিম্বল। সিম্বল চেপে ধরলে আরও চিহ্ন (₹ € £ ≠ ± ÷ ×)।\n" +
                "• বাংলা মোডে নাম্বার বাংলায় (১২৩) বসে — অপশন থেকে বন্ধ করা যায়।")

        expandCard(col, "😊 ইমোজি",
            "ওপরের আইকন সারি (বাঁ থেকে ডানে):\n" +
                "🔍 সার্চ · 🕘 সাম্প্রতিক · 😀 স্মাইলি ও মানুষ · 🌸 প্রাণী ও প্রকৃতি · 🎂 খাবার · ⚽ খেলা · 🚗 ভ্রমণ · 👑 জিনিসপত্র · ❤ হৃদয় · ▲ বিশেষ চিহ্ন (♡ ★ ✿ ➤) · 🏳 পতাকা\n\n" +
                "• ৮ কলামে সব ইমোজি; ওপর-নিচ স্ক্রল করুন, ডানে-বামে স্লাইড করলে পাশের বিভাগ।\n" +
                "• 🔍 চেপে ইংরেজি বা বাংলায় (অভ্র ধাঁচে) লিখে ইমোজি খুঁজুন; ✕ চেপে ফিরুন।\n" +
                "• হাত/মানুষের ইমোজি চেপে ধরলে স্কিন টোন বেছে নেওয়া যায়।\n" +
                "• নিচে ABC = কীবোর্ডে ফেরা, মাঝের 🙂 = স্মাইলি বিভাগে ফেরা, ⌫ = ডিলিট।")

        expandCard(col, "🎨 থিম ও কাস্টমাইজ",
            "• থিম পেজে যেকোনো থিমে ট্যাপ করলেই কীবোর্ডে বদলে যায়।\n" +
                "• “রঙ কাস্টমাইজ করুন” চাপলে ব্যাকগ্রাউন্ড, সাধারণ কী, বিশেষ কী, অ্যাকসেন্ট (এন্টার/সিলেক্টেড), লেখা, ছোট চিহ্ন ও বর্ডার — সব অংশের রঙ আলাদা করে বদলানো যায়। বদলালেই “কাস্টম” থিম চালু হয়।\n" +
                "• “তৈরি থিম থেকে শুরু করুন” থেকে কোনো থিমের রঙ কপি করে তারপর নিজের মতো বদলান। “ডিফল্ট রঙে ফিরুন” চাপলে রঙ আগের অবস্থায় ফেরে।")

        expandCard(col, "🔡 কী চেপে ধরা ও পপ-আপ",
            "• যেকোনো কী চাপলে ওপরে তীরওয়ালা ছোট বুদবুদে অক্ষরটা ভেসে ওঠে — তীর দেখায় কোন কী চেপেছেন।\n" +
                "• যে কী-তে কোণে ছোট চিহ্ন (যেমন প্রভাতে ব-এর ওপরে ভ) আছে, সেটা চেপে ধরলে সেই চিহ্ন পপ-আপে দেখায়। আঙুল তুললে বসে যায়।\n" +
                "• একাধিক চিহ্ন থাকলে আঙুল না তুলে ডানে-বামে স্লাইড করে বেছে নিন।\n" +
                "• অপশন → কন্ট্রোল থেকে কী পপ-আপ বন্ধ করা যায়।")

        expandCard(col, "🗂️ ডেটা: ক্লিপবোর্ড ও শেখা শব্দ",
            "• অপশন → ডেটা পেজে “ইতিহাস দেখুন ও এডিট করুন” চাপলে কপি করা সব লেখা দেখা যায়। লেখায় ট্যাপ করে বদলান, ✕ চেপে মুছুন, ＋ চেপে নতুন যোগ করুন।\n" +
                "• “শেখা শব্দ দেখুন ও এডিট করুন” চাপলে কীবোর্ডের শেখা সব শব্দ ব্যবহারের সংখ্যাসহ দেখা যায়। বানান ঠিক করা, ভুল শব্দ মোছা বা নিজের শব্দ যোগ করা যায়; ওপরে খুঁজেও দেখা যায়।")

        expandCard(col, "🎤 ভয়েস ও 🔊 ভলিউম কার্সর",
            "• 🎤 চেপে বলুন, লেখা বসে যাবে (ইন্টারনেট ও Google অ্যাপ লাগে; প্রথমবার মাইক্রোফোনের অনুমতি দিন)।\n" +
                "• অপশন → কন্ট্রোল থেকে চালু করলে ভলিউম − চাপলে কার্সর পিছনে, + চাপলে সামনে যায়।")
    }

    // ------------------------------------------------------------------ পেজ: ডেভেলপার

    private fun infoRow(parent: LinearLayout, icon: String, label: String, value: String) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(8f), 0, dp(8f))
        }
        row.addView(TextView(this).apply {
            text = icon
            textSize = 20f
            gravity = Gravity.CENTER
            background = glass(14f, "#22FFFFFF", "#33FFFFFF")
        }, LinearLayout.LayoutParams(dp(44f), dp(44f)))
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12f), 0, 0, 0)
        }
        col.addView(tv(label, 12f, false, SUB))
        col.addView(tv(value, 15f, true))
        row.addView(col, LinearLayout.LayoutParams(0, WRAP, 1f))
        parent.addView(row, LinearLayout.LayoutParams(MATCH, WRAP))
    }

    private fun buildDeveloper(col: LinearLayout) {
        col.addView(tv("👨‍💻 Developer", 20f, true).apply { setPadding(dp(2f), dp(6f), 0, dp(2f)) })
        val me = card(col)
        me.gravity = Gravity.CENTER_HORIZONTAL
        me.setPadding(dp(16f), dp(22f), dp(16f), dp(16f))
        me.addView(tv("S Om Yogeshananda", 21f, true).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(10f), 0, 0)
        })
        me.addView(tv("B.Sc in CSE  •  Teesta University, Rangpur 🎓", 13.5f, false, SUB).apply { gravity = Gravity.CENTER })

        val info = card(col, "📇 Profile")
        infoRow(info, "👤", "Name", "S Om Yogeshananda")
        infoRow(info, "📘", "Degree", "B.Sc in CSE")
        infoRow(info, "🏫", "University", "Teesta University, Rangpur")

        val tech = card(col, "🛠️ Built With")
        tech.addView(tv(
            "• 🟣 Kotlin & Android SDK (InputMethodService)\n" +
                "• 🧱 Gradle & GitHub Actions (auto build)\n" +
                "• 🎙️ Android SpeechRecognizer (voice typing)\n" +
                "• 😊 Android ICU (emoji names & search)\n" +
                "• 🤖 Designed & coded with AI assistance",
            13.5f, false, SUB
        ).apply {
            setPadding(0, dp(6f), 0, 0)
            setLineSpacing(0f, 1.2f)
        })

        val about = card(col, "💙 About")
        about.addView(tv(
            "Glass Keyboard — an effort to make Bangla & English typing easy, beautiful and fast.\nVersion 4.0 ✨",
            13.5f, false, SUB
        ).apply { setPadding(0, dp(4f), 0, 0) })
    }

    // ------------------------------------------------------------------ মূল কাঠামো

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        clips = ClipStore(this)

        val root = FrameLayout(this).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.parseColor("#FF0B1020"), Color.parseColor("#FF1A1038"), Color.parseColor("#FF081C2A"))
            )
        }
        root.addView(BlobBg(this), FrameLayout.LayoutParams(MATCH, MATCH))

        val main = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(main, FrameLayout.LayoutParams(MATCH, MATCH))

        // হেডার
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(20f), dp(16f), dp(20f), dp(8f))
        }
        header.addView(tv("গ্লাস কীবোর্ড", 22f, true), LinearLayout.LayoutParams(0, WRAP, 1f))
        header.addView(tv("✨", 22f))
        main.addView(header, LinearLayout.LayoutParams(MATCH, WRAP))

        // পেজ
        val content = FrameLayout(this)
        main.addView(content, LinearLayout.LayoutParams(MATCH, 0, 1f))
        val pages = ArrayList<ScrollView>()
        val builders = listOf<(LinearLayout) -> Unit>(
            { buildHome(it) }, { buildThemes(it) }, { buildOptions(it) }, { buildGuide(it) }, { buildDeveloper(it) }
        )
        for (b in builders) {
            val (sv, col) = scrollPage()
            b(col)
            content.addView(sv, FrameLayout.LayoutParams(MATCH, MATCH))
            pages.add(sv)
        }

        // নিচের নেভিগেশন (গ্লাস বার)
        val navIcons = listOf("🏠", "🎨", "⚙️", "📖", "👨‍💻")
        val navNames = listOf("হোম", "থিম", "অপশন", "গাইড", "Developer")
        val items = ArrayList<LinearLayout>()
        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            background = glass(26f, "#2EFFFFFF", "#40FFFFFF")
            setPadding(dp(6f), dp(6f), dp(6f), dp(6f))
            elevation = dp(6f).toFloat()
        }
        fun show(n: Int) {
            pages.forEachIndexed { i, p -> p.visibility = if (i == n) View.VISIBLE else View.GONE }
            items.forEachIndexed { i, it ->
                val sel = i == n
                it.background = if (sel) accentBg(20f) else null
                (it.getChildAt(0) as TextView).alpha = if (sel) 1f else 0.75f
                (it.getChildAt(1) as TextView).setTextColor(if (sel) Color.WHITE else SUB)
            }
            if (n == 2) updateCounts()
        }
        for (i in 0..4) {
            val item = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(0, dp(7f), 0, dp(7f))
                setOnClickListener { show(i) }
            }
            item.addView(tv(navIcons[i], 19f).apply { gravity = Gravity.CENTER })
            item.addView(tv(navNames[i], 10.5f, true, SUB).apply { gravity = Gravity.CENTER })
            items.add(item)
            nav.addView(item, LinearLayout.LayoutParams(0, WRAP, 1f))
        }
        main.addView(nav, LinearLayout.LayoutParams(MATCH, WRAP).apply {
            setMargins(dp(14f), dp(4f), dp(14f), dp(12f))
        })
        show(0)
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
        updateCounts()
        customPreviewBox?.let { box ->
            box.removeAllViews()
            box.addView(ThemePreview(this, Themes.custom(prefs.sp)), FrameLayout.LayoutParams(MATCH, MATCH))
        }
        themeRefresh?.invoke()
    }

    private fun updateCounts() {
        clipCountTv?.text = "জমা আছে: ${clips.all().size} টি"
        learnedTv?.text = "শেখা শব্দ: ${UserDict(this).count()} টি"
    }

    private fun updateStatus() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val enabled = imm.enabledInputMethodList.any { it.packageName == packageName }
        val cur = Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD) ?: ""
        val selected = cur.startsWith("$packageName/")
        enabledTv?.text = if (enabled) "✅ কীবোর্ড চালু আছে" else "⚠️ কীবোর্ড এখনো চালু করা হয়নি"
        selectedTv?.text = if (selected) "✅ এটাই এখন আপনার কীবোর্ড" else "⚠️ এটা এখনো ডিফল্ট কীবোর্ড নয়"
    }
}

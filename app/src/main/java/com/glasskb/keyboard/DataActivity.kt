package com.glasskb.keyboard

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/** ক্লিপবোর্ড ইতিহাস ও শেখা শব্দ — দেখা, নতুন যোগ, এডিট ও মোছার পেজ */
class DataActivity : Activity() {

    companion object {
        const val KIND = "kind"
        const val CLIP = "clip"
        const val WORDS = "words"
    }

    private val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
    private val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT
    private val INK = Color.parseColor("#FFF2F4FF")
    private val SUB = Color.parseColor("#FFA9B0D6")
    private val ACC1 = Color.parseColor("#FF7F5AF0")
    private val ACC2 = Color.parseColor("#FF2CB1BC")

    private var kind = CLIP
    private lateinit var clips: ClipStore
    private var listBox: LinearLayout? = null
    private var countTv: TextView? = null
    private var query = ""

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

    private fun pill(label: String, accent: Boolean, onClick: () -> Unit): TextView =
        TextView(this).apply {
            text = label
            textSize = 13.5f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(if (accent) Color.WHITE else INK)
            gravity = Gravity.CENTER
            setPadding(dp(12f), dp(10f), dp(12f), dp(10f))
            background = if (accent) accentBg(14f) else glass(14f, "#22FFFFFF", "#33FFFFFF")
            setOnClickListener { onClick() }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        kind = intent.getStringExtra(KIND) ?: CLIP
        clips = ClipStore(this)

        val root = FrameLayout(this).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.parseColor("#FF0B1020"), Color.parseColor("#FF1A1038"), Color.parseColor("#FF081C2A"))
            )
        }
        root.addView(BlobBg(this), FrameLayout.LayoutParams(MATCH, MATCH))
        val main = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14f), dp(16f), dp(14f), dp(10f))
        }
        root.addView(main, FrameLayout.LayoutParams(MATCH, MATCH))

        // হেডার
        val head = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        head.addView(pill("← ফিরুন", false) { finish() }, LinearLayout.LayoutParams(WRAP, WRAP))
        val titleCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12f), 0, 0, 0)
        }
        titleCol.addView(tv(if (kind == WORDS) "🧠 শেখা শব্দ" else "📋 ক্লিপবোর্ড ইতিহাস", 19f, true))
        val ct = tv("", 12.5f, false, SUB)
        countTv = ct
        titleCol.addView(ct)
        head.addView(titleCol, LinearLayout.LayoutParams(0, WRAP, 1f))
        main.addView(head, LinearLayout.LayoutParams(MATCH, WRAP))

        // অ্যাকশন বাটন
        val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        actions.addView(pill("＋ নতুন যোগ", true) { addNew() },
            LinearLayout.LayoutParams(0, WRAP, 1f).apply { rightMargin = dp(4f) })
        actions.addView(pill("🗑 সব মুছুন", false) { confirmClear() },
            LinearLayout.LayoutParams(0, WRAP, 1f).apply { leftMargin = dp(4f) })
        main.addView(actions, LinearLayout.LayoutParams(MATCH, WRAP).apply { topMargin = dp(12f) })

        if (kind == WORDS) {
            val et = EditText(this).apply {
                hint = "🔍 শব্দ খুঁজুন…"
                setHintTextColor(Color.parseColor("#99A9B0D6"))
                setTextColor(INK)
                inputType = InputType.TYPE_CLASS_TEXT
                maxLines = 1
                background = glass(14f, "#22FFFFFF", "#33FFFFFF")
                setPadding(dp(12f), dp(10f), dp(12f), dp(10f))
                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
                    override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
                    override fun afterTextChanged(s: Editable?) {
                        query = s?.toString()?.trim() ?: ""
                        refresh()
                    }
                })
            }
            main.addView(et, LinearLayout.LayoutParams(MATCH, WRAP).apply { topMargin = dp(10f) })
        }

        val sv = ScrollView(this).apply { isVerticalScrollBarEnabled = false }
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(6f), 0, dp(10f))
        }
        sv.addView(box, ViewGroup.LayoutParams(MATCH, WRAP))
        listBox = box
        main.addView(sv, LinearLayout.LayoutParams(MATCH, 0, 1f).apply { topMargin = dp(6f) })
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    // ------------------------------------------------------------ তালিকা

    private fun refresh() {
        val box = listBox ?: return
        box.removeAllViews()
        if (kind == WORDS) refreshWords(box) else refreshClips(box)
    }

    private fun emptyNote(box: LinearLayout, msg: String) {
        box.addView(tv(msg, 13.5f, false, SUB).apply {
            gravity = Gravity.CENTER
            setPadding(dp(12f), dp(30f), dp(12f), dp(12f))
        }, LinearLayout.LayoutParams(MATCH, WRAP))
    }

    private fun row(box: LinearLayout, main: String, sub: String?, lineCount: Int, onTap: () -> Unit, onDelete: () -> Unit) {
        val r = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12f), dp(8f), dp(4f), dp(8f))
            background = glass(14f)
            setOnClickListener { onTap() }
        }
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        col.addView(tv(main, 15f).apply {
            this.maxLines = lineCount
            ellipsize = TextUtils.TruncateAt.END
        })
        if (sub != null) col.addView(tv(sub, 11.5f, false, SUB))
        r.addView(col, LinearLayout.LayoutParams(0, WRAP, 1f))
        r.addView(tv("✏️", 15f).apply {
            gravity = Gravity.CENTER
            setOnClickListener { onTap() }
        }, LinearLayout.LayoutParams(dp(38f), dp(38f)))
        r.addView(tv("✕", 16f, true, SUB).apply {
            gravity = Gravity.CENTER
            setOnClickListener { onDelete() }
        }, LinearLayout.LayoutParams(dp(38f), dp(38f)))
        box.addView(r, LinearLayout.LayoutParams(MATCH, WRAP).apply { topMargin = dp(5f) })
    }

    private fun refreshClips(box: LinearLayout) {
        val items = clips.all()
        countTv?.text = "মোট ${items.size} টি — লেখায় ট্যাপ করলে এডিট করা যাবে"
        if (items.isEmpty()) {
            emptyNote(box, "এখনো কিছু কপি করা হয়নি।\nকীবোর্ডে c চেপে ধরলে কপি হবে, এখানে জমা থাকবে।\n“＋ নতুন যোগ” দিয়ে হাতেও লিখে রাখতে পারেন।")
            return
        }
        items.forEachIndexed { idx, t ->
            row(box, t, "${t.length} অক্ষর", 3, { editClip(idx, t) }, {
                clips.removeAt(idx)
                refresh()
            })
        }
    }

    private fun refreshWords(box: LinearLayout) {
        val all = UserDict(this).all()
        val q = query.lowercase()
        val shownList = if (q.isEmpty()) all else all.filter { it.first.lowercase().contains(q) }
        countTv?.text = "মোট ${all.size} টি শব্দ — ট্যাপ করলে বানান বদলানো যাবে"
        if (all.isEmpty()) {
            emptyNote(box, "এখনো কোনো শব্দ শেখেনি।\nকীবোর্ডে টাইপ করলে নিজে নিজেই শিখবে।\n“＋ নতুন যোগ” দিয়ে হাতেও শব্দ যোগ করা যায়।")
            return
        }
        if (shownList.isEmpty()) {
            emptyNote(box, "“$query” পাওয়া যায়নি")
            return
        }
        val limit = 250
        for ((w, c) in shownList.take(limit)) {
            row(box, w, "ব্যবহার: $c বার", 1, { editWord(w) }, {
                UserDict(this).removeWord(w)
                refresh()
            })
        }
        if (shownList.size > limit) {
            box.addView(tv("আরও ${shownList.size - limit} টি আছে — ওপরে খুঁজে বের করুন", 12f, false, SUB).apply {
                gravity = Gravity.CENTER
                setPadding(0, dp(12f), 0, dp(4f))
            }, LinearLayout.LayoutParams(MATCH, WRAP))
        }
    }

    // ------------------------------------------------------------ এডিট

    private fun inputDialog(title: String, initial: String, multiline: Boolean, onSave: (String) -> Unit, onDelete: (() -> Unit)?) {
        val et = EditText(this).apply {
            setText(initial)
            setSelection(initial.length)
            if (multiline) {
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                minLines = 3
                maxLines = 8
                gravity = Gravity.TOP
            } else {
                inputType = InputType.TYPE_CLASS_TEXT
                maxLines = 1
            }
        }
        val wrap = FrameLayout(this)
        wrap.setPadding(dp(20f), dp(8f), dp(20f), 0)
        wrap.addView(et, FrameLayout.LayoutParams(MATCH, WRAP))
        val b = AlertDialog.Builder(this)
            .setTitle(title)
            .setView(wrap)
            .setPositiveButton("সংরক্ষণ") { _, _ -> onSave(et.text.toString()) }
            .setNegativeButton("বাতিল", null)
        if (onDelete != null) b.setNeutralButton("মুছুন") { _, _ -> onDelete() }
        b.show()
    }

    private fun editClip(idx: Int, old: String) {
        inputDialog("লেখা এডিট করুন", old, true, { nt ->
            clips.update(idx, nt)
            refresh()
        }, {
            clips.removeAt(idx)
            refresh()
        })
    }

    private fun editWord(old: String) {
        inputDialog("শব্দ এডিট করুন", old, false, { nw ->
            if (!UserDict(this).renameWord(old, nw)) {
                Toast.makeText(this, "শব্দটি ফাঁকা বা একাধিক শব্দ হতে পারবে না", Toast.LENGTH_SHORT).show()
            }
            refresh()
        }, {
            UserDict(this).removeWord(old)
            refresh()
        })
    }

    private fun addNew() {
        if (kind == WORDS) {
            inputDialog("নতুন শব্দ যোগ", "", false, { w ->
                if (!UserDict(this).addWord(w)) {
                    Toast.makeText(this, "একটি শব্দ লিখুন (স্পেস ছাড়া)", Toast.LENGTH_SHORT).show()
                }
                refresh()
            }, null)
        } else {
            inputDialog("নতুন লেখা যোগ", "", true, { t ->
                clips.add(t)
                refresh()
            }, null)
        }
    }

    private fun confirmClear() {
        val what = if (kind == WORDS) "সব শেখা শব্দ" else "পুরো ক্লিপবোর্ড ইতিহাস"
        AlertDialog.Builder(this)
            .setTitle("নিশ্চিত?")
            .setMessage("$what মুছে ফেলা হবে।")
            .setPositiveButton("মুছুন") { _, _ ->
                if (kind == WORDS) UserDict(this).clear() else clips.clear()
                refresh()
            }
            .setNegativeButton("বাতিল", null)
            .show()
    }
}

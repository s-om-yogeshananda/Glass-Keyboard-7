package com.glasskb.keyboard

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.InputType
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ScrollView
import android.widget.TextView
import kotlin.math.min

private const val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
private const val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT

private const val PANEL_KEYS = 0
private const val PANEL_EMOJI = 1
private const val PANEL_CLIP = 2
private const val PANEL_NUM = 3

/** ইংরেজি অক্ষর চেপে ধরলে যা বসবে */
private val LONG_MAP: Map<Char, String> = mapOf(
    'a' to "@", 's' to "#", 'd' to "&", 'f' to "*", 'g' to "-",
    'h' to "!", 'j' to "?", 'k' to "(", 'l' to ")", 'z' to "\"",
    'q' to "1", 'w' to "2", 'e' to "3", 'r' to "4", 't' to "5",
    'y' to "6", 'u' to "7", 'i' to "8", 'o' to "9", 'p' to "0",
    'b' to "'", 'n' to ":", 'm' to "/"
)

/** সিম্বল পেজের কী চেপে ধরলে একাধিক অপশন */
private val SYM_ALT: Map<String, List<String>> = mapOf(
    "1" to listOf("¹", "½", "⅓", "¼"), "2" to listOf("²", "⅔"), "3" to listOf("³", "¾"),
    "4" to listOf("⁴"), "0" to listOf("°", "∅", "⁰"),
    "\$" to listOf("¢", "£", "€", "¥", "₹", "৳"),
    "%" to listOf("‰"), "&" to listOf("§"), "#" to listOf("№"),
    "-" to listOf("_", "–", "—", "•"), "+" to listOf("±"),
    "(" to listOf("[", "{", "<"), ")" to listOf("]", "}", ">"),
    "/" to listOf("÷", "\\", "|"), "*" to listOf("×", "†", "‡", "★"),
    "\"" to listOf("“", "”", "«", "»", "„"), "'" to listOf("‘", "’", "‹", "›"),
    ":" to listOf("…"), "!" to listOf("¡"), "?" to listOf("¿"),
    "=" to listOf("≠", "≈", "≤", "≥"), "<" to listOf("≤", "«"), ">" to listOf("≥", "»"),
    "|" to listOf("¦"), "~" to listOf("≈"), "^" to listOf("ˆ")
)

/** জাতীয় লেআউট (বর্ণমালা ক্রমে): পেজ ১ = ব্যঞ্জন, পেজ ২ = স্বর ও কার */
private val BN_P0: List<String> = listOf(
    "ক", "খ", "গ", "ঘ", "ঙ", "চ", "ছ", "জ", "ঝ", "ঞ",
    "ট", "ঠ", "ড", "ঢ", "ণ", "ত", "থ", "দ", "ধ", "ন",
    "প", "ফ", "ব", "ভ", "ম", "য", "র", "ল"
)
private val BN_P1: List<String> = listOf(
    "অ", "আ", "ই", "ঈ", "উ", "ঊ", "ঋ", "এ", "ঐ", "ও",
    "া", "ি", "ী", "ু", "ূ", "ৃ", "ে", "ৈ", "ো", "ৌ",
    "শ", "ষ", "স", "হ", "য়", "ং", "্", "ঁ"
)

/**
 * প্রভাত (Probhat) লেআউট — ওপরের সারিতে ১২টি, মাঝে ৯টি, নিচে ৯টি কী।
 * শিফট চাপলে (বা কী চেপে ধরলে) দ্বিতীয় অক্ষর বসে।
 */
private val PB_NORMAL: List<String> = listOf(
    "দ", "ূ", "ী", "র", "ট", "এ", "ু", "ি", "ও", "প", "ে", "ো",
    "া", "স", "ড", "ত", "গ", "হ", "জ", "ক", "ল",
    "য়", "শ", "চ", "আ", "ব", "ন", "ম", "ৃ", "়"
)
private val PB_SHIFT: List<String> = listOf(
    "ধ", "ঊ", "ঈ", "ড়", "ঠ", "ঐ", "উ", "ই", "ঔ", "ফ", "ৈ", "ৌ",
    "অ", "ষ", "ঢ", "থ", "ঘ", "ঃ", "ঝ", "খ", "ং",
    "য", "ঢ়", "ছ", "ঋ", "ভ", "ণ", "ঙ", "্", "ঁ"
)

/** প্রভাত (সাধারণ অবস্থা): কী চেপে ধরলে যা যা পাওয়া যাবে — স্লটের ক্রম ধরে */
private val PB_ALT_NORMAL: Map<Int, List<String>> = mapOf(
    3 to listOf("\u09CD\u09B0", "\u09B0\u09CD", "\u09DC"),   // র → র-ফলা, রেফ, ড়
    5 to listOf("\u0990", "\u0981"),                          // এ → ঐ, চন্দ্রবিন্দু
    6 to listOf("\u0989", "\u09CE"),                          // ু → উ, ৎ
    15 to listOf("\u09CE"),                                   // ত → ৎ
    21 to listOf("\u09AF", "\u09CD\u09AF")                    // য় → য, য-ফলা
)

/** প্রভাত (শিফট চাপার পর): কী চেপে ধরলে যা যা পাওয়া যাবে */
private val PB_ALT_SHIFT: Map<Int, List<String>> = mapOf(
    3 to listOf("\u09B0\u09CD", "\u09CD\u09B0"),            // ড় → রেফ, র-ফলা
    5 to listOf("\u099E"),                                    // ঐ → ঞ
    6 to listOf("\u09CE"),                                    // উ → ৎ
    21 to listOf("\u09CD\u09AF")                             // য → য-ফলা
)

/** জাতীয় লেআউটে কী চেপে ধরলে যুক্তাক্ষর ও অন্যান্য */
private val BN_ALT: Map<String, List<String>> = mapOf(
    "ক" to listOf("ক্ষ", "ক্ক", "ক্ত", "ক্র", "ক্ট"),
    "গ" to listOf("গ্ন", "গ্র", "গ্গ", "গ্ধ"),
    "ঘ" to listOf("ঘ্ন"),
    "ঙ" to listOf("ঙ্ক", "ঙ্গ", "ঙ্খ", "ঙ্ঘ"),
    "চ" to listOf("চ্চ", "চ্ছ"),
    "জ" to listOf("জ্জ", "জ্ঞ", "জ্ব"),
    "ঞ" to listOf("ঞ্চ", "ঞ্জ", "ঞ্ছ"),
    "ট" to listOf("ট্ট", "ট্র"),
    "ড" to listOf("ড়", "ড্ড"),
    "ঢ" to listOf("ঢ়"),
    "ণ" to listOf("ণ্ট", "ণ্ঠ", "ণ্ড", "ণ্ণ", "ণ্ম"),
    "ত" to listOf("ৎ", "ত্ত", "ত্র", "ত্ন", "ত্ম", "ত্ব"),
    "থ" to listOf("থ্র"),
    "দ" to listOf("দ্দ", "দ্ধ", "দ্ব", "দ্র", "দ্ম", "দ্ভ"),
    "ধ" to listOf("ধ্র", "ধ্ব", "ধ্ম"),
    "ন" to listOf("ন্ত", "ন্দ", "ন্ধ", "ন্ন", "ন্স", "ন্ম", "ন্থ", "ন্ট"),
    "প" to listOf("প্র", "প্ত", "প্প", "প্ল", "প্স"),
    "ফ" to listOf("ফ্র", "ফ্ল"),
    "ব" to listOf("ব্ব", "ব্দ", "ব্ধ", "ব্র", "ব্জ"),
    "ভ" to listOf("ভ্র"),
    "ম" to listOf("ম্প", "ম্ব", "ম্ভ", "ম্ম", "ম্র", "ম্ন", "ম্ফ"),
    "য" to listOf("য়"),
    "ল" to listOf("ল্ল", "ল্প", "ল্ক", "ল্ট", "ল্ড", "ল্ব", "ল্ম"),
    "শ" to listOf("শ্র", "শ্চ", "শ্ব", "শ্ন"),
    "ষ" to listOf("ষ্ট", "ষ্ঠ", "ষ্ণ", "ষ্প", "ষ্ম"),
    "স" to listOf("স্ক", "স্ত", "স্থ", "স্ন", "স্প", "স্ম", "স্ট", "স্ফ", "স্ব"),
    "হ" to listOf("হ্ম", "হ্ন", "হ্ল", "হ্ব"),
    "ও" to listOf("ঔ"),
    "অ" to listOf("অ্যা"),
    "ং" to listOf("ঃ")
)

class GlassKeyboardService : InputMethodService(), KeyView.Host {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var prefs: Prefs
    private lateinit var clips: ClipStore
    private lateinit var suggester: Suggester

    private var kbTheme: KbTheme = Themes.all[0]
    private var banglaMode = false
    private var hindiMode = false
    private var forceEnglish = false
    private var sensitiveField = false
    private var suggestOk = true
    private var symbols = false
    private var symPage2 = false
    private var shiftState = 0 // 0 বন্ধ, 1 একবার, 2 ক্যাপস লক
    private var bnPage = 0
    private var panel = PANEL_KEYS
    private val roman = StringBuilder()
    private var kh = 0
    private var pendingFlash: String? = null

    // ইমোজি সার্চ
    private var searchMode = false
    private val searchBuf = StringBuilder()
    private var searchText: TextView? = null
    private var searchResults: LinearLayout? = null

    // ভয়েস
    private var recognizer: SpeechRecognizer? = null
    private var listening = false

    // ভিউ
    private var toolsBar: View? = null
    private var searchBar: View? = null
    private val sugViews = ArrayList<TextView>()
    private val sugCurrent = arrayOfNulls<String>(3)
    private var flashView: TextView? = null
    private var micBtn: View? = null
    private var numBtn: View? = null
    private var toolbarView: View? = null
    private var holderView: FrameLayout? = null
    private var tbHeight = 0
    private var toolsOpen = false
    private var toggleBtn: IconView? = null
    private var stripBox: View? = null
    private var toolsRow: View? = null
    private var keyArea: KeyArea? = null
    private var lettersBox: LinearLayout? = null
    private var bnBox: LinearLayout? = null
    private var pbBox: LinearLayout? = null
    private var sym1Box: LinearLayout? = null
    private var sym2Box: LinearLayout? = null
    private var numBox: LinearLayout? = null
    private var emojiPanel: View? = null
    private var clipPanel: View? = null
    private var clipList: LinearLayout? = null
    private var emojiFill: (() -> Unit)? = null

    // কী তালিকা
    private var collector: ArrayList<KeyView>? = null
    private val keysLetters = ArrayList<KeyView>()
    private val keysBn = ArrayList<KeyView>()
    private val keysPb = ArrayList<KeyView>()
    private val keysSym1 = ArrayList<KeyView>()
    private val keysSym2 = ArrayList<KeyView>()
    private val keysNum = ArrayList<KeyView>()
    private var currentKeys: List<KeyView> = emptyList()
    private val letterKeys = ArrayList<Pair<KeyView, Char>>()
    private val digitKeys = ArrayList<Pair<KeyView, Char>>()
    private val bnSlots = ArrayList<KeyView>()
    private val pbSlots = ArrayList<KeyView>()
    private var pbShiftKey: KeyView? = null
    private val spaceKeys = ArrayList<KeyView>()
    private val commaKeys = ArrayList<KeyView>()
    private val numOps = ArrayList<KeyView>()
    private val numOpSyms = arrayOf("+", "-", "*", "/")
    private var numCtx = 0 // 0 = হাতে খোলা প্যাড (+ - * /), 1 = সংখ্যার ঘর, 2 = ফোন নাম্বার, 3 = তারিখ/সময়
    private var numDecimal = false
    private var numSigned = false
    private var shiftKey: KeyView? = null
    private var bnToggleKey: KeyView? = null

    // পপ-আপ
    private var preview: PopupWindow? = null
    private var altPopup: PopupWindow? = null
    private val altItems = ArrayList<TextView>()
    private var altOptions: List<String> = emptyList()
    private var altSel = 0
    private var altLeftScreen = 0f
    private var altItemW = 1f

    private val resetFlash = Runnable { flashView?.visibility = View.GONE }
    private val suggestRun = Runnable { refreshSuggestions() }

    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "scale" || key == "theme" || key?.startsWith("c_") == true) {
            handler.post { rebuild() }
        } else if (key == "fixedkind") {
            handler.post {
                if (prefs.bnLayout >= 1) prefs.bnLayout = prefs.fixedKind
                showPanel(panel)
                refreshLabels()
            }
        } else if (key == "bndigits" || key == "hidigits" || key == "suggest") {
            handler.post {
                refreshLabels()
                scheduleSuggest()
            }
        } else if (key == "hindion") {
            handler.post {
                if (!prefs.hindiOn && hindiMode) {
                    flush()
                    hindiMode = false
                    prefs.hindi = false
                }
                refreshLabels()
                scheduleSuggest()
            }
        }
    }

    private val clipListener = object : ClipboardManager.OnPrimaryClipChangedListener {
        override fun onPrimaryClipChanged() {
            try {
                if (sensitiveField) return
                val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = cm.primaryClip ?: return
                if (clip.itemCount == 0) return
                val extras = clip.description.extras
                if (extras != null && extras.getBoolean("android.content.extra.IS_SENSITIVE", false)) return
                val t = clip.getItemAt(0).coerceToText(this@GlassKeyboardService)?.toString() ?: return
                clips.add(t)
            } catch (e: Exception) {
            }
        }
    }

    // ---------------------------------------------------------------- জীবনচক্র

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this)
        clips = ClipStore(this)
        suggester = Suggester(UserDict(this))
        hindiMode = prefs.hindi && prefs.hindiOn
        banglaMode = prefs.bangla && !hindiMode
        prefs.sp.registerOnSharedPreferenceChangeListener(prefListener)
        (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).addPrimaryClipChangedListener(clipListener)
    }

    override fun onDestroy() {
        try {
            stopVoice()
            suggester.user.save()
            prefs.sp.unregisterOnSharedPreferenceChangeListener(prefListener)
            (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).removePrimaryClipChangedListener(clipListener)
        } catch (e: Exception) {
        }
        super.onDestroy()
    }

    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        roman.setLength(0)
        val inputType = info?.inputType ?: 0
        val cls = inputType and InputType.TYPE_MASK_CLASS
        val variation = inputType and InputType.TYPE_MASK_VARIATION
        val isText = cls == InputType.TYPE_CLASS_TEXT
        val pwd = isText && (variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
        val emailUri = isText && (variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS ||
                variation == InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS ||
                variation == InputType.TYPE_TEXT_VARIATION_URI)
        val numPwd = cls == InputType.TYPE_CLASS_NUMBER && variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD
        sensitiveField = pwd || numPwd
        suggestOk = isText && !pwd && !emailUri
        forceEnglish = (inputType != 0 && !isText) || pwd || emailUri
        // নাম্বার / ফোন / তারিখ-সময়ের ঘরে ক্লিক করলে নিজে থেকেই নাম্বার প্যাড খুলবে
        numCtx = when (cls) {
            InputType.TYPE_CLASS_NUMBER -> 1
            InputType.TYPE_CLASS_PHONE -> 2
            InputType.TYPE_CLASS_DATETIME -> 3
            else -> 0
        }
        numDecimal = cls == InputType.TYPE_CLASS_NUMBER && (inputType and InputType.TYPE_NUMBER_FLAG_DECIMAL) != 0
        numSigned = cls == InputType.TYPE_CLASS_NUMBER && (inputType and InputType.TYPE_NUMBER_FLAG_SIGNED) != 0
        symbols = false
        symPage2 = false
        shiftState = 0
        bnPage = 0
        hindiMode = prefs.hindi && prefs.hindiOn
        banglaMode = prefs.bangla && !hindiMode
        if (prefs.bnLayout >= 1 && prefs.bnLayout != prefs.fixedKind) prefs.bnLayout = prefs.fixedKind
        suggester.user.reload()
        if (searchMode) endSearchUi()
        setToolsOpen(false)
        showPanel(if (numCtx != 0) PANEL_NUM else PANEL_KEYS)
        refreshLabels()
        autoCaps()
        scheduleSuggest()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        roman.setLength(0)
        hidePreview()
        hideAlt()
        stopVoice()
        suggester.user.save()
        super.onFinishInputView(finishingInput)
    }

    override fun onWindowHidden() {
        hidePreview()
        hideAlt()
        super.onWindowHidden()
    }

    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int, newSelStart: Int, newSelEnd: Int,
        candidatesStart: Int, candidatesEnd: Int
    ) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
        if (roman.isNotEmpty() &&
            (candidatesStart == -1 || newSelStart != candidatesEnd || newSelEnd != candidatesEnd)
        ) {
            roman.setLength(0)
            currentInputConnection?.finishComposingText()
        }
        scheduleSuggest()
    }

    // ভলিউম বাটন = কার্সর (সেটিংসে চালু করলে)
    private fun isVolKey(code: Int) = code == KeyEvent.KEYCODE_VOLUME_UP || code == KeyEvent.KEYCODE_VOLUME_DOWN

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (prefs.volCursor && isInputViewShown && isVolKey(keyCode)) {
            flush()
            sendDownUpKeyEvents(if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) KeyEvent.KEYCODE_DPAD_RIGHT else KeyEvent.KEYCODE_DPAD_LEFT)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (prefs.volCursor && isInputViewShown && isVolKey(keyCode)) return true
        return super.onKeyUp(keyCode, event)
    }

    // ---------------------------------------------------------------- সাইজ ও সহায়ক

    private fun dp(v: Float): Int = (v * resources.displayMetrics.density + 0.5f).toInt()

    private fun bn(): Boolean = banglaMode && !hindiMode && !forceEnglish

    private fun hi(): Boolean = hindiMode && !forceEnglish

    /** রোমান বাফার থেকে বানিয়ে লেখা হয় এমন মোড: অভ্র ও হিন্দি */
    private fun phonetic(): Boolean = hi() || (bn() && prefs.bnLayout == 0)

    private fun convertRoman(s: String): String =
        if (hi()) HindiPhonetic.convert(s) else AvroPhonetic.convert(s)

    /** বর্তমান মোডের সংখ্যা: বাংলায় ১২৩, হিন্দিতে १२३, বাকিতে 123 */
    private fun localDigit(d: Char): Char = when {
        bn() && prefs.bnDigits -> '\u09E6' + (d - '0')
        hi() && prefs.hiDigits -> '\u0966' + (d - '0')
        else -> d
    }

    private fun localizeAlt(s: String): String =
        if (s.length == 1 && s[0] in '0'..'9') localDigit(s[0]).toString() else s

    private fun bnFixed(): Boolean = bn() && prefs.bnLayout >= 1 && !searchMode

    private fun computeKeyHeight(): Int {
        val dm = resources.displayMetrics
        val land = dm.widthPixels > dm.heightPixels
        val base = if (land) 40f else 52f
        var h = dp(base) * prefs.scale
        val cap = dm.heightPixels * (if (land) 0.60f else 0.42f) / 4.9f
        h = min(h, cap)
        return maxOf(h.toInt(), dp(28f))
    }

    private fun rebuild() {
        try {
            setInputView(onCreateInputView())
            refreshLabels()
            scheduleSuggest()
        } catch (e: Exception) {
        }
    }

    /** ০ = English, ১ = বাংলা (অভ্র), ২ = বাংলা (প্রভাত অথবা জাতীয়, সেটিংসে যেটি চালু), ৩ = হিন্দি (সেটিংসে চালু থাকলে) — স্পেসবার স্লাইডে ঘুরে ঘুরে আসে */
    private fun langIndex(): Int = when {
        hindiMode -> 3
        !banglaMode -> 0
        prefs.bnLayout == 0 -> 1
        else -> 2
    }

    private fun langCount(): Int = if (prefs.hindiOn) 4 else 3

    private fun langNameAt(i: Int): String {
        val n = langCount()
        return when (((i % n) + n) % n) {
            0 -> "English"
            1 -> "বাংলা (অভ্র)"
            2 -> if (prefs.fixedKind == 2) "বাংলা (জাতীয়)" else "বাংলা (প্রভাত)"
            else -> "हिन्दी"
        }
    }

    // ---------------------------------------------------------------- UI তৈরি

    override fun onCreateInputView(): View {
        kbTheme = Themes.resolve(prefs.sp, prefs.theme)
        kh = computeKeyHeight()
        letterKeys.clear(); digitKeys.clear(); bnSlots.clear(); pbSlots.clear(); spaceKeys.clear(); commaKeys.clear()
        keysLetters.clear(); keysBn.clear(); keysPb.clear(); keysSym1.clear(); keysSym2.clear(); keysNum.clear()
        sugViews.clear()
        shiftKey = null
        bnToggleKey = null
        pbShiftKey = null
        searchMode = false

        val radius = dp(18f).toFloat()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR, kbTheme.bg).apply {
                cornerRadii = floatArrayOf(radius, radius, radius, radius, 0f, 0f, 0f, 0f)
            }
            setPadding(dp(3f), dp(3f), dp(3f), dp(4f))
        }
        val tbH = (kh * 0.85f).toInt()
        tbHeight = tbH
        toolsOpen = false
        val tb = buildToolbar(tbH)
        toolbarView = tb
        root.addView(tb, LinearLayout.LayoutParams(MATCH, tbH))

        val holder = FrameLayout(this)
        holderView = holder
        val area = KeyArea(this)
        keyArea = area
        area.keysProvider = { currentKeys }

        collector = keysLetters
        lettersBox = buildLetters()
        collector = keysBn
        bnBox = buildBangla()
        collector = keysPb
        pbBox = buildPrabhat()
        collector = keysSym1
        sym1Box = buildSymbols(false)
        collector = keysSym2
        sym2Box = buildSymbols(true)
        collector = keysNum
        numBox = buildNumPad()
        collector = null

        for (b in listOf(lettersBox, bnBox, pbBox, sym1Box, sym2Box, numBox)) {
            area.addView(b, FrameLayout.LayoutParams(MATCH, MATCH))
        }
        holder.addView(area, FrameLayout.LayoutParams(MATCH, MATCH))
        val emoji = buildEmojiPanel()
        val clip = buildClipPanel()
        emojiPanel = emoji
        clipPanel = clip
        holder.addView(emoji, FrameLayout.LayoutParams(MATCH, MATCH))
        holder.addView(clip, FrameLayout.LayoutParams(MATCH, MATCH))
        root.addView(holder, LinearLayout.LayoutParams(MATCH, kh * 4))

        showPanel(if (panel == PANEL_NUM) PANEL_NUM else PANEL_KEYS)
        refreshLabels()
        val pf = pendingFlash
        if (pf != null) {
            pendingFlash = null
            handler.post { flash(pf) }
        }
        return root
    }

    private fun makeKey(): KeyView {
        val k = KeyView(this, kbTheme)
        k.host = this
        collector?.add(k)
        return k
    }

    private fun newRow(): LinearLayout = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }

    private fun rowLp(): LinearLayout.LayoutParams = LinearLayout.LayoutParams(MATCH, 0, 1f)

    private fun addKey(row: LinearLayout, k: View, weight: Float) {
        row.addView(k, LinearLayout.LayoutParams(0, MATCH, weight))
    }

    private fun spacer(row: LinearLayout, weight: Float) {
        row.addView(View(this), LinearLayout.LayoutParams(0, MATCH, weight))
    }

    private fun roundBg(color: Int, r: Float = 10f): GradientDrawable =
        GradientDrawable().apply {
            cornerRadius = dp(r).toFloat()
            setColor(color)
        }

    private fun toolBtn(text: String, action: () -> Unit): TextView {
        return TextView(this).apply {
            this.text = text
            gravity = Gravity.CENTER
            setTextColor(kbTheme.text)
            textSize = 16f
            background = roundBg(kbTheme.keyTop)
            setOnClickListener {
                feedback(this)
                action()
            }
        }
    }

    private fun styleTool(v: View?, on: Boolean) {
        v ?: return
        v.background = if (on) {
            GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(kbTheme.accentStart, kbTheme.accentEnd)
            ).apply { cornerRadius = dp(8f).toFloat() }
        } else {
            roundBg(kbTheme.keyTop, 8f)
        }
    }

    /** টুলবারের আইকন-বাটন */
    private fun iconBtn(kind: IconView.Kind, action: () -> Unit): IconView {
        return IconView(this, kind).apply {
            color = kbTheme.text
            background = roundBg(kbTheme.keyTop, 8f)
            setOnClickListener {
                feedback(this)
                action()
            }
        }
    }

    /** রেফারেন্সের লাল দাগ দেওয়া বাটন: চাপলে 📋 🔢 ⚙ বেরোয়, আবার চাপলে সাজেশন ফেরে (🎤 সবসময় থাকে) */
    private fun setToolsOpen(open: Boolean) {
        toolsOpen = open
        stripBox?.visibility = if (open) View.GONE else View.VISIBLE
        toolsRow?.visibility = if (open) View.VISIBLE else View.GONE
        toggleBtn?.kind = if (open) IconView.Kind.CLOSE else IconView.Kind.TOOLS
        styleTool(toggleBtn, open)
        toggleBtn?.color = if (open) Color.WHITE else kbTheme.text
    }

    private fun buildToolbar(h: Int): View {
        val frame = FrameLayout(this)
        val bw = (h * 0.95f).toInt()
        val ih = h - dp(6f)

        // ---- সাধারণ বার: টুল-বাটন | সাজেশন (বা 📋 🔢 ⚙) | মাইক
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        fun add(v: View, w: Int, weight: Float) {
            val lp = LinearLayout.LayoutParams(w, ih, weight)
            lp.setMargins(dp(2f), dp(3f), dp(2f), dp(3f))
            bar.addView(v, lp)
        }
        val tg = iconBtn(IconView.Kind.TOOLS) { setToolsOpen(!toolsOpen) }
        toggleBtn = tg
        add(tg, bw, 0f)

        val strip = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        for (i in 0..2) {
            val tv = TextView(this).apply {
                gravity = Gravity.CENTER
                setTextColor(kbTheme.text)
                textSize = 15f
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                setPadding(dp(4f), 0, dp(4f), 0)
                setOnClickListener {
                    val s = sugCurrent[i]
                    if (s != null && s.isNotEmpty()) {
                        feedback(this)
                        applySuggestion(s)
                    }
                }
            }
            sugViews.add(tv)
            strip.addView(tv, LinearLayout.LayoutParams(0, MATCH, 1f))
            if (i < 2) {
                val d = View(this)
                d.setBackgroundColor((kbTheme.hint and 0x00FFFFFF) or 0x44000000)
                strip.addView(d, LinearLayout.LayoutParams(dp(1f), MATCH).apply {
                    setMargins(0, dp(9f), 0, dp(9f))
                })
            }
        }
        val stripFrame = FrameLayout(this)
        stripFrame.addView(strip, FrameLayout.LayoutParams(MATCH, MATCH))
        stripBox = strip

        // টুল-বাটন চাপলে সাজেশনের জায়গায় এই তিনটি আসে
        val tr = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            visibility = View.GONE
        }
        fun addTool(v: View) {
            tr.addView(v, LinearLayout.LayoutParams(0, MATCH, 1f).apply {
                setMargins(dp(3f), 0, dp(3f), 0)
            })
        }
        addTool(iconBtn(IconView.Kind.CLIP) {
            setToolsOpen(false)
            showPanel(PANEL_CLIP)
        })
        val nb = iconBtn(IconView.Kind.NUM) {
            setToolsOpen(false)
            toggleNumPad()
        }
        numBtn = nb
        addTool(nb)
        addTool(iconBtn(IconView.Kind.GEAR) {
            setToolsOpen(false)
            openSettings()
        })
        toolsRow = tr
        stripFrame.addView(tr, FrameLayout.LayoutParams(MATCH, MATCH))

        val fv = TextView(this).apply {
            gravity = Gravity.CENTER
            setTextColor(kbTheme.text)
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            background = roundBg(kbTheme.keyTop, 8f)
            visibility = View.GONE
        }
        flashView = fv
        stripFrame.addView(fv, FrameLayout.LayoutParams(MATCH, MATCH))
        add(stripFrame, 0, 1f)

        val mb = iconBtn(IconView.Kind.MIC) { startVoice() }
        micBtn = mb
        add(mb, bw, 0f)
        toolsBar = bar
        frame.addView(bar, FrameLayout.LayoutParams(MATCH, MATCH))

        // ---- ইমোজি সার্চ বার
        val sbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            visibility = View.GONE
        }
        fun addS(v: View, w: Int, weight: Float) {
            val lp = LinearLayout.LayoutParams(w, ih, weight)
            lp.setMargins(dp(2f), dp(3f), dp(2f), dp(3f))
            sbar.addView(v, lp)
        }
        addS(toolBtn("✕") { stopEmojiSearch() }, bw, 0f)
        val st = TextView(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            setTextColor(kbTheme.text)
            textSize = 14f
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.START
            setPadding(dp(10f), 0, dp(10f), 0)
            background = roundBg(kbTheme.keyTop)
        }
        searchText = st
        addS(st, dp(112f), 0f)
        val res = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        searchResults = res
        val hs = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            addView(res, FrameLayout.LayoutParams(WRAP, MATCH))
        }
        addS(hs, 0, 1f)
        searchBar = sbar
        frame.addView(sbar, FrameLayout.LayoutParams(MATCH, MATCH))
        return frame
    }

    private fun buildLetters(): LinearLayout {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        box.addView(charRow("qwertyuiop", 0f), rowLp())
        box.addView(charRow("asdfghjkl", 0.5f), rowLp())

        val r3 = newRow()
        val shift = makeKey().apply {
            icon = KeyView.Icon.SHIFT
            style = 1
            onTap = { toggleShift() }
        }
        shiftKey = shift
        addKey(r3, shift, 1.5f)
        for (ch in "zxcvbnm") addKey(r3, letterKey(ch), 1f)
        addKey(r3, deleteKey(), 1.5f)
        box.addView(r3, rowLp())

        box.addView(bottomRow(false), rowLp())
        return box
    }

    private fun charRow(chars: String, pad: Float): LinearLayout {
        val row = newRow()
        if (pad > 0f) spacer(row, pad)
        for (ch in chars) addKey(row, letterKey(ch), 1f)
        if (pad > 0f) spacer(row, pad)
        return row
    }

    private fun letterKey(ch: Char): KeyView {
        val k = makeKey()
        k.previewable = true
        k.onTap = { typeLetter(ch) }
        val alt = LONG_MAP[ch]
        if (alt != null) {
            k.altHint = alt
            k.altList = listOf(alt)
        }
        when (ch) {
            'c' -> k.onLong = { doCopy() }
            'v' -> k.onLong = { doPaste() }
            'x' -> k.onLong = { doCut() }
        }
        letterKeys.add(k to ch)
        return k
    }

    private fun deleteKey(): KeyView = makeKey().apply {
        icon = KeyView.Icon.DELETE
        style = 1
        repeatable = true
        onTap = { doBackspace() }
    }

    /** প্রভাত লেআউট (রেফারেন্স ছবির মতো) */
    private fun buildPrabhat(): LinearLayout {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        var idx = 0
        val r1 = newRow()
        for (c in 0 until 12) {
            addKey(r1, pbSlotKey(idx), 1f)
            idx++
        }
        box.addView(r1, rowLp())
        val r2 = newRow()
        spacer(r2, 0.6f)
        for (c in 0 until 9) {
            addKey(r2, pbSlotKey(idx), 1.2f)
            idx++
        }
        spacer(r2, 0.6f)
        box.addView(r2, rowLp())
        val r3 = newRow()
        val sh = makeKey().apply {
            icon = KeyView.Icon.SHIFT
            style = 1
            onTap = { toggleShift() }
        }
        pbShiftKey = sh
        addKey(r3, sh, 1.5f)
        for (c in 0 until 9) {
            addKey(r3, pbSlotKey(idx), 1f)
            idx++
        }
        addKey(r3, deleteKey(), 1.5f)
        box.addView(r3, rowLp())
        box.addView(bottomRow(false), rowLp())
        return box
    }

    private fun pbSlotKey(index: Int): KeyView {
        val k = makeKey()
        k.previewable = true
        k.onTap = {
            val list = if (shiftState != 0) PB_SHIFT else PB_NORMAL
            commitBn(list[index])
            consumeShift()
        }
        pbSlots.add(k)
        return k
    }

    private fun refreshPrabhat() {
        val up = shiftState != 0
        for ((i, k) in pbSlots.withIndex()) {
            val cur = (if (up) PB_SHIFT else PB_NORMAL)[i]
            val other = (if (up) PB_NORMAL else PB_SHIFT)[i]
            val extra: List<String>? = (if (up) PB_ALT_SHIFT else PB_ALT_NORMAL)[i]
            k.label = disp(cur)
            k.altList = null
            k.onLong = null
            k.altHint = ""
            val opts: List<String> = extra ?: (if (other != cur) listOf(other) else emptyList())
            if (opts.isNotEmpty()) {
                k.altHint = disp(opts[0])
                k.altList = opts
            }
        }
        pbShiftKey?.active = up
    }

    /** জাতীয় (বর্ণমালা ক্রম) বাংলা লেআউট */
    private fun buildBangla(): LinearLayout {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        var idx = 0
        for (r in 0..1) {
            val row = newRow()
            for (c in 0 until 10) {
                addKey(row, bnSlotKey(idx), 1f)
                idx++
            }
            box.addView(row, rowLp())
        }
        val r3 = newRow()
        val tog = makeKey().apply {
            style = 1
            textScale = 0.7f
            onTap = {
                bnPage = if (bnPage == 0) 1 else 0
                refreshLabels()
            }
        }
        bnToggleKey = tog
        addKey(r3, tog, 1.2f)
        for (c in 0 until 8) {
            addKey(r3, bnSlotKey(idx), 1f)
            idx++
        }
        addKey(r3, deleteKey(), 1.2f)
        box.addView(r3, rowLp())
        box.addView(bottomRow(false), rowLp())
        return box
    }

    private fun bnSlotKey(index: Int): KeyView {
        val k = makeKey()
        k.previewable = true
        k.onTap = {
            val s = (if (bnPage == 0) BN_P0 else BN_P1)[index]
            commitBn(s)
        }
        bnSlots.add(k)
        return k
    }

    private fun isMarkStart(s: String): Boolean {
        if (s.isEmpty()) return false
        val t = Character.getType(s[0])
        return t == Character.NON_SPACING_MARK.toInt() || t == Character.COMBINING_SPACING_MARK.toInt()
    }

    /** কার/ফলা/চিহ্ন আলাদা দেখালে আগে ◌ বসে, যাতে বোঝা যায় */
    private fun disp(s: String): String =
        if (s.isNotEmpty() && (s[0] == '\u09CD' || isMarkStart(s))) "\u25CC" + s else s

    private fun refreshBnSlots() {
        val list = if (bnPage == 0) BN_P0 else BN_P1
        for ((i, k) in bnSlots.withIndex()) {
            val s = list[i]
            k.label = if (isMarkStart(s)) "\u25CC" + s else s
            val alts = BN_ALT[s]
            k.altList = null
            k.onLong = null
            k.altHint = ""
            if (alts != null) {
                k.altHint = alts[0]
                k.altList = alts
            }
        }
        bnToggleKey?.label = if (bnPage == 0) "অ আ" else "ক খ"
    }

    private fun buildSymbols(second: Boolean): LinearLayout {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        if (!second) {
            box.addView(numberRow(), rowLp())
            box.addView(symRow(listOf("@", "#", "\$", "%", "&", "-", "+", "(", ")", "/"), null, false), rowLp())
            box.addView(symRow(listOf("*", "\"", "'", ":", ";", "!", "?"), "=\\<", true), rowLp())
        } else {
            box.addView(symRow(listOf("~", "`", "|", "•", "√", "π", "÷", "×", "¶", "৳"), null, false), rowLp())
            box.addView(symRow(listOf("£", "¢", "€", "¥", "^", "°", "=", "{", "}", "\\"), null, false), rowLp())
            box.addView(symRow(listOf("[", "]", "<", ">", "©", "®", "™"), "?123", true), rowLp())
        }
        box.addView(bottomRow(true), rowLp())
        return box
    }

    private fun numberRow(): LinearLayout {
        val row = newRow()
        for (d in "1234567890") {
            val k = makeKey()
            k.previewable = true
            k.onTap = { typeDigit(d) }
            val alts = SYM_ALT[d.toString()]
            if (alts != null) k.altList = alts
            k.onLong = null
            digitKeys.add(k to d)
            addKey(row, k, 1f)
        }
        return row
    }

    private fun symRow(chars: List<String>, toggle: String?, withDelete: Boolean): LinearLayout {
        val row = newRow()
        if (toggle != null) {
            val t = makeKey().apply {
                label = toggle
                textScale = 0.62f
                style = 1
                onTap = {
                    symPage2 = !symPage2
                    showPanel(PANEL_KEYS)
                }
            }
            addKey(row, t, 1.5f)
        }
        for (ch in chars) {
            val k = makeKey()
            k.previewable = true
            k.label = ch
            k.onTap = { commitChar(ch) }
            val alts = SYM_ALT[ch]
            if (alts != null) {
                k.altHint = alts[0]
                k.altList = alts
            }
            addKey(row, k, 1f)
        }
        if (withDelete) addKey(row, deleteKey(), 1.5f)
        return row
    }

    /** শুধু নাম্বার ও + - * / */
    private fun buildNumPad(): LinearLayout {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        fun digit(d: Char): KeyView {
            val k = makeKey()
            k.previewable = true
            k.onTap = { typeDigit(d) }
            digitKeys.add(k to d)
            return k
        }
        numOps.clear()
        fun op(i: Int): KeyView {
            val k = makeKey()
            k.label = numOpSyms[i]
            k.style = 1
            k.textScale = 1.15f
            k.onTap = { if (numOpSyms[i].isNotEmpty()) commitChar(numOpSyms[i]) }
            numOps.add(k)
            return k
        }
        val r1 = newRow()
        for (d in "123") addKey(r1, digit(d), 1f)
        addKey(r1, op(0), 1f)
        addKey(r1, deleteKey(), 1f)
        box.addView(r1, rowLp())
        val r2 = newRow()
        for (d in "456") addKey(r2, digit(d), 1f)
        addKey(r2, op(1), 1f)
        addKey(r2, makeKey().apply {
            label = "ABC"
            textScale = 0.62f
            style = 1
            onTap = { toggleNumPad() }
        }, 1f)
        box.addView(r2, rowLp())
        val r3 = newRow()
        for (d in "789") addKey(r3, digit(d), 1f)
        addKey(r3, op(2), 1f)
        addKey(r3, makeKey().apply {
            icon = KeyView.Icon.ENTER
            style = 2
            onTap = { doEnter() }
        }, 1f)
        box.addView(r3, rowLp())
        val r4 = newRow()
        addKey(r4, digit('0'), 3f)
        addKey(r4, op(3), 2f)
        box.addView(r4, rowLp())
        return box
    }

    private fun bottomRow(forSymbols: Boolean): LinearLayout {
        val row = newRow()
        val sym = makeKey().apply {
            label = if (forSymbols) "ABC" else "?123"
            textScale = 0.62f
            style = 1
            onTap = { toggleSymbols() }
        }
        addKey(row, sym, 1.3f)
        val emo = makeKey().apply {
            label = "😊"
            textScale = 0.85f
            style = 1
            onTap = { showPanel(PANEL_EMOJI) }
        }
        addKey(row, emo, 1.1f)
        val comma = makeKey().apply {
            label = ","
            previewable = true
            onTap = { commitChar(",") }
        }
        commaKeys.add(comma)
        addKey(row, comma, 0.9f)
        val space = makeKey().apply {
            textScale = 0.5f
            swipeable = true
            onTap = { doSpace() }
            onSwipe = { dir -> toggleMode(dir) }
        }
        spaceKeys.add(space)
        addKey(row, space, 4.2f)
        val dot = makeKey().apply {
            label = "."
            previewable = true
            onTap = { doPeriod() }
            onLong = { commitChar(".") }
        }
        addKey(row, dot, 0.9f)
        val enter = makeKey().apply {
            icon = KeyView.Icon.ENTER
            style = 2
            onTap = { doEnter() }
        }
        addKey(row, enter, 1.4f)
        return row
    }

    // ---------------------------------------------------------------- ইমোজি প্যানেল

    private fun buildEmojiPanel(): View {
        val p = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
        }
        val widthPx = resources.displayMetrics.widthPixels - dp(8f)
        val cols = 8
        val cell = widthPx / cols

        // ওপরের আইকন সারি (রেফারেন্স ছবির মতো): সার্চ, সাম্প্রতিক, স্মাইলি, ফুল, কেক, বল, গাড়ি, মুকুট, হৃদয়, চিহ্ন, পতাকা
        val kinds = listOf(
            IconView.Kind.SEARCH, IconView.Kind.CLOCK, IconView.Kind.SMILE, IconView.Kind.FLOWER,
            IconView.Kind.CAKE, IconView.Kind.BALL, IconView.Kind.CAR, IconView.Kind.CROWN,
            IconView.Kind.HEART, IconView.Kind.TRIANGLE, IconView.Kind.FLAG
        )
        val tabCats: List<List<Int>> = listOf(
            emptyList(), emptyList(), listOf(0, 1), listOf(2), listOf(3), listOf(4),
            listOf(5), listOf(6), listOf(7), listOf(8), listOf(9)
        )
        val tabViews = ArrayList<IconView>()
        var selected = 2

        val tabsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        p.addView(tabsRow, LinearLayout.LayoutParams(MATCH, (kh * 0.8f).toInt()).apply {
            setMargins(dp(2f), dp(2f), dp(2f), 0)
        })

        val sv = ScrollView(this).apply {
            isVerticalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
        }
        val grid = GridLayout(this)
        grid.columnCount = cols
        sv.addView(grid, FrameLayout.LayoutParams(MATCH, WRAP))
        val pager = SwipeFrame(this)
        pager.addView(sv, FrameLayout.LayoutParams(MATCH, MATCH))
        p.addView(pager, LinearLayout.LayoutParams(MATCH, 0, 1f))

        fun render() {
            grid.removeAllViews()
            val items: List<String>? = if (selected == 1) {
                prefs.recents.split(" ").filter { it.isNotBlank() }
            } else {
                val cs = EmojiData.cats
                if (cs == null) null else tabCats[selected].flatMap { ci ->
                    cs.getOrNull(ci)?.map { it.s } ?: emptyList<String>()
                }
            }
            if (items == null) {
                val tv = TextView(this).apply {
                    text = "লোড হচ্ছে…"
                    setTextColor(kbTheme.hint)
                    textSize = 13f
                    gravity = Gravity.CENTER
                }
                val lp = GridLayout.LayoutParams(GridLayout.spec(0, 1), GridLayout.spec(0, cols))
                lp.width = widthPx
                lp.height = cell * 2
                grid.addView(tv, lp)
                val want = selected
                EmojiData.ensure { if (selected == want) emojiFill?.invoke() }
            } else if (items.isEmpty()) {
                val tv = TextView(this).apply {
                    text = if (selected == 1) "এখনো কিছু ব্যবহার করা হয়নি" else "এই ফোনে এই বিভাগের ইমোজি নেই"
                    setTextColor(kbTheme.hint)
                    textSize = 13f
                    gravity = Gravity.CENTER
                }
                val lp = GridLayout.LayoutParams(GridLayout.spec(0, 1), GridLayout.spec(0, cols))
                lp.width = widthPx
                lp.height = cell * 2
                grid.addView(tv, lp)
            } else {
                for (e in items) {
                    val tv = TextView(this).apply {
                        text = e
                        setTextSize(TypedValue.COMPLEX_UNIT_PX, cell * 0.56f)
                        gravity = Gravity.CENTER
                        setOnClickListener {
                            feedback(this)
                            insertEmoji(e)
                        }
                        setOnLongClickListener {
                            val v = EmojiData.toneVariants(e)
                            if (v.isNotEmpty()) {
                                showToneMenu(this, v)
                                true
                            } else false
                        }
                    }
                    val lp = GridLayout.LayoutParams()
                    lp.width = cell
                    lp.height = cell
                    grid.addView(tv, lp)
                }
            }
            sv.scrollTo(0, 0)
            tabViews.forEachIndexed { idx, t ->
                val on = idx == selected
                t.color = if (on) kbTheme.text else kbTheme.hint
                t.background = if (on) roundBg(kbTheme.keyTop, 8f) else null
            }
        }

        fun select(i: Int, dir: Int) {
            if (dir == 0) {
                selected = i
                render()
            } else {
                pager.slideOut(dir) {
                    selected = i
                    render()
                    pager.slideIn(dir)
                }
            }
        }
        pager.onSwipe = { dir ->
            val n = selected + dir
            if (n in 1 until kinds.size) {
                select(n, dir)
            } else {
                sv.animate().translationX(0f).alpha(1f).setDuration(120).start()
            }
        }
        emojiFill = { render() }

        kinds.forEachIndexed { idx, kd ->
            val t = IconView(this, kd).apply {
                color = kbTheme.hint
                setOnClickListener {
                    feedback(this)
                    if (idx == 0) {
                        startEmojiSearch()
                    } else if (idx != selected) {
                        select(idx, if (idx > selected) 1 else -1)
                    }
                }
            }
            tabViews.add(t)
            tabsRow.addView(t, LinearLayout.LayoutParams(0, MATCH, 1f).apply {
                setMargins(dp(1f), dp(2f), dp(1f), dp(2f))
            })
        }

        // নিচের সারি: ABC | স্মাইলি | ডিলিট
        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val abc = makeKey().apply {
            label = "ABC"
            textScale = 0.62f
            style = 1
            onTap = { showPanel(PANEL_KEYS) }
        }
        bottom.addView(abc, LinearLayout.LayoutParams((kh * 1.5f).toInt(), MATCH))
        val mid = IconView(this, IconView.Kind.SMILE).apply {
            color = kbTheme.hint
            setOnClickListener {
                feedback(this)
                if (selected != 2) select(2, if (selected < 2) 1 else -1)
            }
        }
        bottom.addView(mid, LinearLayout.LayoutParams(0, MATCH, 1f))
        bottom.addView(deleteKey(), LinearLayout.LayoutParams((kh * 1.4f).toInt(), MATCH))
        p.addView(bottom, LinearLayout.LayoutParams(MATCH, kh))
        return p
    }

    private fun showToneMenu(anchor: View, variants: List<String>) {
        hideAlt()
        try {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                background = roundBg(Color.parseColor("#F0202030"), 14f)
                setPadding(dp(4f), dp(4f), dp(4f), dp(4f))
            }
            for (v in variants) {
                row.addView(TextView(this).apply {
                    text = v
                    textSize = 26f
                    gravity = Gravity.CENTER
                    setOnClickListener {
                        insertEmoji(v)
                        hideAlt()
                    }
                }, LinearLayout.LayoutParams(dp(44f), dp(48f)))
            }
            row.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            val w = row.measuredWidth
            val h = row.measuredHeight
            val pw = PopupWindow(row, w, h)
            pw.isOutsideTouchable = true
            pw.isFocusable = false
            pw.setBackgroundDrawable(roundBg(Color.TRANSPARENT))
            pw.inputMethodMode = PopupWindow.INPUT_METHOD_NOT_NEEDED
            val loc = IntArray(2)
            anchor.getLocationInWindow(loc)
            val rootW = anchor.rootView.width
            var xOff = (anchor.width - w) / 2
            val absX = loc[0] + xOff
            if (absX < 0) xOff -= absX
            if (absX + w > rootW) xOff -= (absX + w - rootW)
            pw.showAsDropDown(anchor, xOff, -(anchor.height + h))
            altPopup = pw
        } catch (e: Exception) {
            altPopup = null
        }
    }

    private fun insertEmoji(e: String) {
        flush()
        currentInputConnection?.commitText(e, 1)
        prefs.addRecent(e)
    }

    // ---- ইমোজি সার্চ (ইমোজি প্যানেলের সার্চ বারে চাপলে কীবোর্ডের অক্ষর দিয়ে খোঁজা যায়)

    private fun startEmojiSearch() {
        flush()
        searchMode = true
        searchBuf.setLength(0)
        symbols = false
        toolsBar?.visibility = View.GONE
        searchBar?.visibility = View.VISIBLE
        showPanel(PANEL_KEYS)
        updateSearch()
    }

    private fun endSearchUi() {
        searchMode = false
        searchBuf.setLength(0)
        searchBar?.visibility = View.GONE
        toolsBar?.visibility = View.VISIBLE
    }

    private fun stopEmojiSearch() {
        endSearchUi()
        showPanel(PANEL_EMOJI)
        refreshLabels()
        scheduleSuggest()
    }

    private fun updateSearch() {
        searchText?.text = "🔍 " + searchBuf + "▏"
        EmojiData.ensure { renderSearchResults() }
    }

    private fun renderSearchResults() {
        val res = searchResults ?: return
        val q = searchBuf.toString().trim()
        val list: List<String> = if (q.isEmpty()) {
            prefs.recents.split(" ").filter { it.isNotBlank() }.take(40)
        } else {
            EmojiData.search(q, AvroPhonetic.convert(q), 50)
        }
        res.removeAllViews()
        if (list.isEmpty()) {
            res.addView(TextView(this).apply {
                text = if (q.isEmpty()) "লিখে ইমোজি খুঁজুন…" else "কিছু পাওয়া যায়নি"
                setTextColor(kbTheme.hint)
                textSize = 13f
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(8f), 0, dp(8f), 0)
            }, LinearLayout.LayoutParams(WRAP, MATCH))
            return
        }
        for (e in list) {
            res.addView(TextView(this).apply {
                text = e
                textSize = 24f
                gravity = Gravity.CENTER
                setOnClickListener {
                    feedback(this)
                    insertEmoji(e)
                }
            }, LinearLayout.LayoutParams(dp(44f), MATCH))
        }
    }

    // ---------------------------------------------------------------- ক্লিপবোর্ড প্যানেল

    private fun chip(text: String, action: () -> Unit): TextView = TextView(this).apply {
        this.text = text
        setTextColor(kbTheme.text)
        textSize = 13f
        gravity = Gravity.CENTER
        setPadding(dp(12f), 0, dp(12f), 0)
        background = roundBg(kbTheme.keyTop)
        setOnClickListener {
            feedback(this)
            action()
        }
    }

    private fun buildClipPanel(): View {
        val p = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
        }
        val head = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val m = dp(3f)
        head.addView(chip("← ফিরুন") { showPanel(PANEL_KEYS) },
            LinearLayout.LayoutParams(WRAP, kh - dp(10f)).apply { setMargins(m, m, m, m) })
        head.addView(TextView(this).apply {
            text = "ক্লিপবোর্ড"
            setTextColor(kbTheme.text)
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(0, MATCH, 1f))
        head.addView(chip("সব মুছুন") {
            clips.clear()
            refreshClipList()
        }, LinearLayout.LayoutParams(WRAP, kh - dp(10f)).apply { setMargins(m, m, m, m) })
        p.addView(head, LinearLayout.LayoutParams(MATCH, kh))

        val sv = ScrollView(this)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        sv.addView(list, FrameLayout.LayoutParams(MATCH, WRAP))
        clipList = list
        p.addView(sv, LinearLayout.LayoutParams(MATCH, 0, 1f))
        return p
    }

    private fun refreshClipList() {
        val list = clipList ?: return
        list.removeAllViews()
        val items = clips.all()
        if (items.isEmpty()) {
            list.addView(TextView(this).apply {
                text = "এখনো কিছু কপি করা হয়নি।\nc চেপে ধরলে কপি হবে, কপি করা লেখা এখানে জমা থাকবে।"
                setTextColor(kbTheme.hint)
                textSize = 13f
                gravity = Gravity.CENTER
                setPadding(dp(12f), dp(24f), dp(12f), dp(12f))
            }, LinearLayout.LayoutParams(MATCH, WRAP))
            return
        }
        items.forEachIndexed { idx, t ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(10f), dp(6f), dp(4f), dp(6f))
                background = roundBg(kbTheme.keyTop, 12f)
            }
            row.addView(TextView(this).apply {
                text = t
                setTextColor(kbTheme.text)
                textSize = 14f
                maxLines = 2
                ellipsize = TextUtils.TruncateAt.END
                setOnClickListener {
                    feedback(this)
                    flush()
                    currentInputConnection?.commitText(t, 1)
                    showPanel(PANEL_KEYS)
                }
            }, LinearLayout.LayoutParams(0, WRAP, 1f))
            row.addView(TextView(this).apply {
                text = "✕"
                setTextColor(kbTheme.hint)
                textSize = 16f
                gravity = Gravity.CENTER
                setOnClickListener {
                    feedback(this)
                    clips.removeAt(idx)
                    refreshClipList()
                }
            }, LinearLayout.LayoutParams(dp(40f), dp(36f)))
            list.addView(row, LinearLayout.LayoutParams(MATCH, WRAP).apply {
                setMargins(dp(4f), dp(3f), dp(4f), dp(3f))
            })
        }
    }

    // ---------------------------------------------------------------- প্যানেল / লেবেল

    private fun showPanel(p: Int) {
        panel = p
        hidePreview()
        hideAlt()
        // ইমোজি প্যানেলে টুলবার থাকে না (রেফারেন্সের মতো), তাই জায়গাটাও ইমোজি পায়
        val emojiOn = p == PANEL_EMOJI
        toolbarView?.visibility = if (emojiOn) View.GONE else View.VISIBLE
        holderView?.let { hv ->
            val lp = hv.layoutParams
            val want = if (emojiOn) kh * 4 + tbHeight else kh * 4
            if (lp != null && lp.height != want) {
                lp.height = want
                hv.layoutParams = lp
            }
        }
        if (p == PANEL_NUM) applyNumOps()
        val useBn = bnFixed()
        val keys = p == PANEL_KEYS
        fun vis(v: View?, on: Boolean) {
            v?.visibility = if (on) View.VISIBLE else View.GONE
        }
        vis(lettersBox, keys && !symbols && !useBn)
        vis(bnBox, keys && !symbols && useBn && prefs.bnLayout == 2)
        vis(pbBox, keys && !symbols && useBn && prefs.bnLayout == 1)
        vis(sym1Box, keys && symbols && !symPage2)
        vis(sym2Box, keys && symbols && symPage2)
        vis(numBox, p == PANEL_NUM)
        vis(keyArea, keys || p == PANEL_NUM)
        vis(emojiPanel, p == PANEL_EMOJI)
        vis(clipPanel, p == PANEL_CLIP)
        currentKeys = when {
            p == PANEL_NUM -> keysNum
            keys && symbols -> if (symPage2) keysSym2 else keysSym1
            keys && useBn -> if (prefs.bnLayout == 2) keysBn else keysPb
            keys -> keysLetters
            else -> emptyList()
        }
        keyArea?.invalidateHits()
        styleTool(numBtn, p == PANEL_NUM)
        if (p == PANEL_CLIP) refreshClipList()
        if (p == PANEL_EMOJI) emojiFill?.invoke()
    }

    /** নাম্বার প্যাডের চিহ্ন-কী: হাতে খুললে + - * /, আর ঘরের ধরন অনুযায়ী শুধু যেগুলো লাগে সেগুলো (বাকিগুলো লুকানো) */
    private fun applyNumOps() {
        val syms: Array<String> = when (numCtx) {
            1 -> arrayOf(if (numDecimal) "." else "", if (numSigned) "-" else "", "", "")
            2 -> arrayOf("+", "-", "*", "#")
            3 -> arrayOf("/", ":", "-", ".")
            else -> arrayOf("+", "-", "*", "/")
        }
        for (i in 0..3) {
            numOpSyms[i] = syms[i]
            val k = numOps.getOrNull(i) ?: continue
            k.label = syms[i]
            k.visibility = if (syms[i].isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun refreshLabels() {
        val b = bn()
        val up = shiftState != 0
        val h = hi()
        for ((k, ch) in letterKeys) {
            val shown = if (up) ch.uppercaseChar() else ch
            k.label = shown.toString()
            k.hint = if (h) HindiHints.hint(shown) else ""
            k.subHint = when (ch) {
                'c' -> "কপি"
                'v' -> "পেস্ট"
                'x' -> "কাট"
                else -> ""
            }
            val alt = LONG_MAP[ch]
            if (alt != null) {
                val la = localizeAlt(alt)
                k.altHint = la
                k.altList = listOf(la)
            }
        }
        for ((k, d) in digitKeys) {
            k.label = localDigit(d).toString()
        }
        // কমা কী চেপে ধরলে: অভ্র/হিন্দিতে বিশেষ চিহ্ন
        val commaAlts: List<String>? = when {
            h -> listOf("`", "\u094D", "\u0901", "\u0902", "\u0903", "\u0950")
            b && prefs.bnLayout == 0 -> listOf("`", "^", ":")
            else -> null
        }
        for (k in commaKeys) {
            k.altList = commaAlts
            k.altHint = if (commaAlts != null) disp(commaAlts[0]) else ""
        }
        shiftKey?.active = shiftState != 0
        shiftKey?.subHint = if (shiftState == 2) "CAPS" else ""
        val cur = if (forceEnglish) 0 else langIndex()
        for (s in spaceKeys) {
            s.label = langNameAt(cur)
            s.altRight = langNameAt(cur + 1)
            s.altLeft = langNameAt(cur - 1)
        }
        refreshBnSlots()
        refreshPrabhat()
        styleTool(micBtn, listening)
    }

    private fun flash(msg: String) {
        val tv = flashView ?: return
        tv.animate().cancel()
        tv.alpha = 1f
        tv.typeface = Typeface.DEFAULT_BOLD
        tv.maxLines = 1
        tv.textSize = 13f
        tv.text = msg
        tv.visibility = View.VISIBLE
        handler.removeCallbacks(resetFlash)
        handler.postDelayed(resetFlash, 1600)
    }

    // ---------------------------------------------------------------- পপ-আপ ও ফিডব্যাক

    override fun keyPressed(k: KeyView) {
        feedback(k)
        if (k.previewable && prefs.popup) showPreview(k, null)
    }

    override fun keyReleased(k: KeyView) {
        hidePreview()
    }

    override fun altShow(k: KeyView, list: List<String>) {
        hidePreview()
        hideAlt()
        try {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                background = GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    intArrayOf(kbTheme.accentStart, kbTheme.accentEnd)
                ).apply {
                    cornerRadius = dp(14f).toFloat()
                    setStroke(dp(1f), Color.argb(150, 255, 255, 255))
                }
                setPadding(dp(4f), dp(4f), dp(4f), dp(4f))
                elevation = dp(8f).toFloat()
            }
            altItems.clear()
            for (s in list) {
                val tv = TextView(this).apply {
                    text = disp(s)
                    setTextColor(Color.WHITE)
                    textSize = 20f
                    gravity = Gravity.CENTER
                }
                altItems.add(tv)
                row.addView(tv, LinearLayout.LayoutParams(dp(42f), dp(50f)))
            }
            row.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            val w = row.measuredWidth
            val h = row.measuredHeight
            val pw = PopupWindow(row, w, h)
            pw.isTouchable = false
            pw.isFocusable = false
            pw.isClippingEnabled = false
            pw.inputMethodMode = PopupWindow.INPUT_METHOD_NOT_NEEDED
            val loc = IntArray(2)
            k.getLocationInWindow(loc)
            val rootW = k.rootView.width
            var xOff = (k.width - w) / 2
            val absX = loc[0] + xOff
            if (absX < 0) xOff -= absX
            if (absX + w > rootW) xOff -= (absX + w - rootW)
            pw.showAsDropDown(k, xOff, -(k.height + h) - dp(2f))
            altPopup = pw
            altOptions = list
            val sloc = IntArray(2)
            k.getLocationOnScreen(sloc)
            altLeftScreen = (sloc[0] + xOff + dp(4f)).toFloat()
            altItemW = dp(42f).toFloat()
            altSel = 0
            highlightAlt()
        } catch (e: Exception) {
            altPopup = null
            altOptions = emptyList()
        }
    }

    private fun highlightAlt() {
        altItems.forEachIndexed { i, tv ->
            tv.background = if (i == altSel) roundBg(Color.argb(110, 255, 255, 255), 10f) else null
        }
    }

    override fun altMove(k: KeyView, screenX: Float) {
        if (altOptions.isEmpty()) return
        val i = ((screenX - altLeftScreen) / altItemW).toInt().coerceIn(0, altOptions.size - 1)
        if (i != altSel) {
            altSel = i
            highlightAlt()
            try {
                k.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            } catch (e: Exception) {
            }
        }
    }

    override fun altEnd(k: KeyView, commit: Boolean) {
        val s = altOptions.getOrNull(altSel)
        hideAlt()
        if (commit && s != null) commitAlt(s)
    }

    private fun hideAlt() {
        try {
            altPopup?.dismiss()
        } catch (e: Exception) {
        }
        altPopup = null
        altOptions = emptyList()
        altItems.clear()
    }

    private fun commitAlt(s: String) {
        if (s.length == 1 && s[0] in '0'..'9') {
            typeDigit(s[0])
        } else if (s.any { it.code in 0x0980..0x09FF }) {
            commitBn(s)
        } else {
            commitChar(s)
        }
    }

    private fun feedback(v: View) {
        try {
            if (prefs.vibrate) {
                v.performHapticFeedback(
                    HapticFeedbackConstants.KEYBOARD_TAP,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                )
            }
            if (prefs.sound) {
                (getSystemService(Context.AUDIO_SERVICE) as AudioManager)
                    .playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 0.5f)
            }
        } catch (e: Exception) {
        }
    }

    /** কী চাপলে ওপরে ছোট বুদবুদ (লেজসহ) ভেসে ওঠে — চাপা কী-টা কোনটা সেটা লেজ দেখিয়ে দেয় */
    private fun showPreview(k: KeyView, override: String?) {
        val text0 = override ?: k.label
        if (text0.isEmpty()) return
        hidePreview()
        try {
            val sub = if (override == null && (bn() || hi()) && k.hint.isNotEmpty()) k.hint else ""
            val bw = (k.width * 1.25f).toInt().coerceIn(dp(50f), dp(78f))
            val bodyH = (k.height * 1.22f).toInt().coerceIn(dp(56f), dp(74f))
            val tailH = dp(9f)
            val totalH = bodyH + tailH
            val bubble = PreviewBubble(this, kbTheme.accentStart, kbTheme.accentEnd)
            bubble.text = text0
            bubble.sub = sub
            bubble.tailH = tailH.toFloat()

            val loc = IntArray(2)
            k.getLocationInWindow(loc)
            val rootW = k.rootView.width
            var xOff = (k.width - bw) / 2
            val absX = loc[0] + xOff
            val edge = dp(3f)
            if (absX < edge) xOff += edge - absX
            if (absX + bw > rootW - edge) xOff -= (absX + bw - (rootW - edge))
            bubble.tailX = k.width / 2f - xOff

            val pw = PopupWindow(bubble, bw, totalH)
            pw.isTouchable = false
            pw.isFocusable = false
            pw.isClippingEnabled = false
            pw.inputMethodMode = PopupWindow.INPUT_METHOD_NOT_NEEDED
            pw.showAsDropDown(k, xOff, -(k.height + totalH) + dp(3f))
            preview = pw
            bubble.scaleX = 0.6f
            bubble.scaleY = 0.6f
            bubble.alpha = 0f
            bubble.pivotX = bubble.tailX
            bubble.pivotY = totalH.toFloat()
            bubble.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(85).start()
        } catch (e: Exception) {
            preview = null
        }
    }

    private fun hidePreview() {
        try {
            preview?.dismiss()
        } catch (e: Exception) {
        }
        preview = null
    }

    // ---------------------------------------------------------------- টাইপিং লজিক

    private fun commit(s: String) {
        currentInputConnection?.commitText(s, 1)
    }

    private fun renderComposing() {
        val ic = currentInputConnection ?: return
        ic.setComposingText(convertRoman(roman.toString()), 1)
    }

    private fun flush() {
        if (roman.isEmpty()) return
        val t = convertRoman(roman.toString())
        roman.setLength(0)
        currentInputConnection?.commitText(t, 1)
    }

    private fun consumeShift() {
        if (shiftState == 1) {
            shiftState = 0
            refreshLabels()
        }
    }

    private fun typeLetter(c: Char) {
        val ch = if (shiftState != 0) c.uppercaseChar() else c
        if (searchMode) {
            searchBuf.append(ch.lowercaseChar())
            updateSearch()
            consumeShift()
            return
        }
        if (phonetic()) {
            roman.append(ch)
            renderComposing()
        } else {
            commit(ch.toString())
        }
        consumeShift()
        scheduleSuggest()
    }

    private fun typeDigit(d: Char) {
        if (searchMode) {
            searchBuf.append(d)
            updateSearch()
            return
        }
        flush()
        commit(localDigit(d).toString())
        scheduleSuggest()
    }

    private fun commitChar(s: String) {
        if (searchMode) {
            searchBuf.append(s)
            updateSearch()
            return
        }
        // অভ্র/হিন্দিতে শব্দের মাঝে ^ (চন্দ্রবিন্দু), ` (যুক্ত ভাঙা), : (বিসর্গ) বাফারেই ঢোকে
        if (phonetic() && roman.isNotEmpty() && !symbols && (s == "^" || s == "`" || s == ":")) {
            roman.append(s)
            renderComposing()
            return
        }
        flush()
        commit(s)
        scheduleSuggest()
    }

    /** বাংলা অক্ষর/যুক্তাক্ষর সরাসরি বসানো (প্রভাত ও জাতীয় লেআউট) */
    private fun commitBn(s: String) {
        if (searchMode) return
        flush()
        commit(s)
        scheduleSuggest()
    }

    private fun doSpace() {
        if (searchMode) {
            searchBuf.append(' ')
            updateSearch()
            return
        }
        flush()
        learnLast()
        commit(" ")
    }

    private fun doPeriod() {
        if (searchMode) {
            searchBuf.append('.')
            updateSearch()
            return
        }
        if (bn() || hi()) {
            if (roman.isNotEmpty()) {
                flush()
                commit("।")
                return
            }
            val before = currentInputConnection?.getTextBeforeCursor(1, 0)?.toString() ?: ""
            val prev = before.firstOrNull()
            if (prev != null && (prev in '0'..'9' || prev in '০'..'৯' || prev in '\u0966'..'\u096F')) commit(".") else commit("।")
        } else {
            flush()
            commit(".")
        }
    }

    private fun doEnter() {
        if (searchMode) {
            stopEmojiSearch()
            return
        }
        flush()
        learnLast()
        val ei = currentInputEditorInfo
        val ic = currentInputConnection
        if (ei == null || ic == null) {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
            return
        }
        val action = ei.imeOptions and EditorInfo.IME_MASK_ACTION
        val noAction = (ei.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0
        if (noAction || action == EditorInfo.IME_ACTION_NONE || action == EditorInfo.IME_ACTION_UNSPECIFIED) {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
        } else {
            ic.performEditorAction(action)
        }
    }

    private fun doBackspace() {
        if (searchMode) {
            if (searchBuf.isNotEmpty()) {
                searchBuf.setLength(searchBuf.length - 1)
                updateSearch()
            }
            return
        }
        if (roman.isNotEmpty()) {
            roman.setLength(roman.length - 1)
            renderComposing()
        } else {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
        }
    }

    /** বাংলা ও হিন্দিতে (অভ্র/প্রভাত/জাতীয়/হিন্দি) শিফট একবার চাপলেই কাজ করে, ক্যাপস-লক হয় না। ইংরেজিতে দু'বার চাপলে ক্যাপস-লক */
    private fun toggleShift() {
        shiftState = if (bn() || hi()) {
            if (shiftState == 0) 1 else 0
        } else {
            when (shiftState) {
                0 -> 1
                1 -> 2
                else -> 0
            }
        }
        refreshLabels()
    }

    /** বাক্যের শুরুতে ইংরেজি অক্ষর নিজে থেকেই বড় হাতের (ঘরের নিয়ম মানলে) */
    private fun autoCaps() {
        if (searchMode || bn() || hi() || shiftState == 2 || panel != PANEL_KEYS || symbols) return
        val ei = currentInputEditorInfo ?: return
        val ic = currentInputConnection ?: return
        val want = try {
            ic.getCursorCapsMode(ei.inputType) != 0
        } catch (e: Exception) {
            false
        }
        val ns = if (want) 1 else 0
        if (ns != shiftState) {
            shiftState = ns
            refreshLabels()
        }
    }

    private fun toggleSymbols() {
        symbols = !symbols
        symPage2 = false
        showPanel(PANEL_KEYS)
    }

    private fun toggleNumPad() {
        showPanel(if (panel == PANEL_NUM) PANEL_KEYS else PANEL_NUM)
    }

    /** স্পেসবার ডানে টানলে পরের ভাষা, বামে টানলে আগের ভাষা: English → অভ্র → প্রভাত/জাতীয় (যেটি চালু) → হিন্দি (চালু থাকলে) → English */
    private fun toggleMode(dir: Int) {
        if (searchMode) return
        if (forceEnglish) {
            flash("এই ঘরে শুধু ইংরেজি চলবে")
            return
        }
        flush()
        val cnt = langCount()
        val n = (((langIndex() + dir) % cnt) + cnt) % cnt
        banglaMode = n == 1 || n == 2
        hindiMode = n == 3
        if (n == 1) prefs.bnLayout = 0
        if (n == 2) prefs.bnLayout = prefs.fixedKind
        prefs.bangla = banglaMode
        prefs.hindi = hindiMode
        shiftState = 0
        bnPage = 0
        showPanel(PANEL_KEYS)
        refreshLabels()
        slideKeys(dir)
        flash(langNameAt(n) + " চালু")
        scheduleSuggest()
    }

    /** ভাষা বদলের সময় কী-গুলো পাশ থেকে মসৃণভাবে স্লাইড করে ঢোকে */
    private fun slideKeys(dir: Int) {
        val a = keyArea ?: return
        try {
            a.animate().cancel()
            a.translationX = -dir * dp(34f).toFloat()
            a.alpha = 0.15f
            a.animate()
                .translationX(0f)
                .alpha(1f)
                .setDuration(260)
                .setInterpolator(DecelerateInterpolator(1.8f))
                .start()
        } catch (e: Exception) {
            a.translationX = 0f
            a.alpha = 1f
        }
    }

    // ---------------------------------------------------------------- কপি / পেস্ট / কাট

    private fun doCopy() {
        if (searchMode) return
        flush()
        if (sensitiveField) {
            flash("পাসওয়ার্ড ঘরে কপি বন্ধ")
            return
        }
        val ic = currentInputConnection ?: return
        var text: CharSequence? = ic.getSelectedText(0)
        var all = false
        if (text.isNullOrEmpty()) {
            val b = ic.getTextBeforeCursor(20000, 0) ?: ""
            val a = ic.getTextAfterCursor(20000, 0) ?: ""
            text = "$b$a"
            all = true
        }
        if (text.isNullOrBlank()) {
            flash("কপি করার মতো কিছু নেই")
            return
        }
        val s = text.toString()
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("glass_keyboard", s))
        clips.add(s)
        showClipPop("📋", if (all) "সব লেখা কপি হয়েছে" else "কপি হয়েছে", s)
    }

    /** x চেপে ধরলে: সিলেক্ট করা লেখা, না থাকলে সব লেখা কেটে ক্লিপবোর্ডে যায় */
    private fun doCut() {
        if (searchMode) return
        flush()
        if (sensitiveField) {
            flash("পাসওয়ার্ড ঘরে কাট বন্ধ")
            return
        }
        val ic = currentInputConnection ?: return
        val sel = ic.getSelectedText(0)
        val s: String
        if (!sel.isNullOrEmpty()) {
            s = sel.toString()
            ic.commitText("", 1)
        } else {
            val b = ic.getTextBeforeCursor(20000, 0) ?: ""
            val a = ic.getTextAfterCursor(20000, 0) ?: ""
            s = "$b$a"
            if (s.isBlank()) {
                flash("কাট করার মতো কিছু নেই")
                return
            }
            ic.deleteSurroundingText(b.length, a.length)
        }
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("glass_keyboard", s))
        clips.add(s)
        showClipPop("✂️", "কাট হয়েছে", s)
        scheduleSuggest()
    }

    private fun doPaste() {
        if (searchMode) return
        flush()
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = cm.primaryClip
        val t: String? = if (clip != null && clip.itemCount > 0) {
            clip.getItemAt(0).coerceToText(this)?.toString()
        } else null
        if (t.isNullOrEmpty()) {
            flash("ক্লিপবোর্ড খালি")
            return
        }
        commit(t)
        showClipPop("📥", "পেস্ট হয়েছে", t)
    }

    // ---------------------------------------------------------------- কাট/কপি/পেস্টের বার্তা (সাজেশনের জায়গায়)

    /** কাট/কপি/পেস্ট হলে আলাদা পপ-আপ নয়, সাজেশন বারের জায়গাতেই ১.৫ সেকেন্ড লেখাটা দেখায় */
    private fun showClipPop(icon: String, title: String, body: String) {
        val tv = flashView
        if (tv == null) return
        try {
            val oneLine = body.replace(Regex("\\s+"), " ").trim()
            val shown = if (oneLine.length > 70) oneLine.substring(0, 70) + "…" else oneLine
            val sb = SpannableStringBuilder("$icon  $title")
            sb.setSpan(StyleSpan(Typeface.BOLD), 0, sb.length, 0)
            if (shown.isNotEmpty()) {
                sb.append("\n")
                val st = sb.length
                sb.append(shown)
                sb.setSpan(RelativeSizeSpan(0.88f), st, sb.length, 0)
            }
            tv.typeface = Typeface.DEFAULT
            tv.maxLines = 2
            tv.textSize = 13f
            tv.text = sb
            tv.visibility = View.VISIBLE
            tv.animate().cancel()
            tv.alpha = 0f
            tv.animate().alpha(1f).setDuration(140).start()
            handler.removeCallbacks(resetFlash)
            handler.postDelayed(resetFlash, 1500)
        } catch (e: Exception) {
            flash(title)
        }
    }

    // ---------------------------------------------------------------- সাজেশন

    private fun isWordChar(c: Char): Boolean {
        if (Character.isLetter(c) || c == '\'') return true
        val t = Character.getType(c)
        return t == Character.NON_SPACING_MARK.toInt() || t == Character.COMBINING_SPACING_MARK.toInt()
    }

    private fun trailingWord(s: String): String {
        var i = s.length
        while (i > 0 && isWordChar(s[i - 1])) i--
        return s.substring(i)
    }

    private fun scheduleSuggest() {
        handler.removeCallbacks(suggestRun)
        handler.postDelayed(suggestRun, 25)
    }

    private fun refreshSuggestions() {
        autoCaps()
        if (searchMode) return
        if (!prefs.suggest || !suggestOk || panel == PANEL_NUM) {
            setSuggestions(emptyList())
            return
        }
        val ic = currentInputConnection ?: return
        val before = ic.getTextBeforeCursor(60, 0)?.toString() ?: ""
        val pref = trailingWord(before)
        val rest = before.substring(0, before.length - pref.length)
        val list: List<String> = if (pref.isNotEmpty()) {
            suggester.complete(pref)
        } else {
            val prev = if (rest.isEmpty() || rest.last().isWhitespace()) trailingWord(rest.trimEnd()) else ""
            suggester.next(if (prev.isEmpty()) null else prev, if (hi()) 2 else if (bn()) 1 else 0)
        }
        setSuggestions(list)
    }

    /** সেরা সাজেশন মাঝে, বাকিগুলো দুই পাশে */
    private fun setSuggestions(list: List<String>) {
        val order = intArrayOf(1, 0, 2) // ভিউ ০ ← list[1], ভিউ ১ ← list[0], ভিউ ২ ← list[2]
        for (i in 0..2) {
            val s = list.getOrNull(order[i])
            sugCurrent[i] = s
            sugViews.getOrNull(i)?.apply {
                text = s ?: ""
                typeface = if (i == 1) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            }
        }
    }

    private fun applySuggestion(s: String) {
        val ic = currentInputConnection ?: return
        roman.setLength(0)
        ic.finishComposingText()
        val before = ic.getTextBeforeCursor(60, 0)?.toString() ?: ""
        val pref = trailingWord(before)
        val rest = before.substring(0, before.length - pref.length)
        ic.beginBatchEdit()
        if (pref.isNotEmpty()) ic.deleteSurroundingText(pref.length, 0)
        ic.commitText("$s ", 1)
        ic.endBatchEdit()
        if (prefs.learn && !sensitiveField) {
            val prev = trailingWord(rest.trimEnd())
            suggester.user.learn(if (prev.isEmpty()) null else prev, s)
        }
        scheduleSuggest()
    }

    /** শব্দ শেষ হলে (স্পেস/এন্টার) সেটা শিখে রাখে */
    private fun learnLast() {
        if (!prefs.suggest || !prefs.learn || sensitiveField || !suggestOk) return
        val ic = currentInputConnection ?: return
        val before = ic.getTextBeforeCursor(60, 0)?.toString() ?: return
        val w = trailingWord(before)
        if (w.length < 2) return
        val rest = before.substring(0, before.length - w.length)
        val prev = trailingWord(rest.trimEnd())
        suggester.user.learn(if (prev.isEmpty()) null else prev, w)
    }

    // ---------------------------------------------------------------- ভয়েস টাইপিং

    private fun startVoice() {
        if (listening) {
            stopVoice()
            return
        }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            flash("মাইক্রোফোনের অনুমতি দিন")
            try {
                val i = Intent(this, VoicePermissionActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(i)
            } catch (e: Exception) {
            }
            return
        }
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            flash("ভয়েস সার্ভিস নেই (Google অ্যাপ লাগবে)")
            return
        }
        flush()
        try {
            val r = SpeechRecognizer.createSpeechRecognizer(this)
            recognizer = r
            r.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    flash("🎤 বলুন…")
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onEvent(eventType: Int, params: Bundle?) {}

                override fun onError(error: Int) {
                    val msg = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "কিছু শোনা যায়নি"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "মাইক্রোফোনের অনুমতি দিন"
                        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "ইন্টারনেট দরকার"
                        else -> "ভয়েস কাজ করেনি"
                    }
                    finishVoice()
                    flash(msg)
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val l = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val t = l?.firstOrNull()
                    if (!t.isNullOrEmpty()) flash("🎤 $t")
                }

                override fun onResults(results: Bundle?) {
                    val l = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val t = l?.firstOrNull()
                    finishVoice()
                    if (!t.isNullOrEmpty()) {
                        flush()
                        commit("$t ")
                        scheduleSuggest()
                    }
                }
            })
            val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (hi()) "hi-IN" else if (bn()) "bn-BD" else "en-US")
            i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
            listening = true
            styleTool(micBtn, true)
            r.startListening(i)
        } catch (e: Exception) {
            finishVoice()
            flash("ভয়েস চালু করা যায়নি")
        }
    }

    private fun finishVoice() {
        listening = false
        styleTool(micBtn, false)
        val r = recognizer
        recognizer = null
        if (r != null) {
            handler.post {
                try {
                    r.destroy()
                } catch (e: Exception) {
                }
            }
        }
    }

    private fun stopVoice() {
        if (recognizer == null && !listening) return
        try {
            recognizer?.stopListening()
        } catch (e: Exception) {
        }
        finishVoice()
    }

    // ---------------------------------------------------------------- সেটিংস

    private fun openSettings() {
        val i = Intent(this, SettingsActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(i)
        requestHideSelf(0)
    }
}

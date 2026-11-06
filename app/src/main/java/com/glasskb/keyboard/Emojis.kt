package com.glasskb.keyboard

import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import java.util.Locale

/**
 * ইমোজি ফোনের নিজের ফন্ট থেকে যাচাই করে সাজানো হয় — যে ইমোজি ফোনে দেখাতে পারে না সেটা বাদ যায়।
 * তাই নতুন অ্যান্ড্রয়েডে নতুন ইমোজিও নিজে থেকেই আসে।
 */
object EmojiData {

    class Item(val s: String, val name: String)

    /** 0 = সাম্প্রতিক (আলাদা), এরপর এই ৯টি বিভাগ */
    val tabs = listOf("😀", "👍", "🐶", "🍎", "⚽", "🚗", "💡", "❤️", "♡", "🏳️")
    val tabNames = listOf("স্মাইলি", "মানুষ ও হাত", "প্রাণী ও প্রকৃতি", "খাবার", "খেলা", "ভ্রমণ", "জিনিসপত্র", "হৃদয় ও চিহ্ন", "বিশেষ চিহ্ন", "পতাকা")

    @Volatile
    var cats: List<List<Item>>? = null
    private var building = false
    private val waiting = ArrayList<() -> Unit>()
    private val main = Handler(Looper.getMainLooper())

    fun ensure(cb: () -> Unit) {
        if (cats != null) {
            cb()
            return
        }
        synchronized(waiting) {
            waiting.add(cb)
            if (building) return
            building = true
        }
        Thread {
            val r = try { build() } catch (e: Exception) { List(10) { emptyList<Item>() } }
            cats = r
            main.post {
                val l = synchronized(waiting) {
                    val c = ArrayList(waiting)
                    waiting.clear()
                    building = false
                    c
                }
                for (f in l) f()
            }
        }.start()
    }

    private fun rg(a: Int, b: Int) = a..b

    private val DEFS: List<List<IntRange>> = listOf(
        // 0 স্মাইলি
        listOf(
            rg(0x1F600, 0x1F644), rg(0x1F910, 0x1F917), rg(0x1F920, 0x1F925), rg(0x1F927, 0x1F92F),
            rg(0x1F970, 0x1F97A), rg(0x1FAE0, 0x1FAE8), rg(0x1F648, 0x1F64A), rg(0x1F4A9, 0x1F4A9),
            rg(0x1F47B, 0x1F47F), rg(0x1F480, 0x1F480), rg(0x1F916, 0x1F916), rg(0x2639, 0x263A)
        ),
        // 1 মানুষ ও হাত
        listOf(
            rg(0x1F440, 0x1F450), rg(0x1F466, 0x1F478), rg(0x1F47C, 0x1F47C), rg(0x1F481, 0x1F487),
            rg(0x1F4AA, 0x1F4AA), rg(0x1F645, 0x1F647), rg(0x1F64B, 0x1F64F), rg(0x1F90C, 0x1F90C),
            rg(0x1F90F, 0x1F90F), rg(0x1F918, 0x1F91F), rg(0x1F926, 0x1F926), rg(0x1F930, 0x1F939),
            rg(0x1F93C, 0x1F93E), rg(0x1F9B5, 0x1F9B9), rg(0x1F9BB, 0x1F9BF), rg(0x1F9D0, 0x1F9DF),
            rg(0x1F9E0, 0x1F9E0), rg(0x261D, 0x261D), rg(0x270A, 0x270D), rg(0x1F463, 0x1F465),
            rg(0x1FAC0, 0x1FAC5), rg(0x1FAF0, 0x1FAF8), rg(0x1F6B6, 0x1F6B6), rg(0x1F3C3, 0x1F3C4),
            rg(0x1F6A3, 0x1F6A3), rg(0x1F6B4, 0x1F6B5), rg(0x1F6C0, 0x1F6C0), rg(0x1F46A, 0x1F46D)
        ),
        // 2 প্রাণী ও প্রকৃতি
        listOf(
            rg(0x1F400, 0x1F43F), rg(0x1F980, 0x1F9AF), rg(0x1FAB0, 0x1FABA), rg(0x1F54A, 0x1F54A),
            rg(0x1F577, 0x1F578), rg(0x1F300, 0x1F344), rg(0x1F490, 0x1F490), rg(0x1F525, 0x1F525),
            rg(0x1F4A7, 0x1F4A7), rg(0x2600, 0x2604), rg(0x2614, 0x2614), rg(0x2744, 0x2744),
            rg(0x26C4, 0x26C5), rg(0x26A1, 0x26A1), rg(0x2B50, 0x2B50), rg(0x1F324, 0x1F32C)
        ),
        // 3 খাবার
        listOf(
            rg(0x1F345, 0x1F37F), rg(0x1F32D, 0x1F32F), rg(0x1F942, 0x1F944), rg(0x1F950, 0x1F96F),
            rg(0x1F9C0, 0x1F9CB), rg(0x1FAD0, 0x1FADB), rg(0x2615, 0x2615), rg(0x1F374, 0x1F37D)
        ),
        // 4 খেলা
        listOf(
            rg(0x1F380, 0x1F393), rg(0x1F396, 0x1F39B), rg(0x1F39E, 0x1F39F), rg(0x1F3A0, 0x1F3CA),
            rg(0x1F3CB, 0x1F3D3), rg(0x1F3F8, 0x1F3F9), rg(0x1F93A, 0x1F93B), rg(0x1F93F, 0x1F94F),
            rg(0x26BD, 0x26BE), rg(0x26F3, 0x26F3), rg(0x26F8, 0x26F8), rg(0x1F9E7, 0x1F9E9),
            rg(0x1FA80, 0x1FA86)
        ),
        // 5 ভ্রমণ
        listOf(
            rg(0x1F680, 0x1F6A2), rg(0x1F6A4, 0x1F6B3), rg(0x1F6B7, 0x1F6BF), rg(0x1F6C1, 0x1F6FF),
            rg(0x1F3D4, 0x1F3DF), rg(0x1F3E0, 0x1F3F0), rg(0x1F5FA, 0x1F5FF), rg(0x2693, 0x2693),
            rg(0x26E9, 0x26FA), rg(0x2708, 0x2708), rg(0x1F9ED, 0x1F9ED), rg(0x1F9F3, 0x1F9F3)
        ),
        // 6 জিনিসপত্র
        listOf(
            rg(0x231A, 0x231B), rg(0x23F0, 0x23F3), rg(0x1F451, 0x1F462), rg(0x1F484, 0x1F484),
            rg(0x1F489, 0x1F48A), rg(0x1F48B, 0x1F48E), rg(0x1F4A1, 0x1F4A1), rg(0x1F4A3, 0x1F4A3),
            rg(0x1F4B0, 0x1F4B9), rg(0x1F4BA, 0x1F4FD), rg(0x1F4FF, 0x1F4FF), rg(0x1F50A, 0x1F517),
            rg(0x1F526, 0x1F52E), rg(0x260E, 0x260E), rg(0x2709, 0x2709), rg(0x2702, 0x2702),
            rg(0x270F, 0x2712), rg(0x2692, 0x269C), rg(0x1F5A5, 0x1F5A5), rg(0x1F5A8, 0x1F5A8),
            rg(0x1F5B1, 0x1F5B2), rg(0x1F5BC, 0x1F5BC), rg(0x1F5C2, 0x1F5C4), rg(0x1F5D1, 0x1F5D3),
            rg(0x1F5DC, 0x1F5DE), rg(0x1F5E1, 0x1F5E1), rg(0x1F9E2, 0x1F9FF), rg(0x1F97B, 0x1F97F),
            rg(0x1FA70, 0x1FA7C), rg(0x1FA90, 0x1FAAC), rg(0x1F3A4, 0x1F3A4), rg(0x1F3A7, 0x1F3A7)
        ),
        // 7 হৃদয় ও চিহ্ন
        listOf(
            rg(0x1F493, 0x1F49F), rg(0x2763, 0x2764), rg(0x1F90D, 0x1F90E), rg(0x1F5A4, 0x1F5A4),
            rg(0x1F9E1, 0x1F9E1), rg(0x1F4A0, 0x1F4A0), rg(0x1F4A2, 0x1F4A2), rg(0x1F4A4, 0x1F4A6),
            rg(0x1F4A8, 0x1F4A8), rg(0x1F4AB, 0x1F4AF), rg(0x2728, 0x2728), rg(0x2705, 0x2705),
            rg(0x274C, 0x274C), rg(0x274E, 0x274E), rg(0x2753, 0x2755), rg(0x2757, 0x2757),
            rg(0x2795, 0x2797), rg(0x27B0, 0x27B0), rg(0x27BF, 0x27BF), rg(0x2B05, 0x2B07),
            rg(0x2B1B, 0x2B1C), rg(0x2B55, 0x2B55), rg(0x2194, 0x2199), rg(0x21A9, 0x21AA),
            rg(0x2934, 0x2935), rg(0x1F500, 0x1F509), rg(0x1F518, 0x1F524), rg(0x1F52F, 0x1F53D),
            rg(0x1F7E0, 0x1F7EB), rg(0x2660, 0x2660), rg(0x2663, 0x2663), rg(0x2665, 0x2666),
            rg(0x1F0CF, 0x1F0CF), rg(0x1F004, 0x1F004), rg(0x2640, 0x2642), rg(0x2695, 0x2695),
            rg(0x267B, 0x267B), rg(0x267E, 0x267E), rg(0x26A0, 0x26A0), rg(0x26AA, 0x26AB),
            rg(0x2622, 0x2623), rg(0x262E, 0x262F), rg(0x2626, 0x2626), rg(0x262A, 0x262A),
            rg(0x1F549, 0x1F549), rg(0x2721, 0x2721), rg(0x2638, 0x2638), rg(0x2648, 0x2653),
            rg(0x2716, 0x2716), rg(0x2714, 0x2714), rg(0x2733, 0x2734), rg(0x2747, 0x2747),
            rg(0x203C, 0x203C), rg(0x2049, 0x2049), rg(0x2122, 0x2122), rg(0x2139, 0x2139),
            rg(0x24C2, 0x24C2), rg(0x1F170, 0x1F171), rg(0x1F17E, 0x1F17F), rg(0x1F18E, 0x1F18E),
            rg(0x1F191, 0x1F19A), rg(0x1F201, 0x1F202), rg(0x1F21A, 0x1F21A), rg(0x1F22F, 0x1F22F),
            rg(0x1F232, 0x1F23A), rg(0x1F250, 0x1F251), rg(0x3297, 0x3297), rg(0x3299, 0x3299),
            rg(0x00A9, 0x00A9), rg(0x00AE, 0x00AE), rg(0x23E9, 0x23EF), rg(0x23F8, 0x23FA),
            rg(0x25B6, 0x25B6), rg(0x25C0, 0x25C0), rg(0x1F389, 0x1F38A), rg(0x1F31F, 0x1F31F),
            rg(0x1F6AB, 0x1F6AB), rg(0x1F51E, 0x1F51E)
        )
    )

    private val ZWJ = listOf(
        "👨‍👩‍👧", "👨‍👩‍👧‍👦", "👩‍❤️‍👨", "👨‍❤️‍👨", "👩‍❤️‍👩", "🧑‍💻", "👨‍💻", "👩‍💻",
        "👨‍🍳", "👩‍🍳", "👨‍🎓", "👩‍🎓", "👨‍⚕️", "👩‍⚕️", "👨‍🏫", "👩‍🏫", "👨‍🌾", "👩‍🌾",
        "👮‍♂️", "👮‍♀️", "🕵️‍♂️", "💂‍♂️", "👷‍♂️", "🧑‍🚀", "🧑‍🚒", "🧑‍🎨", "🧑‍🔧", "🧑‍🏭"
    )

    private val ZWJ_NAMES = mapOf(
        "👨‍👩‍👧" to "family", "👨‍👩‍👧‍👦" to "family", "👩‍❤️‍👨" to "couple with heart love",
        "🧑‍💻" to "technologist coder", "👨‍💻" to "man technologist coder",
        "👩‍💻" to "woman technologist coder", "👨‍🍳" to "man cook chef", "👩‍🍳" to "woman cook chef",
        "👨‍🎓" to "man student graduate", "👩‍🎓" to "woman student graduate",
        "👨‍⚕️" to "man doctor health worker", "👩‍⚕️" to "woman doctor health worker",
        "👨‍🏫" to "man teacher", "👩‍🏫" to "woman teacher", "👨‍🌾" to "man farmer", "👩‍🌾" to "woman farmer"
    )

    private val KEYCAPS = listOf(
        "#\uFE0F\u20E3", "*\uFE0F\u20E3", "0\uFE0F\u20E3", "1\uFE0F\u20E3", "2\uFE0F\u20E3", "3\uFE0F\u20E3",
        "4\uFE0F\u20E3", "5\uFE0F\u20E3", "6\uFE0F\u20E3", "7\uFE0F\u20E3", "8\uFE0F\u20E3", "9\uFE0F\u20E3", "🔟"
    )

    /** ফোনের ফন্টে পাওয়া সব ইমোজি, যেগুলো উপরের তালিকায় পড়েনি সেগুলোও "জিনিসপত্র"-এ যোগ হয় */
    private val LEFTOVER = listOf(
        rg(0x1F300, 0x1F64F), rg(0x1F680, 0x1F6FF), rg(0x1F900, 0x1FAFF)
    )

    /** ♡ ★ ✿ ➤ ∞ ₹ ৳ — টেক্সট স্টাইলের বিশেষ চিহ্ন */
    private val SPECIAL = listOf(
        rg(0x2660, 0x2667), rg(0x2600, 0x2638), rg(0x2639, 0x2653), rg(0x2654, 0x26FF),
        rg(0x2700, 0x27BF), rg(0x2190, 0x21FF), rg(0x2200, 0x22FF), rg(0x25A0, 0x25FF),
        rg(0x2B00, 0x2BFF), rg(0x2460, 0x24FF), rg(0x2100, 0x214F), rg(0x2150, 0x218F),
        rg(0x20A0, 0x20C0), rg(0x2500, 0x259F), rg(0x2300, 0x23FF), rg(0x2E00, 0x2E2F),
        rg(0x00A1, 0x00BF), rg(0x00D7, 0x00D7), rg(0x00F7, 0x00F7), rg(0x2010, 0x2027),
        rg(0x2030, 0x205E), rg(0x2070, 0x209F), rg(0x09F2, 0x09FB),
        rg(0x0964, 0x0965), rg(0x3000, 0x303F), rg(0xFF01, 0xFF0F)
    )

    private fun build(): List<List<Item>> {
        val paint = Paint()
        val seen = HashSet<String>()
        val out = ArrayList<MutableList<Item>>()
        for ((idx, ranges) in DEFS.withIndex()) {
            val items = ArrayList<Item>()
            for (rg in ranges) for (cp in rg) addCp(cp, paint, items, seen)
            if (idx == 1) {
                for (z in ZWJ) {
                    if (seen.add(z) && paint.hasGlyph(z)) items.add(Item(z, ZWJ_NAMES[z] ?: "person"))
                }
            }
            if (idx == 7) {
                for (k in KEYCAPS) {
                    if (seen.add(k) && paint.hasGlyph(k)) items.add(Item(k, "keycap"))
                }
            }
            out.add(items)
        }
        // বাদ পড়া ইমোজি "জিনিসপত্র"-এ
        val objects = out[6]
        for (rgn in LEFTOVER) for (cp in rgn) {
            if (cp in 0x1F1E6..0x1F1FF || cp in 0x1F3FB..0x1F3FF) continue
            addCp(cp, paint, objects, seen)
        }
        // বিশেষ চিহ্ন (টেক্সট স্টাইল): ♡ ★ ✿ ➤ ...
        val special = ArrayList<Item>()
        val seenText = HashSet<String>()
        for (rgn in SPECIAL) for (cp in rgn) {
            if (Character.isISOControl(cp) || Character.isWhitespace(cp)) continue
            val s = String(Character.toChars(cp))
            if (!seenText.add(s)) continue
            // ইমোজি স্টাইলে যেগুলো আগেই আছে সেগুলো আবার দেওয়া হবে না
            if (seen.contains(s + "\uFE0F") || seen.contains(s)) continue
            if (!paint.hasGlyph(s)) continue
            val nm = try { android.icu.lang.UCharacter.getName(cp) ?: "" } catch (e: Throwable) { "" }
            special.add(Item(s, nm.lowercase()))
        }
        out.add(special)
        out.add(flags(paint).toMutableList())
        return out
    }

    private fun addCp(cp: Int, paint: Paint, items: MutableList<Item>, seen: MutableSet<String>) {
        val base = String(Character.toChars(cp))
        val withVs = base + "\uFE0F"
        // কিছু ফোনে ❤️ (বেস + FE0F) এভাবে যাচাইয়ে ধরা পড়ে না, তাই দুটোই দেখা হয়
        val ok: Boolean
        val s: String
        if (cp < 0x1F000) {
            ok = paint.hasGlyph(withVs) || paint.hasGlyph(base)
            s = withVs
        } else {
            ok = paint.hasGlyph(base)
            s = base
        }
        if (!ok || seen.contains(s)) return
        seen.add(s)
        seen.add(base)
        val nm = try {
            android.icu.lang.UCharacter.getName(cp) ?: ""
        } catch (e: Throwable) {
            ""
        }
        items.add(Item(s, nm.lowercase()))
    }

    private fun ri(c: Char): String = String(Character.toChars(0x1F1E6 + (c - 'A')))

    private fun flags(paint: Paint): List<Item> {
        val items = ArrayList<Item>()
        val misc = listOf(
            "🏁" to "chequered flag", "🚩" to "triangular flag", "🎌" to "crossed flags",
            "🏴" to "black flag", "🏳️" to "white flag", "🏳️‍🌈" to "rainbow flag pride",
            "🏴‍☠️" to "pirate flag"
        )
        val pri = listOf("BD", "IN", "PK", "US", "GB", "SA", "AE", "CA", "AU", "MY", "NP", "LK", "QA", "KW", "OM", "SG", "JP", "CN", "TR", "EG")
        val codes = Locale.getISOCountries().toList()
        val sorted = pri.filter { codes.contains(it) } + codes.filter { !pri.contains(it) }.sorted()
        for (cc in sorted) {
            if (cc.length != 2) continue
            val s = ri(cc[0]) + ri(cc[1])
            if (!paint.hasGlyph(s)) continue
            val nm = Locale("", cc).getDisplayCountry(Locale.ENGLISH).lowercase()
            items.add(Item(s, "flag $nm"))
        }
        for ((s, n) in misc) {
            if (paint.hasGlyph(s)) items.add(Item(s, n))
        }
        return items
    }

    // ------------------------------------------------------------ স্কিন টোন

    private val TONE_BASES: List<IntRange> = listOf(
        rg(0x261D, 0x261D), rg(0x26F9, 0x26F9), rg(0x270A, 0x270D), rg(0x1F385, 0x1F385),
        rg(0x1F3C2, 0x1F3C4), rg(0x1F3C7, 0x1F3C7), rg(0x1F3CA, 0x1F3CC), rg(0x1F442, 0x1F443),
        rg(0x1F446, 0x1F450), rg(0x1F466, 0x1F478), rg(0x1F47C, 0x1F47C), rg(0x1F481, 0x1F483),
        rg(0x1F485, 0x1F487), rg(0x1F4AA, 0x1F4AA), rg(0x1F574, 0x1F575), rg(0x1F57A, 0x1F57A),
        rg(0x1F590, 0x1F590), rg(0x1F595, 0x1F596), rg(0x1F645, 0x1F647), rg(0x1F64B, 0x1F64F),
        rg(0x1F6A3, 0x1F6A3), rg(0x1F6B4, 0x1F6B6), rg(0x1F6C0, 0x1F6C0), rg(0x1F6CC, 0x1F6CC),
        rg(0x1F90C, 0x1F90C), rg(0x1F90F, 0x1F90F), rg(0x1F918, 0x1F91F), rg(0x1F926, 0x1F926),
        rg(0x1F930, 0x1F939), rg(0x1F93C, 0x1F93E), rg(0x1F977, 0x1F977), rg(0x1F9B5, 0x1F9B6),
        rg(0x1F9B8, 0x1F9B9), rg(0x1F9BB, 0x1F9BB), rg(0x1F9CD, 0x1F9CF), rg(0x1F9D1, 0x1F9DD),
        rg(0x1FAC3, 0x1FAC5), rg(0x1FAF0, 0x1FAF8)
    )

    /** হাত/মানুষের ইমোজির জন্য ৫টি স্কিন টোনসহ তালিকা; অন্যগুলোর জন্য খালি */
    fun toneVariants(emoji: String): List<String> {
        if (emoji.isEmpty()) return emptyList()
        val cp = emoji.codePointAt(0)
        val len = Character.charCount(cp)
        // শুধু সিঙ্গেল ইমোজি (পরে শুধু FE0F থাকলে চলবে)
        val rest = emoji.substring(len)
        if (rest.isNotEmpty() && rest != "\uFE0F") return emptyList()
        if (TONE_BASES.none { cp in it }) return emptyList()
        val paint = Paint()
        val base = String(Character.toChars(cp))
        val out = ArrayList<String>()
        out.add(emoji)
        for (t in 0x1F3FB..0x1F3FF) {
            val v = base + String(Character.toChars(t))
            if (paint.hasGlyph(v)) out.add(v)
        }
        return if (out.size > 1) out else emptyList()
    }

    // ------------------------------------------------------------ সার্চ

    private val ALIAS: Map<String, String> = mapOf(
        "হাসি" to "smil grin laugh joy", "হাসা" to "smil grin laugh joy", "haha" to "joy laugh",
        "কান্না" to "cry sob tear", "কাঁদা" to "cry sob tear", "মনখারাপ" to "sad cry pensive disappointed",
        "ভালোবাসা" to "heart kiss love", "ভালবাসা" to "heart kiss love", "প্রেম" to "heart kiss love",
        "রাগ" to "angry rage pouting", "ফুল" to "flower blossom rose tulip", "খাবার" to "food pizza hamburger fries",
        "ফল" to "fruit apple banana mango", "গাড়ি" to "car automobile", "পাখি" to "bird", "বিড়াল" to "cat",
        "কুকুর" to "dog", "চাঁদ" to "moon", "সূর্য" to "sun", "ঘুম" to "sleep zzz", "চা" to "tea",
        "কফি" to "coffee", "টাকা" to "money dollar coin", "বই" to "book", "মোবাইল" to "mobile phone",
        "ফোন" to "mobile phone telephone", "আগুন" to "fire", "দোয়া" to "folded hands pray", "হাত" to "hand",
        "চোখ" to "eye", "মুখ" to "mouth face", "নাচ" to "dance", "গান" to "music musical", "ক্রিকেট" to "cricket",
        "ফুটবল" to "soccer football", "বাংলাদেশ" to "bangladesh", "ভারত" to "india", "পাকিস্তান" to "pakistan",
        "ঈদ" to "crescent moon star", "মসজিদ" to "mosque", "কাবা" to "kaaba", "তারা" to "star",
        "বৃষ্টি" to "rain umbrella cloud", "বিয়ে" to "wedding ring bride", "জন্মদিন" to "birthday cake balloon party",
        "উপহার" to "gift present", "ধন্যবাদ" to "thanks folded hands", "ওকে" to "ok hand thumbs", "লাইক" to "thumbs up",
        "ok" to "ok hand thumbs up", "lol" to "joy laugh", "omg" to "astonished shocked", "like" to "thumbs up",
        "sad" to "sad cry pensive disappointed frown", "happy" to "smil grin", "love" to "heart kiss love",
        "angry" to "angry rage pouting", "fire" to "fire", "cool" to "sunglasses", "sleep" to "sleep zzz",
        "food" to "food pizza hamburger", "party" to "party popper confetti", "pray" to "folded hands"
    )

    fun search(q: String, qBn: String, limit: Int): List<String> {
        val c = cats ?: return emptyList()
        val terms = ArrayList<String>()
        val t1 = q.trim().lowercase()
        if (t1.isEmpty()) return emptyList()
        terms.add(t1)
        for ((k, v) in ALIAS) {
            val hit = k.startsWith(t1) || (qBn.isNotEmpty() && k.startsWith(qBn))
            if (hit) for (w in v.split(' ')) if (w.isNotEmpty()) terms.add(w)
        }
        val first = ArrayList<String>()
        val second = ArrayList<String>()
        val seen = HashSet<String>()
        for (list in c) for (it in list) {
            if (it.name.isEmpty() || seen.contains(it.s)) continue
            var starts = false
            var has = false
            for (t in terms) {
                if (it.name.startsWith(t) || it.name.contains(" $t")) {
                    starts = true
                    break
                }
                if (it.name.contains(t)) has = true
            }
            if (starts) {
                first.add(it.s)
                seen.add(it.s)
            } else if (has) {
                second.add(it.s)
                seen.add(it.s)
            }
        }
        return (first + second).take(limit)
    }
}

package com.glasskb.keyboard

import android.content.SharedPreferences
import android.graphics.Color

class KbTheme(
    val name: String,
    val bg: IntArray,
    val keyTop: Int,
    val keyBottom: Int,
    val keyStroke: Int,
    val specialTop: Int,
    val specialBottom: Int,
    val accentStart: Int,
    val accentEnd: Int,
    val text: Int,
    val hint: Int,
    val press: Int
)

object Themes {
    private fun p(s: String): Int = Color.parseColor(s)

    private fun glassDark(
        name: String, b1: String, b2: String, b3: String, a1: String, a2: String
    ) = KbTheme(
        name,
        intArrayOf(p(b1), p(b2), p(b3)),
        p("#44FFFFFF"), p("#1CFFFFFF"), p("#80FFFFFF"),
        p("#55000000"), p("#33000000"),
        p(a1), p(a2),
        p("#FFFFFFFF"), p("#B3FFFFFF"), p("#55FFFFFF")
    )

    private fun glassLight(
        name: String, b1: String, b2: String, b3: String, a1: String, a2: String,
        sp1: String, sp2: String, txt: String, hint: String
    ) = KbTheme(
        name,
        intArrayOf(p(b1), p(b2), p(b3)),
        p("#E6FFFFFF"), p("#99FFFFFF"), p("#FFFFFFFF"),
        p(sp1), p(sp2),
        p(a1), p(a2),
        p(txt), p(hint), p("#33000000")
    )

    val all: List<KbTheme> by lazy {
        listOf(
            glassDark("অরোরা", "#FF141E3C", "#FF3B2A78", "#FF0F6E8C", "#FF7F5AF0", "#FF2CB1BC"),
            glassDark("সানসেট", "#FF2B1055", "#FFC33764", "#FFE8743B", "#FFFF512F", "#FFDD2476"),
            glassDark("ওশান", "#FF0F2027", "#FF203A43", "#FF2C5364", "#FF00C6FF", "#FF0072FF"),
            glassDark("ফরেস্ট", "#FF053B2C", "#FF0B7A5E", "#FF2BB673", "#FF38EF7D", "#FF11998E"),
            glassDark("রোজ", "#FF3D0C3A", "#FF9C2A6B", "#FFD9558F", "#FFFF5E9C", "#FFB24BF3"),
            glassLight(
                "ফ্রস্ট", "#FFDCE7F7", "#FFEFE3F7", "#FFFBE8EF", "#FF6C8CFF", "#FFA06CFF",
                "#66B9C6E6", "#44A8B8DC", "#FF1E2340", "#99283050"
            ),
            glassDark("নাইট", "#FF000000", "#FF0A0A12", "#FF121226", "#FF00E5FF", "#FF2979FF"),
            // ---- নতুন থিম ----
            glassLight(
                "চেরি ব্লসম", "#FFFCE4EC", "#FFF8BBD0", "#FFF48FB1", "#FFEC407A", "#FFAB47BC",
                "#66F06292", "#44F06292", "#FF4A1530", "#99602040"
            ),
            glassLight(
                "মিন্ট ফ্রেশ", "#FFE0F7F1", "#FFC8F0E3", "#FFA7E8D3", "#FF1DB98A", "#FF0F8F7A",
                "#6692D6C0", "#4476C8B0", "#FF0B3D33", "#99135E50"
            ),
            glassLight(
                "ল্যাভেন্ডার", "#FFEDE7F6", "#FFD1C4E9", "#FFB39DDB", "#FF7E57C2", "#FF5E35B1",
                "#66B39DDB", "#449575CD", "#FF2A1458", "#99402878"
            ),
            glassLight(
                "পিচ", "#FFFFF3E0", "#FFFFE0B2", "#FFFFCC80", "#FFFF9800", "#FFF4511E",
                "#66FFB74D", "#44FFA726", "#FF4E2A00", "#99694000"
            ),
            KbTheme(
                "গোল্ডেন নাইট",
                intArrayOf(p("#FF0B0B0B"), p("#FF1A1405"), p("#FF2A1F08")),
                p("#26FFD700"), p("#0FFFD700"), p("#66FFD54F"),
                p("#55000000"), p("#33000000"),
                p("#FFFFB300"), p("#FFFF6F00"),
                p("#FFFFF3C4"), p("#B3FFE082"), p("#44FFD54F")
            ),
            KbTheme(
                "নিয়ন সাইবার",
                intArrayOf(p("#FF0D0221"), p("#FF190535"), p("#FF2D0A4E")),
                p("#2600F0FF"), p("#12FF2BD6"), p("#99FF2BD6"),
                p("#55000000"), p("#33000000"),
                p("#FFFF2BD6"), p("#FF00B8D4"),
                p("#FFFFFFFF"), p("#CCB8F0FF"), p("#55FF2BD6")
            ),
            glassDark("লাভা", "#FF1A0000", "#FF4A0A0A", "#FF8A1C0C", "#FFFF5722", "#FFFFA000"),
            glassDark("রয়েল ব্লু", "#FF0A1F44", "#FF123A7A", "#FF1E5BB5", "#FF4FC3F7", "#FF1976D2"),
            glassDark("এমারেল্ড", "#FF00201A", "#FF004D40", "#FF00796B", "#FF1DE9B6", "#FF00BFA5"),
            KbTheme(
                "কার্বন",
                intArrayOf(p("#FF1B1B1F"), p("#FF1F1F24"), p("#FF232328")),
                p("#FF3C3C42"), p("#FF36363C"), p("#FF4A4A52"),
                p("#FF2A2A2F"), p("#FF26262B"),
                p("#FF8AB4F8"), p("#FF669DF6"),
                p("#FFE8EAED"), p("#FF9AA0A6"), p("#44FFFFFF")
            ),
            KbTheme(
                "টিল ডার্ক",
                intArrayOf(p("#FF000000"), p("#FF050808"), p("#FF0A1110")),
                p("#FF1D1D20"), p("#FF18181B"), p("#FF2A2A2E"),
                p("#FF009688"), p("#FF00877A"),
                p("#FF009688"), p("#FF00A896"),
                p("#FF80E0EC"), p("#FF139C90"), p("#44FFFFFF")
            ),
            KbTheme(
                "রিদমিক লাইট",
                intArrayOf(p("#FFE8EAED"), p("#FFE3E6EA"), p("#FFDDE1E6")),
                p("#FFFFFFFF"), p("#FFF8F9FA"), p("#FFD0D4D9"),
                p("#FFD3D8DE"), p("#FFCBD0D7"),
                p("#FF8AB4F8"), p("#FF669DF6"),
                p("#FF202124"), p("#FF70757A"), p("#33000000")
            )
        )
    }

    // ---------------------------------------------------------------- কাস্টম থিম

    /** কাস্টমাইজ পেজে যে অংশগুলোর রঙ বদলানো যায়: কী, নাম, ডিফল্ট রঙ */
    val customFields: List<Triple<String, String, String>> = listOf(
        Triple("bg1", "কীবোর্ডের ব্যাকগ্রাউন্ড (ওপর)", "#FF141E3C"),
        Triple("bg2", "কীবোর্ডের ব্যাকগ্রাউন্ড (নিচ)", "#FF0F6E8C"),
        Triple("key", "সাধারণ কী (অক্ষর কী)", "#44FFFFFF"),
        Triple("special", "বিশেষ কী (শিফট, ডিলিট, ?123)", "#55000000"),
        Triple("acc1", "অ্যাকসেন্ট কী — শুরুর রঙ (এন্টার, সিলেক্টেড)", "#FF7F5AF0"),
        Triple("acc2", "অ্যাকসেন্ট কী — শেষের রঙ", "#FF2CB1BC"),
        Triple("text", "কী-এর অক্ষরের রঙ", "#FFFFFFFF"),
        Triple("hint", "কী-এর ওপরের ছোট চিহ্ন / টুলবার আইকন", "#B3FFFFFF"),
        Triple("border", "কী-এর বর্ডার", "#80FFFFFF")
    )

    /** কাস্টম থিমের ইনডেক্স = সাধারণ থিমগুলোর পরের নম্বর */
    val customIndex: Int get() = all.size

    private fun fade(c: Int, f: Float): Int {
        val a = (Color.alpha(c) * f).toInt().coerceIn(0, 255)
        return (c and 0x00FFFFFF) or (a shl 24)
    }

    private fun mix(a: Int, b: Int): Int = Color.argb(
        (Color.alpha(a) + Color.alpha(b)) / 2,
        (Color.red(a) + Color.red(b)) / 2,
        (Color.green(a) + Color.green(b)) / 2,
        (Color.blue(a) + Color.blue(b)) / 2
    )

    fun customColor(sp: SharedPreferences, key: String): Int {
        val def = customFields.firstOrNull { it.first == key }?.third ?: "#FFFFFFFF"
        return sp.getInt("c_$key", p(def))
    }

    fun custom(sp: SharedPreferences): KbTheme {
        fun g(k: String): Int = customColor(sp, k)
        val bg1 = g("bg1")
        val bg2 = g("bg2")
        val key = g("key")
        val sk = g("special")
        val a1 = g("acc1")
        return KbTheme(
            "কাস্টম",
            intArrayOf(bg1, mix(bg1, bg2), bg2),
            key, fade(key, 0.55f), g("border"),
            sk, fade(sk, 0.6f),
            a1, g("acc2"),
            g("text"), g("hint"), fade(a1, 0.35f)
        )
    }

    fun resolve(sp: SharedPreferences, index: Int): KbTheme =
        if (index >= all.size) custom(sp) else all[index.coerceIn(0, all.size - 1)]

    /** কোনো তৈরি থিম থেকে কাস্টমের রঙগুলো কপি করে নেয় (শুরু করার জন্য) */
    fun copyToCustom(sp: SharedPreferences, t: KbTheme) {
        sp.edit()
            .putInt("c_bg1", t.bg.first())
            .putInt("c_bg2", t.bg.last())
            .putInt("c_key", t.keyTop)
            .putInt("c_special", t.specialTop)
            .putInt("c_acc1", t.accentStart)
            .putInt("c_acc2", t.accentEnd)
            .putInt("c_text", t.text)
            .putInt("c_hint", t.hint)
            .putInt("c_border", t.keyStroke)
            .apply()
    }

    fun resetCustom(sp: SharedPreferences) {
        val e = sp.edit()
        for (f in customFields) e.remove("c_" + f.first)
        e.apply()
    }
}

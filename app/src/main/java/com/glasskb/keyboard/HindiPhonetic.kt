package com.glasskb.keyboard

/**
 * হিন্দি (देवनागरी) ফোনেটিক কনভার্টার — রোমান অক্ষর থেকে দেবনাগরী।
 * অভ্রর মতোই কাজ করে: প্রতিটি কী চাপার পর পুরো রোমান বাফার থেকে হিন্দি নতুন করে বানানো হয়।
 *
 * নিয়ম (সংক্ষেপে):
 *  - a = অ (ব্যঞ্জনের পরে কিছুই বসে না; তবে শব্দের একদম শেষের a = ा), aa/A = आ/ा, i = इ/ि, ee/ii/I = ई/ी, u = उ/ु, oo/uu/U = ऊ/ू
 *  - e = ए/े, ai/E = ऐ/ै, o = ओ/ो, au/ou/O = औ/ौ, rri = ऋ/ृ
 *  - ব্যঞ্জনের পরে ব্যঞ্জন লিখলে নিজে নিজেই যুক্তাক্ষর (হলন্ত) হয়: kt = क्त, pr = प्र
 *  - M = ं, ^ = ँ, H বা : = ः, ` = যুক্ত ভাঙা
 */
object HindiPhonetic {

    private enum class Type { VOWEL, CONS, SIGN, BREAK }

    private class Tok(val key: String, val type: Type, val a: String = "", val b: String = "")

    private const val HAL = "\u094D"   // ्
    private const val NUK = "\u093C"   // ़

    private val toks: List<Tok> = listOf(
        // স্বর (পূর্ণ রূপ, মাত্রা)
        Tok("rri", Type.VOWEL, "\u090B", "\u0943"),   // ऋ ृ
        Tok("aa", Type.VOWEL, "\u0906", "\u093E"),    // आ ा
        Tok("ai", Type.VOWEL, "\u0910", "\u0948"),    // ऐ ै
        Tok("au", Type.VOWEL, "\u0914", "\u094C"),    // औ ौ
        Tok("oi", Type.VOWEL, "\u0910", "\u0948"),
        Tok("OI", Type.VOWEL, "\u0910", "\u0948"),
        Tok("ou", Type.VOWEL, "\u0914", "\u094C"),
        Tok("OU", Type.VOWEL, "\u0914", "\u094C"),
        Tok("ee", Type.VOWEL, "\u0908", "\u0940"),    // ई ी
        Tok("ii", Type.VOWEL, "\u0908", "\u0940"),
        Tok("oo", Type.VOWEL, "\u090A", "\u0942"),    // ऊ ू
        Tok("uu", Type.VOWEL, "\u090A", "\u0942"),
        Tok("a", Type.VOWEL, "\u0905", ""),           // अ (ব্যঞ্জনের পরে কিছু না)
        Tok("A", Type.VOWEL, "\u0906", "\u093E"),
        Tok("i", Type.VOWEL, "\u0907", "\u093F"),     // इ ि
        Tok("I", Type.VOWEL, "\u0908", "\u0940"),
        Tok("u", Type.VOWEL, "\u0909", "\u0941"),     // उ ु
        Tok("U", Type.VOWEL, "\u090A", "\u0942"),
        Tok("e", Type.VOWEL, "\u090F", "\u0947"),     // ए े
        Tok("E", Type.VOWEL, "\u0910", "\u0948"),
        Tok("o", Type.VOWEL, "\u0913", "\u094B"),     // ओ ो
        Tok("O", Type.VOWEL, "\u0914", "\u094C"),

        // বিশেষ যুক্তাক্ষর
        Tok("kkh", Type.CONS, "\u0915$HAL\u0937"),    // क्ष
        Tok("ksh", Type.CONS, "\u0915$HAL\u0937"),
        Tok("kSh", Type.CONS, "\u0915$HAL\u0937"),
        Tok("jny", Type.CONS, "\u091C$HAL\u091E"),    // ज्ञ
        Tok("gy", Type.CONS, "\u091C$HAL\u091E"),
        Tok("gny", Type.CONS, "\u091C$HAL\u091E"),
        Tok("GY", Type.CONS, "\u091C$HAL\u091E"),
        Tok("x", Type.CONS, "\u0915$HAL\u0938"),      // क्स

        // ব্যঞ্জন
        Tok("chh", Type.CONS, "\u091B"),              // छ
        Tok("Rh", Type.CONS, "\u0922$NUK"),           // ढ़
        Tok("NG", Type.CONS, "\u091E"),               // ञ
        Tok("Ng", Type.CONS, "\u0919"),               // ङ
        Tok("kh", Type.CONS, "\u0916"),               // ख
        Tok("gh", Type.CONS, "\u0918"),               // घ
        Tok("ch", Type.CONS, "\u091B"),               // छ
        Tok("jh", Type.CONS, "\u091D"),               // झ
        Tok("Th", Type.CONS, "\u0920"),               // ठ
        Tok("Dh", Type.CONS, "\u0922"),               // ढ
        Tok("th", Type.CONS, "\u0925"),               // थ
        Tok("dh", Type.CONS, "\u0927"),               // ध
        Tok("ph", Type.CONS, "\u092B"),               // फ
        Tok("bh", Type.CONS, "\u092D"),               // भ
        Tok("sh", Type.CONS, "\u0936"),               // श
        Tok("Sh", Type.CONS, "\u0937"),               // ष
        Tok("k", Type.CONS, "\u0915"),                // क
        Tok("g", Type.CONS, "\u0917"),                // ग
        Tok("c", Type.CONS, "\u091A"),                // च
        Tok("j", Type.CONS, "\u091C"),                // ज
        Tok("T", Type.CONS, "\u091F"),                // ट
        Tok("D", Type.CONS, "\u0921"),                // ड
        Tok("N", Type.CONS, "\u0923"),                // ण
        Tok("t", Type.CONS, "\u0924"),                // त
        Tok("d", Type.CONS, "\u0926"),                // द
        Tok("n", Type.CONS, "\u0928"),                // न
        Tok("p", Type.CONS, "\u092A"),                // प
        Tok("f", Type.CONS, "\u092B$NUK"),            // फ़
        Tok("b", Type.CONS, "\u092C"),                // ब
        Tok("m", Type.CONS, "\u092E"),                // म
        Tok("y", Type.CONS, "\u092F"),                // य
        Tok("r", Type.CONS, "\u0930"),                // र
        Tok("l", Type.CONS, "\u0932"),                // ल
        Tok("v", Type.CONS, "\u0935"),                // व
        Tok("w", Type.CONS, "\u0935"),
        Tok("s", Type.CONS, "\u0938"),                // स
        Tok("S", Type.CONS, "\u0937"),                // ष
        Tok("h", Type.CONS, "\u0939"),                // ह
        Tok("R", Type.CONS, "\u0921$NUK"),            // ड़
        Tok("q", Type.CONS, "\u0915$NUK"),            // क़
        Tok("K", Type.CONS, "\u0916$NUK"),            // ख़
        Tok("G", Type.CONS, "\u0917$NUK"),            // ग़
        Tok("z", Type.CONS, "\u091C$NUK"),            // ज़
        Tok("Z", Type.CONS, "\u091C$NUK"),

        // চিহ্ন
        Tok("M", Type.SIGN, "\u0902"),                // ं
        Tok("^", Type.SIGN, "\u0901"),                // ँ
        Tok("H", Type.SIGN, "\u0903"),                // ः
        Tok(":", Type.SIGN, "\u0903"),
        Tok("`", Type.BREAK)
    ).sortedByDescending { it.key.length }

    private fun find(s: String, i: Int): Tok? {
        for (t in toks) {
            if (s.startsWith(t.key, i)) return t
        }
        val lc = s[i].lowercaseChar()
        if (lc != s[i]) {
            for (t in toks) {
                if (t.key.length == 1 && t.key[0] == lc) return t
            }
        }
        return null
    }

    fun convert(src: String): String {
        val sb = StringBuilder()
        // state: 0 = ফাঁকা/আটকানো, 1 = ব্যঞ্জনের পর
        var state = 0
        var afterBreak = false
        var i = 0
        while (i < src.length) {
            val tok = find(src, i)
            if (tok == null) {
                sb.append(src[i])
                state = 0
                i++
                continue
            }
            i += tok.key.length
            val brk = afterBreak
            afterBreak = false
            when (tok.type) {
                Type.VOWEL -> {
                    if (state == 1) {
                        // শব্দের শেষে একটি a = ा (mera = मेरा, kya = क्या)
                        if (tok.key == "a" && i >= src.length) sb.append("\u093E") else sb.append(tok.b)
                    } else {
                        sb.append(tok.a)
                    }
                    state = 0
                }
                Type.CONS -> {
                    if (state == 1) sb.append(HAL)
                    sb.append(tok.a)
                    state = 1
                }
                Type.SIGN -> {
                    if (tok.key == ":" && brk) sb.append(':') else sb.append(tok.a)
                    state = 0
                }
                Type.BREAK -> {
                    state = 0
                    afterBreak = true
                }
            }
        }
        return sb.toString()
    }
}

/** হিন্দি মোডে কীর কোণে ছোট করে দেখানো দেবনাগরী অক্ষর */
object HindiHints {
    private val map = mapOf(
        'a' to "\u0905", 'b' to "\u092C", 'c' to "\u091A", 'd' to "\u0926", 'e' to "\u090F", 'f' to "\u092B\u093C",
        'g' to "\u0917", 'h' to "\u0939", 'i' to "\u0907", 'j' to "\u091C", 'k' to "\u0915", 'l' to "\u0932",
        'm' to "\u092E", 'n' to "\u0928", 'o' to "\u0913", 'p' to "\u092A", 'q' to "\u0915\u093C", 'r' to "\u0930",
        's' to "\u0938", 't' to "\u0924", 'u' to "\u0909", 'v' to "\u0935", 'w' to "\u0935", 'x' to "\u0915\u094D\u0938",
        'y' to "\u092F", 'z' to "\u091C\u093C",
        'A' to "\u0906", 'E' to "\u0910", 'G' to "\u0917\u093C", 'H' to "\u0903", 'I' to "\u0908", 'K' to "\u0916\u093C",
        'M' to "\u0902", 'N' to "\u0923", 'O' to "\u0914", 'R' to "\u0921\u093C", 'S' to "\u0937", 'T' to "\u091F",
        'U' to "\u090A", 'D' to "\u0921", 'Z' to "\u091C\u093C"
    )

    fun hint(c: Char): String = map[c] ?: map[c.lowercaseChar()] ?: ""
}

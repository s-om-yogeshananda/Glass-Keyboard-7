package com.glasskb.keyboard

/**
 * অভ্র ফোনেটিক ধাঁচের রোমান -> বাংলা কনভার্টার।
 * কীবোর্ড প্রতিটি কী চাপার পর পুরো রোমান বাফার থেকে বাংলা নতুন করে বানায়,
 * তাই বানান ভাঙার সুযোগ থাকে না।
 */
object AvroPhonetic {

    private enum class Type { VOWEL, CONS, SIGN, BREAK, NG, Y, ZF, W, RR }

    private class Tok(val key: String, val type: Type, val a: String = "", val b: String = "")

    private val toks: List<Tok> = listOf(
        // স্বরবর্ণ (পূর্ণ রূপ, কার রূপ)
        Tok("rri", Type.VOWEL, "ঋ", "ৃ"),
        Tok("oi", Type.VOWEL, "ঐ", "ৈ"),
        Tok("OI", Type.VOWEL, "ঐ", "ৈ"),
        Tok("ou", Type.VOWEL, "ঔ", "ৌ"),
        Tok("OU", Type.VOWEL, "ঔ", "ৌ"),
        Tok("ii", Type.VOWEL, "ঈ", "ী"),
        Tok("ee", Type.VOWEL, "ঈ", "ী"),
        Tok("uu", Type.VOWEL, "ঊ", "ূ"),
        Tok("oo", Type.VOWEL, "উ", "ু"),
        Tok("aa", Type.VOWEL, "আ", "া"),
        Tok("a", Type.VOWEL, "আ", "া"),
        Tok("i", Type.VOWEL, "ই", "ি"),
        Tok("I", Type.VOWEL, "ঈ", "ী"),
        Tok("u", Type.VOWEL, "উ", "ু"),
        Tok("U", Type.VOWEL, "ঊ", "ূ"),
        Tok("e", Type.VOWEL, "এ", "ে"),
        Tok("O", Type.VOWEL, "ও", "ো"),
        Tok("o", Type.VOWEL, "অ", ""),

        // বিশেষ যুক্তাক্ষর (অভ্র রেফারেন্স চার্ট অনুযায়ী)
        Tok("ngkh", Type.CONS, "ঙ্খ"),
        Tok("ngk", Type.CONS, "ঙ্ক"),
        Tok("ngg", Type.CONS, "ঙ্গ"),
        Tok("Ngg", Type.CONS, "ঙ্গ"),
        Tok("nk", Type.CONS, "ঙ্ক"),
        Tok("kkh", Type.CONS, "ক্ষ"),
        Tok("ksh", Type.CONS, "ক্ষ"),
        Tok("kSh", Type.CONS, "ক্ষ"),
        Tok("hm", Type.CONS, "হ্ম"),
        Tok("NGjh", Type.CONS, "ঞ্ঝ"),
        Tok("NGch", Type.CONS, "ঞ্ছ"),
        Tok("nch", Type.CONS, "ঞ্ছ"),
        Tok("NGc", Type.CONS, "ঞ্চ"),
        Tok("nc", Type.CONS, "ঞ্চ"),
        Tok("NGj", Type.CONS, "ঞ্জ"),
        Tok("nj", Type.CONS, "ঞ্জ"),
        Tok("ND", Type.CONS, "ণ্ড"),
        Tok("ShN", Type.CONS, "ষ্ণ"),
        Tok("gg", Type.CONS, "জ্ঞ"),
        Tok("jNG", Type.CONS, "জ্ঞ"),
        Tok("x", Type.CONS, "ক্স"),

        // ব্যঞ্জনবর্ণ
        Tok("chh", Type.CONS, "ছ"),
        Tok("NG", Type.CONS, "ঞ"),
        Tok("Ng", Type.CONS, "ঙ"),
        Tok("kh", Type.CONS, "খ"),
        Tok("gh", Type.CONS, "ঘ"),
        Tok("ch", Type.CONS, "ছ"),
        Tok("jh", Type.CONS, "ঝ"),
        Tok("Th", Type.CONS, "ঠ"),
        Tok("Dh", Type.CONS, "ঢ"),
        Tok("th", Type.CONS, "থ"),
        Tok("dh", Type.CONS, "ধ"),
        Tok("ph", Type.CONS, "ফ"),
        Tok("bh", Type.CONS, "ভ"),
        Tok("sh", Type.CONS, "শ"),
        Tok("Sh", Type.CONS, "ষ"),
        Tok("Rh", Type.CONS, "ঢ়"),
        Tok("k", Type.CONS, "ক"),
        Tok("g", Type.CONS, "গ"),
        Tok("c", Type.CONS, "চ"),
        Tok("j", Type.CONS, "জ"),
        Tok("J", Type.CONS, "জ"),
        Tok("T", Type.CONS, "ট"),
        Tok("D", Type.CONS, "ড"),
        Tok("N", Type.CONS, "ণ"),
        Tok("t", Type.CONS, "ত"),
        Tok("d", Type.CONS, "দ"),
        Tok("n", Type.CONS, "ন"),
        Tok("p", Type.CONS, "প"),
        Tok("f", Type.CONS, "ফ"),
        Tok("b", Type.CONS, "ব"),
        Tok("v", Type.CONS, "ভ"),
        Tok("m", Type.CONS, "ম"),
        Tok("z", Type.CONS, "য"),
        Tok("l", Type.CONS, "ল"),
        Tok("S", Type.CONS, "শ"),
        Tok("s", Type.CONS, "স"),
        Tok("h", Type.CONS, "হ"),
        Tok("R", Type.CONS, "ড়"),
        Tok("q", Type.CONS, "ক"),
        Tok("r", Type.CONS, "র"),

        Tok("rr", Type.RR),
        Tok("y", Type.Y),
        Tok("Y", Type.Y),
        Tok("Z", Type.ZF),
        Tok("w", Type.W),
        Tok("ng", Type.NG),

        // চিহ্ন
        Tok("t``", Type.SIGN, "ৎ"),
        Tok("H", Type.SIGN, "ঃ"),
        Tok(":", Type.SIGN, "ঃ"),
        Tok("^", Type.SIGN, "ঁ"),
        Tok("qq", Type.SIGN, "ঁ"),
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

    private fun isVowelLetter(c: Char?): Boolean = c != null && "aeiouAEIOU".indexOf(c) >= 0

    fun convert(src: String): String {
        val sb = StringBuilder()
        // state: 0 = ফাঁকা/আটকানো, 1 = ব্যঞ্জনের পর, 2 = হসন্তের পর
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
                        sb.append(tok.b)
                    } else if (state == 2) {
                        sb.setLength(sb.length - 1)
                        sb.append(tok.b)
                    } else {
                        sb.append(tok.a)
                    }
                    state = 0
                }
                Type.CONS -> {
                    if (state == 1) sb.append('্')
                    sb.append(tok.a)
                    state = 1
                }
                Type.RR -> {
                    if (state == 1) sb.append('্')
                    sb.append("র্")
                    state = 2
                }
                Type.Y -> {
                    if (state == 1) {
                        sb.append("্য")
                    } else {
                        sb.append("য়")
                    }
                    state = 1
                }
                Type.ZF -> {
                    sb.append("্য")
                    state = 1
                }
                Type.W -> {
                    if (state == 1) {
                        sb.append("্ব")
                        state = 1
                    } else {
                        sb.append("ও")
                        state = 0
                    }
                }
                Type.NG -> {
                    if (isVowelLetter(src.getOrNull(i))) {
                        if (state == 1) sb.append('্')
                        sb.append("ঙ")
                        state = 1
                    } else {
                        sb.append("ং")
                        state = 0
                    }
                }
                Type.SIGN -> {
                    // ` দিয়ে ভাঙার পরের ":" সাধারণ কোলন হয়ে বসে
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

object AvroHints {
    private val lower = mapOf(
        'a' to "আ", 'b' to "ব", 'c' to "চ", 'd' to "দ", 'e' to "এ", 'f' to "ফ",
        'g' to "গ", 'h' to "হ", 'i' to "ই", 'j' to "জ", 'k' to "ক", 'l' to "ল",
        'm' to "ম", 'n' to "ন", 'o' to "অ", 'p' to "প", 'q' to "ক", 'r' to "র",
        's' to "স", 't' to "ত", 'u' to "উ", 'v' to "ভ", 'w' to "ও", 'x' to "ক্স",
        'y' to "য়", 'z' to "য"
    )
    private val upper = mapOf(
        'T' to "ট", 'D' to "ড", 'N' to "ণ", 'S' to "শ", 'R' to "ড়", 'O' to "ও",
        'I' to "ঈ", 'U' to "ঊ", 'J' to "জ", 'Y' to "য়", 'H' to "ঃ"
    )

    fun hint(c: Char): String {
        return if (c.isUpperCase()) {
            upper[c] ?: lower[c.lowercaseChar()] ?: ""
        } else {
            lower[c] ?: ""
        }
    }
}

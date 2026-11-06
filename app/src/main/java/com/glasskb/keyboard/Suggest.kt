package com.glasskb.keyboard

import android.content.Context
import org.json.JSONObject

/** আপনি যা টাইপ করেন সেখান থেকে শেখা শব্দ ও শব্দ-জোড়া। শুধু ফোনেই থাকে, কোথাও পাঠানো হয় না। */
class UserDict(ctx: Context) {
    private val sp = ctx.getSharedPreferences("glass_dict", Context.MODE_PRIVATE)
    private val words = HashMap<String, Int>()
    private val bigrams = HashMap<String, Int>()
    private var dirty = 0
    private var stamp = 0L

    init {
        load()
    }

    private fun load() {
        stamp = sp.getLong("stamp", 0L)
        try {
            val w = JSONObject(sp.getString("w", "{}") ?: "{}")
            for (k in w.keys()) words[k] = w.getInt(k)
            val b = JSONObject(sp.getString("b", "{}") ?: "{}")
            for (k in b.keys()) bigrams[k] = b.getInt(k)
        } catch (e: Exception) {
            words.clear()
            bigrams.clear()
        }
    }

    /** সেটিংস থেকে শব্দ বদলানো হলে কীবোর্ড সার্ভিস নতুন করে পড়ে নেয় */
    fun reload() {
        if (dirty > 0) return
        if (sp.getLong("stamp", 0L) != stamp) {
            words.clear()
            bigrams.clear()
            load()
        }
    }

    private fun norm(w: String): String = if (w.any { it.code in 0x0900..0x09FF }) w else w.lowercase()

    fun learn(prev: String?, word: String) {
        val w = norm(word)
        if (w.length < 2 || w.any { it.isDigit() }) return
        words[w] = (words[w] ?: 0) + 1
        if (prev != null && prev.isNotEmpty()) {
            val key = norm(prev) + "\t" + w
            bigrams[key] = (bigrams[key] ?: 0) + 1
        }
        dirty++
        if (dirty >= 25) save()
    }

    private fun prune(m: HashMap<String, Int>, limit: Int) {
        if (m.size <= limit) return
        val sorted = m.entries.sortedByDescending { it.value }
        val keep = sorted.take(limit * 3 / 4).map { it.key }.toHashSet()
        val it = m.keys.iterator()
        while (it.hasNext()) if (!keep.contains(it.next())) it.remove()
    }

    fun save() {
        if (dirty == 0) return
        persist()
    }

    private fun persist() {
        dirty = 0
        prune(words, 4000)
        prune(bigrams, 6000)
        val st = System.currentTimeMillis()
        stamp = st
        try {
            sp.edit()
                .putString("w", JSONObject(words as Map<*, *>).toString())
                .putString("b", JSONObject(bigrams as Map<*, *>).toString())
                .putLong("stamp", st)
                .apply()
        } catch (e: Exception) {
        }
    }

    fun clear() {
        words.clear()
        bigrams.clear()
        dirty = 0
        val st = System.currentTimeMillis()
        stamp = st
        sp.edit().clear().putLong("stamp", st).apply()
    }

    /** শেখা সব শব্দ, বেশিবার ব্যবহার করা আগে */
    fun all(): List<Pair<String, Int>> =
        words.entries.sortedByDescending { it.value }.map { Pair(it.key, it.value) }

    /** হাতে নতুন শব্দ যোগ */
    fun addWord(w: String): Boolean {
        val x = norm(w.trim())
        if (x.length < 1 || x.contains(' ')) return false
        words[x] = (words[x] ?: 0) + 3
        persist()
        return true
    }

    fun removeWord(w: String) {
        words.remove(w)
        val it = bigrams.keys.iterator()
        while (it.hasNext()) {
            val k = it.next()
            val parts = k.split("\t")
            if (parts.size == 2 && (parts[0] == w || parts[1] == w)) it.remove()
        }
        persist()
    }

    /** শব্দের বানান বদলানো (ব্যবহারের সংখ্যা একই থাকে) */
    fun renameWord(old: String, nw: String): Boolean {
        val x = norm(nw.trim())
        if (x.isEmpty() || x.contains(' ')) return false
        if (x == old) return true
        val c = words.remove(old) ?: 1
        words[x] = (words[x] ?: 0) + c
        val upd = HashMap<String, Int>()
        val it = bigrams.entries.iterator()
        while (it.hasNext()) {
            val e = it.next()
            val parts = e.key.split("\t")
            if (parts.size != 2) continue
            if (parts[0] == old || parts[1] == old) {
                val a = if (parts[0] == old) x else parts[0]
                val b = if (parts[1] == old) x else parts[1]
                val key = a + "\t" + b
                upd[key] = (upd[key] ?: 0) + e.value
                it.remove()
            }
        }
        for ((k, v) in upd) bigrams[k] = (bigrams[k] ?: 0) + v
        persist()
        return true
    }

    fun count(): Int = words.size

    fun matching(prefix: String, limit: Int): List<String> {
        val p = norm(prefix)
        return words.entries
            .filter { it.key.startsWith(p) && it.key != p }
            .sortedByDescending { it.value }
            .take(limit)
            .map { it.key }
    }

    fun followers(prev: String, limit: Int): List<String> {
        val p = norm(prev) + "\t"
        return bigrams.entries
            .filter { it.key.startsWith(p) }
            .sortedByDescending { it.value }
            .take(limit)
            .map { it.key.substring(p.length) }
    }
}

class Suggester(val user: UserDict) {

    /** 0 = ইংরেজি/ল্যাটিন, 1 = বাংলা, 2 = দেবনাগরী (হিন্দি) */
    private fun scriptOf(s: String): Int {
        for (c in s) {
            val code = c.code
            if (code in 0x0980..0x09FF) return 1
            if (code in 0x0900..0x097F) return 2
        }
        return 0
    }

    private fun isBn(s: String) = scriptOf(s) != 0

    private fun matchCase(src: String, w: String): String {
        if (isBn(w) || src.isEmpty()) return w
        if (src.length >= 2 && src.all { it.isUpperCase() }) return w.uppercase()
        if (src[0].isUpperCase()) return w.replaceFirstChar { it.uppercase() }
        if (w == "i") return "I"
        return w
    }

    /** টাইপ করা অংশ দিয়ে শুরু হওয়া শব্দ (প্রয়োজনে ছোট বানান ভুল ধরে) */
    fun complete(prefixRaw: String, limit: Int = 3): List<String> {
        if (prefixRaw.isEmpty()) return emptyList()
        val sc = scriptOf(prefixRaw)
        val bn = sc != 0
        val pref = if (bn) prefixRaw else prefixRaw.lowercase()
        val out = LinkedHashSet<String>()
        for (w in user.matching(pref, limit)) out.add(w)
        if (out.size < limit) {
            val dict = when (sc) {
                1 -> Words.bn
                2 -> Words.hi
                else -> Words.en
            }
            for (w in dict) {
                if (w.startsWith(pref) && w != pref) {
                    out.add(w)
                    if (out.size >= limit) break
                }
            }
        }
        if (out.isEmpty() && !bn && pref.length >= 3) {
            for (w in Words.en) {
                if (w[0] == pref[0] && kotlin.math.abs(w.length - pref.length) <= 1 && editDistanceAtMost1(w, pref)) {
                    out.add(w)
                    if (out.size >= limit) break
                }
            }
        }
        return out.take(limit).map { matchCase(prefixRaw, it) }
    }

    /** পরের শব্দের অনুমান */
    /** lang: 0 = ইংরেজি, 1 = বাংলা, 2 = হিন্দি */
    fun next(prev: String?, lang: Int, limit: Int = 3): List<String> {
        val out = LinkedHashSet<String>()
        if (prev != null && prev.isNotEmpty()) {
            val ps = scriptOf(prev)
            val key = if (ps != 0) prev else prev.lowercase()
            for (w in user.followers(key, limit)) out.add(w)
            val seedMap: Map<String, List<String>> = when (ps) {
                1 -> Words.bnNext
                2 -> Words.hiNext
                else -> Words.enNext
            }
            val seeds = seedMap[key]
            if (seeds != null) {
                for (w in seeds) {
                    out.add(w)
                    if (out.size >= limit) break
                }
            }
        }
        if (out.size < limit) {
            val starters = when (lang) {
                1 -> Words.bnStart
                2 -> Words.hiStart
                else -> Words.enStart
            }
            for (w in starters) {
                out.add(w)
                if (out.size >= limit) break
            }
        }
        return out.take(limit).toList()
    }

    private fun editDistanceAtMost1(a: String, b: String): Boolean {
        if (a == b) return true
        val la = a.length
        val lb = b.length
        if (kotlin.math.abs(la - lb) > 1) return false
        var i = 0
        var j = 0
        var edits = 0
        while (i < la && j < lb) {
            if (a[i] == b[j]) {
                i++
                j++
            } else {
                edits++
                if (edits > 1) return false
                if (la > lb) i++ else if (lb > la) j++ else {
                    i++
                    j++
                }
            }
        }
        edits += (la - i) + (lb - j)
        return edits <= 1
    }
}

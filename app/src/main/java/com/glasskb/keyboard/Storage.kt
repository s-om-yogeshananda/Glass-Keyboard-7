package com.glasskb.keyboard

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray

class Prefs(ctx: Context) {
    val sp: SharedPreferences = ctx.getSharedPreferences("glass_kb", Context.MODE_PRIVATE)

    var scale: Float
        get() = sp.getFloat("scale", 1.0f)
        set(v) { sp.edit().putFloat("scale", v).apply() }

    var theme: Int
        get() = sp.getInt("theme", 0)
        set(v) { sp.edit().putInt("theme", v).apply() }

    var popup: Boolean
        get() = sp.getBoolean("popup", true)
        set(v) { sp.edit().putBoolean("popup", v).apply() }

    var vibrate: Boolean
        get() = sp.getBoolean("vibrate", true)
        set(v) { sp.edit().putBoolean("vibrate", v).apply() }

    var sound: Boolean
        get() = sp.getBoolean("sound", false)
        set(v) { sp.edit().putBoolean("sound", v).apply() }

    var bangla: Boolean
        get() = sp.getBoolean("bangla", false)
        set(v) { sp.edit().putBoolean("bangla", v).apply() }

    var suggest: Boolean
        get() = sp.getBoolean("suggest", true)
        set(v) { sp.edit().putBoolean("suggest", v).apply() }

    var learn: Boolean
        get() = sp.getBoolean("learn", true)
        set(v) { sp.edit().putBoolean("learn", v).apply() }

    /** ভলিউম বাটন দিয়ে কার্সর সরানো */
    var volCursor: Boolean
        get() = sp.getBoolean("volcursor", false)
        set(v) { sp.edit().putBoolean("volcursor", v).apply() }

    /** বাংলা মোডে নাম্বার বাংলা (১২৩) হবে কি না */
    var bnDigits: Boolean
        get() = sp.getBoolean("bndigits", true)
        set(v) { sp.edit().putBoolean("bndigits", v).apply() }

    /** এখন হিন্দি মোডে আছে কি না (বাংলা মোড তখন বন্ধ) */
    var hindi: Boolean
        get() = sp.getBoolean("hindi", false)
        set(v) { sp.edit().putBoolean("hindi", v).apply() }

    /** স্পেসবার স্লাইডের তালিকায় হিন্দি মোড থাকবে কি না */
    var hindiOn: Boolean
        get() = sp.getBoolean("hindion", true)
        set(v) { sp.edit().putBoolean("hindion", v).apply() }

    /** হিন্দি মোডে নাম্বার দেবনাগরী (१२३) হবে কি না */
    var hiDigits: Boolean
        get() = sp.getBoolean("hidigits", true)
        set(v) { sp.edit().putBoolean("hidigits", v).apply() }

    /** এখন যে বাংলা লেআউটে আছে: 0 = অভ্র ফোনেটিক, 1 = প্রভাত, 2 = জাতীয় */
    var bnLayout: Int
        get() = sp.getInt("bnlayout", 0)
        set(v) { sp.edit().putInt("bnlayout", v).apply() }

    /** সেটিংসে চালু রাখা ফিক্সড লেআউট: 1 = প্রভাত, 2 = জাতীয় (একসাথে দুটো নয়) */
    var fixedKind: Int
        get() = sp.getInt("fixedkind", 1).let { if (it == 2) 2 else 1 }
        set(v) { sp.edit().putInt("fixedkind", if (v == 2) 2 else 1).apply() }

    var recents: String
        get() = sp.getString("recents", "") ?: ""
        set(v) { sp.edit().putString("recents", v).apply() }

    fun addRecent(e: String) {
        val l = recents.split(" ").filter { it.isNotBlank() }.toMutableList()
        l.remove(e)
        l.add(0, e)
        while (l.size > 32) l.removeAt(l.size - 1)
        recents = l.joinToString(" ")
    }
}

class ClipStore(ctx: Context) {
    private val sp: SharedPreferences = ctx.getSharedPreferences("glass_clips", Context.MODE_PRIVATE)

    fun all(): List<String> {
        val raw = sp.getString("items", "[]") ?: "[]"
        return try {
            val a = JSONArray(raw)
            (0 until a.length()).map { a.getString(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun save(list: List<String>) {
        sp.edit().putString("items", JSONArray(list).toString()).apply()
    }

    fun add(text: String) {
        if (text.isBlank()) return
        val t = if (text.length > 20000) text.substring(0, 20000) else text
        val l = all().toMutableList()
        l.remove(t)
        l.add(0, t)
        while (l.size > 60) l.removeAt(l.size - 1)
        save(l)
    }

    /** ইতিহাসের কোনো লেখা বদলানো (সম্পাদনা) */
    fun update(i: Int, text: String) {
        val l = all().toMutableList()
        if (i !in l.indices) return
        val t = if (text.length > 20000) text.substring(0, 20000) else text
        if (t.isBlank()) {
            l.removeAt(i)
        } else {
            l[i] = t
            // একই লেখা দু'বার থাকলে একটি রাখা
            val first = l.indexOf(t)
            if (first != i) l.removeAt(i)
        }
        save(l)
    }

    fun removeAt(i: Int) {
        val l = all().toMutableList()
        if (i in l.indices) {
            l.removeAt(i)
            save(l)
        }
    }

    fun clear() {
        save(emptyList())
    }
}

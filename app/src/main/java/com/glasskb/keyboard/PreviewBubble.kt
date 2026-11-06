package com.glasskb.keyboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.view.View

/**
 * কী চাপলে ওপরে যে বুদবুদ (বাবল) ভেসে ওঠে সেটা। নিচে ছোট তীরের মতো লেজ থাকে, যেটা ঠিক চাপা কী-কে দেখায়।
 * এতে মনে হয় না যে আরেকটা কী ওপরে উঠে এসেছে।
 */
class PreviewBubble(ctx: Context, private val c1: Int, private val c2: Int) : View(ctx) {

    var text: String = ""
    var sub: String = ""
    /** লেজের মাঝের বিন্দুর x (পপ-আপের বাঁ দিক থেকে) */
    var tailX: Float = 0f
    /** লেজের উচ্চতা (px) */
    var tailH: Float = 0f

    private val d = resources.displayMetrics.density
    private val bodyP = Paint(Paint.ANTI_ALIAS_FLAG)
    private val lineP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.2f * d
        color = Color.argb(170, 255, 255, 255)
    }
    private val shadowP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(80, 0, 0, 0) }
    private val glowP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(34, 255, 255, 255) }
    private val textP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val subP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(215, 255, 255, 255)
        textAlign = Paint.Align.CENTER
    }
    private val rect = RectF()
    private val bodyPath = Path()
    private val tailPath = Path()

    override fun onDraw(c: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val bh = h - tailH
        if (w <= 0f || bh <= 0f) return
        val r = minOf(w, bh) * 0.30f

        // নিচে হালকা ছায়া
        rect.set(d, 3f * d, w - d, bh + 2.5f * d)
        c.drawRoundRect(rect, r, r, shadowP)

        // মূল বডি
        rect.set(d, d, w - d, bh)
        val sh = LinearGradient(0f, 0f, 0f, h, c1, c2, Shader.TileMode.CLAMP)
        bodyP.shader = sh
        bodyPath.reset()
        bodyPath.addRoundRect(rect, r, r, Path.Direction.CW)
        c.drawPath(bodyPath, bodyP)

        // লেজ (তীর)
        val tw = 9f * d
        val tx = tailX.coerceIn(r + tw * 0.6f, w - r - tw * 0.6f)
        tailPath.reset()
        tailPath.moveTo(tx - tw, bh - 1.5f * d)
        tailPath.lineTo(tx, h - d)
        tailPath.lineTo(tx + tw, bh - 1.5f * d)
        tailPath.close()
        c.drawPath(tailPath, bodyP)

        // ওপরের হালকা কাচের ঝিলিক
        rect.set(d * 3f, d * 3f, w - d * 3f, bh * 0.46f)
        c.drawRoundRect(rect, r * 0.8f, r * 0.8f, glowP)

        // বর্ডার (লেজসহ)
        c.drawPath(bodyPath, lineP)

        // লেখা
        val hasSub = sub.isNotEmpty()
        var size = bh * (if (hasSub) 0.42f else 0.52f)
        textP.textSize = size
        val maxW = w * 0.82f
        val mw = textP.measureText(text)
        if (mw > maxW && mw > 0f) {
            size = size * maxW / mw
            textP.textSize = size
        }
        val cy = if (hasSub) {
            bh * 0.46f - (textP.ascent() + textP.descent()) / 2f
        } else {
            bh / 2f - (textP.ascent() + textP.descent()) / 2f
        }
        c.drawText(text, w / 2f, cy, textP)
        if (hasSub) {
            subP.textSize = bh * 0.24f
            c.drawText(sub, w / 2f, bh - 6f * d - subP.descent(), subP)
        }
    }
}

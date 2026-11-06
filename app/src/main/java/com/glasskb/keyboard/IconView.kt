package com.glasskb.keyboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

/**
 * টুলবার ও ইমোজি ট্যাবের সব আইকন এখানে আঁকা হয় (রেফারেন্স ছবির মতো সাধারণ রেখার আইকন)।
 * ২৪×২৪ গ্রিডে আঁকা, তাই যেকোনো সাইজে পরিষ্কার দেখায় এবং থিমের রঙে রঙিন হয়।
 */
class IconView(context: Context, k: IconView.Kind) : View(context) {

    enum class Kind {
        TOOLS, CLOSE, CLIP, NUM, GEAR, MIC,
        SEARCH, CLOCK, SMILE, FLOWER, CAKE, BALL, CAR, CROWN, HEART, TRIANGLE, FLAG
    }

    var kind: Kind = k
        set(v) { field = v; invalidate() }

    var color: Int = Color.WHITE
        set(v) { field = v; invalidate() }

    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = 2.1f
    }
    private val solid = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
        textSize = 9f
    }
    private val path = Path()
    private val r = RectF()

    override fun onDraw(c: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val s = minOf(w, h) * 0.58f
        if (s <= 0f) return
        val u = s / 24f
        line.color = color
        solid.color = color
        text.color = color
        c.save()
        c.translate((w - s) / 2f, (h - s) / 2f)
        c.scale(u, u)
        draw24(c)
        c.restore()
    }

    private fun rr(c: Canvas, l: Float, t: Float, rt: Float, b: Float, rad: Float, p: Paint) {
        r.set(l, t, rt, b)
        c.drawRoundRect(r, rad, rad, p)
    }

    private fun draw24(c: Canvas) {
        path.reset()
        when (kind) {
            Kind.TOOLS -> {
                rr(c, 3f, 3f, 10.5f, 10.5f, 2.5f, line)
                rr(c, 13.5f, 3f, 21f, 10.5f, 2.5f, line)
                rr(c, 3f, 13.5f, 10.5f, 21f, 2.5f, line)
                rr(c, 13.5f, 13.5f, 21f, 21f, 2.5f, line)
            }
            Kind.CLOSE -> {
                c.drawLine(5f, 5f, 19f, 19f, line)
                c.drawLine(19f, 5f, 5f, 19f, line)
            }
            Kind.CLIP -> {
                rr(c, 5f, 4.5f, 19f, 22f, 2.5f, line)
                rr(c, 9f, 2f, 15f, 6.5f, 1.8f, solid)
                c.drawLine(8.5f, 11.5f, 15.5f, 11.5f, line)
                c.drawLine(8.5f, 15.5f, 15.5f, 15.5f, line)
                c.drawLine(8.5f, 19f, 12.5f, 19f, line)
            }
            Kind.NUM -> {
                rr(c, 2.5f, 4.5f, 21.5f, 19.5f, 3.5f, line)
                c.drawText("123", 12f, 15.2f, text)
            }
            Kind.GEAR -> {
                c.drawCircle(12f, 12f, 6.4f, line)
                c.drawCircle(12f, 12f, 2.4f, line)
                for (i in 0..7) {
                    val a = Math.toRadians(i * 45.0)
                    val ca = cos(a).toFloat()
                    val sa = sin(a).toFloat()
                    c.drawLine(12f + ca * 7.6f, 12f + sa * 7.6f, 12f + ca * 10.2f, 12f + sa * 10.2f, line)
                }
            }
            Kind.MIC -> {
                rr(c, 8.8f, 2.5f, 15.2f, 14f, 3.2f, line)
                r.set(5.5f, 6.5f, 18.5f, 19.5f)
                c.drawArc(r, 0f, 180f, false, line)
                c.drawLine(12f, 19.5f, 12f, 22f, line)
                c.drawLine(8.5f, 22f, 15.5f, 22f, line)
            }
            Kind.SEARCH -> {
                c.drawCircle(10.5f, 10.5f, 6.8f, line)
                c.drawLine(15.6f, 15.6f, 21.5f, 21.5f, line)
            }
            Kind.CLOCK -> {
                c.drawCircle(12f, 12f, 9.5f, line)
                c.drawLine(12f, 12f, 12f, 6.5f, line)
                c.drawLine(12f, 12f, 16f, 14.2f, line)
            }
            Kind.SMILE -> {
                c.drawCircle(12f, 12f, 9.5f, line)
                c.drawCircle(8.7f, 10f, 1.3f, solid)
                c.drawCircle(15.3f, 10f, 1.3f, solid)
                r.set(7f, 7.5f, 17f, 17.5f)
                c.drawArc(r, 25f, 130f, false, line)
            }
            Kind.FLOWER -> {
                c.drawCircle(12f, 12f, 2.4f, solid)
                for (i in 0..5) {
                    val a = Math.toRadians(i * 60.0 - 90.0)
                    c.drawCircle(12f + cos(a).toFloat() * 6.3f, 12f + sin(a).toFloat() * 6.3f, 3.3f, line)
                }
            }
            Kind.CAKE -> {
                rr(c, 3.5f, 12.5f, 20.5f, 21.5f, 2.2f, line)
                c.drawLine(3.8f, 17f, 20.2f, 17f, line)
                c.drawLine(8f, 12.5f, 8f, 8.5f, line)
                c.drawLine(12f, 12.5f, 12f, 8f, line)
                c.drawLine(16f, 12.5f, 16f, 8.5f, line)
                c.drawCircle(8f, 6.3f, 1.2f, solid)
                c.drawCircle(12f, 5.8f, 1.2f, solid)
                c.drawCircle(16f, 6.3f, 1.2f, solid)
            }
            Kind.BALL -> {
                c.drawCircle(12f, 12f, 9.5f, line)
                c.drawLine(12f, 2.5f, 12f, 21.5f, line)
                c.drawLine(2.5f, 12f, 21.5f, 12f, line)
                r.set(-11f, 3f, 7f, 21f)
                c.drawArc(r, -50f, 100f, false, line)
                r.set(17f, 3f, 35f, 21f)
                c.drawArc(r, 130f, 100f, false, line)
            }
            Kind.CAR -> {
                path.moveTo(3f, 17.5f)
                path.lineTo(3f, 13.5f)
                path.lineTo(5f, 9.5f)
                path.lineTo(8f, 7f)
                path.lineTo(16f, 7f)
                path.lineTo(19f, 9.5f)
                path.lineTo(21f, 13.5f)
                path.lineTo(21f, 17.5f)
                path.close()
                c.drawPath(path, line)
                c.drawLine(5f, 12.5f, 19f, 12.5f, line)
                c.drawCircle(7.5f, 18f, 2.3f, solid)
                c.drawCircle(16.5f, 18f, 2.3f, solid)
            }
            Kind.CROWN -> {
                path.moveTo(3.5f, 18.5f)
                path.lineTo(3f, 7f)
                path.lineTo(8f, 12f)
                path.lineTo(12f, 5f)
                path.lineTo(16f, 12f)
                path.lineTo(21f, 7f)
                path.lineTo(20.5f, 18.5f)
                path.close()
                c.drawPath(path, line)
                c.drawLine(3.5f, 21.5f, 20.5f, 21.5f, line)
            }
            Kind.HEART -> {
                path.moveTo(12f, 20.5f)
                path.cubicTo(4f, 14.5f, 2.5f, 9.5f, 6f, 6.3f)
                path.cubicTo(8.5f, 4.2f, 11f, 5.5f, 12f, 8f)
                path.cubicTo(13f, 5.5f, 15.5f, 4.2f, 18f, 6.3f)
                path.cubicTo(21.5f, 9.5f, 20f, 14.5f, 12f, 20.5f)
                path.close()
                c.drawPath(path, line)
            }
            Kind.TRIANGLE -> {
                path.moveTo(12f, 3.5f)
                path.lineTo(21.5f, 20f)
                path.lineTo(2.5f, 20f)
                path.close()
                c.drawPath(path, solid)
            }
            Kind.FLAG -> {
                c.drawLine(6f, 3f, 6f, 22f, line)
                path.moveTo(6f, 4.5f)
                path.lineTo(19.5f, 4.5f)
                path.lineTo(16f, 9.5f)
                path.lineTo(19.5f, 14.5f)
                path.lineTo(6f, 14.5f)
                c.drawPath(path, line)
            }
        }
    }
}

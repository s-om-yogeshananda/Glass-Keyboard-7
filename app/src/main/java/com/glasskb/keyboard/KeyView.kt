package com.glasskb.keyboard

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import kotlin.math.abs

/**
 * গ্লাস-স্টাইল একটি কী। নিজেই গ্রেডিয়েন্ট, বর্ডার ও টেক্সট আঁকে।
 * কী-এর মাঝে ফাঁকা জায়গা (মার্জিন) নেই — ফাঁকটা ভিতরে আঁকা হয় (inset),
 * তাই আঙুল যেখানেই পড়ুক কোনো না কোনো কী ধরবে। এতে মিস-টাচ কমে।
 */
open class KeyView(context: Context, private val th: KbTheme) : View(context) {

    enum class Icon { NONE, DELETE, SHIFT, ENTER }

    interface Host {
        fun keyPressed(k: KeyView)
        fun keyReleased(k: KeyView)
        fun altShow(k: KeyView, list: List<String>)
        fun altMove(k: KeyView, screenX: Float)
        fun altEnd(k: KeyView, commit: Boolean)
    }

    var host: Host? = null
    var onTap: (() -> Unit)? = null
    var onLong: (() -> Unit)? = null
    /** স্পেসবার স্লাইড: dir = +1 ডানে, -1 বামে */
    var onSwipe: ((Int) -> Unit)? = null
    /** একাধিক অপশন থাকলে লং-প্রেসে পপ-আপ লিস্ট দেখায় */
    var altList: List<String>? = null
    var repeatable = false
    var previewable = false
    var swipeable = false

    var label: String = ""
        set(v) { field = v; invalidate() }
    var hint: String = ""
        set(v) { field = v; invalidate() }
    var altHint: String = ""
        set(v) { field = v; invalidate() }
    var subHint: String = ""
        set(v) { field = v; invalidate() }
    /** স্লাইডের সময় যে ভাষার নাম ঢুকবে: ডানে টানলে altRight (পরের ভাষা), বামে টানলে altLeft (আগের ভাষা) */
    var altRight: String = ""
    var altLeft: String = ""
    var icon: Icon = Icon.NONE
        set(v) { field = v; invalidate() }
    var textScale: Float = 1f
        set(v) { field = v; invalidate() }
    /** 0 = সাধারণ, 1 = বিশেষ কী, 2 = অ্যাকসেন্ট */
    var style: Int = 0
        set(v) { field = v; updateShader(); invalidate() }
    var active: Boolean = false
        set(v) { field = v; invalidate() }

    private var isDown = false
    var longFired = false
        private set
    private var altShown = false
    private var dragging = false
    private var crossed = false
    private var downX = 0f
    private var dragDx = 0f
    private var settleP = 0f
    private var settleDir = 0
    private var outgoing = ""
    private var settling = false
    private var repeats = 0
    private var cancelled = false
    private var anim: ValueAnimator? = null

    /** লং-প্রেস, স্লাইড বা অল্ট-লিস্ট চালু থাকলে এই কী থেকে অন্য কী-তে সরানো যাবে না */
    val locked: Boolean get() = longFired || dragging || altShown

    private val density = resources.displayMetrics.density
    private val inset = 2.5f * density
    private val thr = 38f * density
    private val slop = 8f * density
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(0x2E, 0, 0, 0) }
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.style = Paint.Style.STROKE
        strokeWidth = density
    }
    private val txt = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val hintP = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.RIGHT }
    private val altP = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.LEFT }
    private val subP = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val overlay = Paint(Paint.ANTI_ALIAS_FLAG)
    private val iconP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val rect = RectF()
    private val shadowRect = RectF()
    private val path = Path()

    private val longRun = Runnable {
        longFired = true
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        val list = altList
        if (list != null && list.isNotEmpty()) {
            altShown = true
            host?.altShow(this, list)
        } else {
            onLong?.invoke()
        }
    }

    private val repeatRun = object : Runnable {
        override fun run() {
            onTap?.invoke()
            repeats++
            postDelayed(this, if (repeats > 12) 32L else 55L)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateShader()
    }

    private fun updateShader() {
        if (height <= 0) return
        val top: Int
        val bottom: Int
        when (style) {
            1 -> { top = th.specialTop; bottom = th.specialBottom }
            2 -> { top = th.accentStart; bottom = th.accentEnd }
            else -> { top = th.keyTop; bottom = th.keyBottom }
        }
        val h = height.toFloat()
        fill.shader = LinearGradient(0f, 0f, 0f, h, top, bottom, Shader.TileMode.CLAMP)
        val s = th.keyStroke
        val faint = (s and 0x00FFFFFF) or (((s ushr 24) / 3) shl 24)
        stroke.shader = LinearGradient(0f, 0f, 0f, h, s, faint, Shader.TileMode.CLAMP)
    }

    override fun onDraw(c: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        rect.set(inset, inset, w - inset, h - inset)
        val r = minOf(w - 2 * inset, h - 2 * inset) * 0.17f
        shadowRect.set(rect)
        shadowRect.offset(0f, density * 1.2f)
        c.drawRoundRect(shadowRect, r, r, shadowP)
        c.drawRoundRect(rect, r, r, fill)
        if (active) {
            overlay.color = Color.argb(0x99, Color.red(th.accentStart), Color.green(th.accentStart), Color.blue(th.accentStart))
            c.drawRoundRect(rect, r, r, overlay)
        }
        if (isDown) {
            overlay.color = th.press
            c.drawRoundRect(rect, r, r, overlay)
        }
        c.drawRoundRect(rect, r, r, stroke)

        if (icon != Icon.NONE) {
            drawIcon(c, w, h)
        } else if (label.isNotEmpty()) {
            txt.color = th.text
            val size = h * 0.38f * textScale
            txt.textSize = size
            val mw = txt.measureText(label)
            val maxW = w * 0.86f
            if (mw > maxW) txt.textSize = size * maxW / mw
            val shift = if (subHint.isNotEmpty()) h * 0.07f else 0f
            val cy = h / 2f - (txt.ascent() + txt.descent()) / 2f - shift
            if (swipeable && (dragDx != 0f || settling)) {
                drawSwipeLabels(c, w, cy)
            } else {
                c.drawText(label, w / 2f, cy, txt)
            }
        }
        if (swipeable && dragDx == 0f && !settling) {
            // স্লাইড করা যায় বোঝাতে দু'পাশে ছোট তীর
            val ts = h * 0.11f
            val ty = h / 2f
            iconP.style = Paint.Style.FILL
            iconP.color = (th.hint and 0x00FFFFFF) or 0x99000000.toInt()
            path.reset()
            val lx = w * 0.17f
            path.moveTo(lx - ts, ty)
            path.lineTo(lx + ts * 0.8f, ty - ts)
            path.lineTo(lx + ts * 0.8f, ty + ts)
            path.close()
            c.drawPath(path, iconP)
            path.reset()
            val rx = w * 0.83f
            path.moveTo(rx + ts, ty)
            path.lineTo(rx - ts * 0.8f, ty - ts)
            path.lineTo(rx - ts * 0.8f, ty + ts)
            path.close()
            c.drawPath(path, iconP)
        }
        if (hint.isNotEmpty()) {
            hintP.color = th.hint
            hintP.textSize = h * 0.22f
            c.drawText(hint, w - inset - h * 0.08f, inset + h * 0.06f + hintP.textSize * 0.85f, hintP)
        }
        if (altHint.isNotEmpty()) {
            altP.color = th.hint
            altP.textSize = h * 0.22f
            if (hint.isNotEmpty()) {
                c.drawText(altHint, inset + h * 0.08f, inset + h * 0.06f + altP.textSize * 0.85f, altP)
            } else {
                hintP.color = th.hint
                hintP.textSize = h * 0.22f
                c.drawText(altHint, w - inset - h * 0.08f, inset + h * 0.06f + hintP.textSize * 0.85f, hintP)
            }
        }
        if (subHint.isNotEmpty()) {
            subP.color = th.hint
            subP.textSize = h * 0.18f
            c.drawText(subHint, w / 2f, h - inset - h * 0.06f, subP)
        }
    }

    private fun smooth(p: Float): Float {
        val x = p.coerceIn(0f, 1f)
        return x * x * (3f - 2f * x)
    }

    /** স্পেসবারে স্লাইডের সময় পুরোনো ভাষা আঙুলের সাথে সরে যায়, নতুন ভাষা উল্টো দিক থেকে মসৃণভাবে ঢোকে */
    private fun drawSwipeLabels(c: Canvas, w: Float, cy: Float) {
        if (dragDx != 0f) {
            val p = smooth(abs(dragDx) / thr)
            val sgn = if (dragDx > 0f) 1f else -1f
            val cx = w / 2f + dragDx * 0.55f
            txt.alpha = (255 * (1f - p)).toInt()
            c.drawText(label, cx, cy, txt)
            val incoming = if (dragDx > 0f) altRight else altLeft
            if (incoming.isNotEmpty()) {
                txt.alpha = (255 * p).toInt()
                c.drawText(incoming, w / 2f - sgn * w * 0.36f * (1f - p), cy, txt)
            }
            txt.alpha = 255
        } else {
            // ছেড়ে দেওয়ার পর: পুরোনো নাম বেরিয়ে যায়, নতুন নাম মাঝে এসে থামে
            val p = smooth(settleP)
            val oldX = w / 2f + settleDir * w * 0.42f * p
            val newX = w / 2f - settleDir * w * 0.42f * (1f - p)
            if (outgoing.isNotEmpty() && p < 1f) {
                txt.alpha = (255 * (1f - p)).toInt()
                c.drawText(outgoing, oldX, cy, txt)
            }
            txt.alpha = (255 * p).toInt()
            c.drawText(label, newX, cy, txt)
            txt.alpha = 255
        }
    }

    private fun drawIcon(c: Canvas, w: Float, h: Float) {
        val s = h * 0.19f
        val cx = w / 2f
        val cy = h / 2f - (if (subHint.isNotEmpty()) h * 0.05f else 0f)
        iconP.color = th.text
        iconP.strokeWidth = h * 0.06f
        iconP.style = Paint.Style.STROKE
        path.reset()
        when (icon) {
            Icon.DELETE -> {
                path.moveTo(cx - s * 1.5f, cy)
                path.lineTo(cx - s * 0.6f, cy - s)
                path.lineTo(cx + s * 1.3f, cy - s)
                path.lineTo(cx + s * 1.3f, cy + s)
                path.lineTo(cx - s * 0.6f, cy + s)
                path.close()
                c.drawPath(path, iconP)
                c.drawLine(cx - s * 0.2f, cy - s * 0.45f, cx + s * 0.75f, cy + s * 0.45f, iconP)
                c.drawLine(cx - s * 0.2f, cy + s * 0.45f, cx + s * 0.75f, cy - s * 0.45f, iconP)
            }
            Icon.SHIFT -> {
                path.moveTo(cx, cy - s * 1.1f)
                path.lineTo(cx + s * 1.1f, cy)
                path.lineTo(cx + s * 0.45f, cy)
                path.lineTo(cx + s * 0.45f, cy + s * 0.9f)
                path.lineTo(cx - s * 0.45f, cy + s * 0.9f)
                path.lineTo(cx - s * 0.45f, cy)
                path.lineTo(cx - s * 1.1f, cy)
                path.close()
                iconP.style = if (active) Paint.Style.FILL_AND_STROKE else Paint.Style.STROKE
                c.drawPath(path, iconP)
            }
            Icon.ENTER -> {
                path.moveTo(cx + s * 1.2f, cy - s * 0.9f)
                path.lineTo(cx + s * 1.2f, cy + s * 0.2f)
                path.lineTo(cx - s * 1.2f, cy + s * 0.2f)
                c.drawPath(path, iconP)
                c.drawLine(cx - s * 1.2f, cy + s * 0.2f, cx - s * 0.5f, cy - s * 0.4f, iconP)
                c.drawLine(cx - s * 1.2f, cy + s * 0.2f, cx - s * 0.5f, cy + s * 0.8f, iconP)
            }
            else -> {}
        }
    }

    // ------------------------------------------------------------ প্রেস / রিলিজ লজিক

    fun press(screenX: Float) {
        longFired = false
        altShown = false
        dragging = false
        crossed = false
        downX = screenX
        repeats = 0
        isDown = true
        invalidate()
        animate().cancel()
        animate().scaleX(0.94f).scaleY(0.94f).setDuration(50).start()
        host?.keyPressed(this)
        if (repeatable) {
            onTap?.invoke()
            postDelayed(repeatRun, 380)
        } else if (!swipeable && (onLong != null || (altList?.size ?: 0) >= 1)) {
            postDelayed(longRun, 380)
        }
    }

    fun move(screenX: Float) {
        if (altShown) {
            host?.altMove(this, screenX)
            return
        }
        if (swipeable) {
            val dx = screenX - downX
            if (!dragging && abs(dx) > slop) {
                dragging = true
                crossed = false
                anim?.cancel()
                host?.keyReleased(this)
            }
            if (dragging) {
                dragDx = dx
                if (!crossed && abs(dx) >= thr) {
                    crossed = true
                    performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                } else if (crossed && abs(dx) < thr * 0.85f) {
                    crossed = false
                }
                invalidate()
            }
        }
    }

    fun lift(fire: Boolean) {
        removeCallbacks(longRun)
        removeCallbacks(repeatRun)
        val wasAlt = altShown
        altShown = false
        isDown = false
        invalidate()
        animate().cancel()
        animate().scaleX(1f).scaleY(1f).setDuration(130).setInterpolator(OvershootInterpolator(2f)).start()
        host?.keyReleased(this)
        if (wasAlt) {
            host?.altEnd(this, fire)
            return
        }
        if (dragging) {
            dragging = false
            finishDrag(fire)
            return
        }
        if (fire && !repeatable && !longFired) onTap?.invoke()
    }

    private fun finishDrag(fire: Boolean) {
        val dx = dragDx
        if (fire && abs(dx) >= thr) {
            val dir = if (dx > 0f) 1 else -1
            val before = label
            dragDx = 0f
            onSwipe?.invoke(dir)
            outgoing = before
            settleDir = dir
            settleP = 0f
            startAnim(0f, 1f, 300L) { settleP = it }
        } else {
            startAnim(dx, 0f, 220L) { dragDx = it }
        }
    }

    private fun startAnim(from: Float, to: Float, ms: Long, set: (Float) -> Unit) {
        anim?.cancel()
        set(from)
        val isSettle = from == 0f && to == 1f
        settling = isSettle
        val a = ValueAnimator.ofFloat(from, to)
        a.duration = ms
        a.interpolator = if (isSettle) LinearInterpolator() else DecelerateInterpolator(1.6f)
        a.addUpdateListener {
            set(it.animatedValue as Float)
            invalidate()
        }
        a.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (isSettle) {
                    settling = false
                    settleP = 0f
                    outgoing = ""
                    invalidate()
                }
            }

            override fun onAnimationCancel(animation: Animator) {
                if (isSettle) {
                    settling = false
                    settleP = 0f
                    outgoing = ""
                }
            }
        })
        a.start()
        anim = a
    }

    /** কী নিজে একা (রাউটার ছাড়া) থাকলে — যেমন ইমোজি প্যানেলের নিচের বাটন */
    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                cancelled = false
                parent?.requestDisallowInterceptTouchEvent(true)
                press(e.rawX)
            }
            MotionEvent.ACTION_MOVE -> {
                if (!cancelled) {
                    val s = 40f * density
                    if (!locked && (e.x < -s || e.x > width + s || e.y < -s || e.y > height + s)) {
                        cancelled = true
                        lift(false)
                    } else {
                        move(e.rawX)
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                if (!cancelled) lift(true)
            }
            MotionEvent.ACTION_CANCEL -> {
                if (!cancelled) lift(false)
            }
        }
        return true
    }
}

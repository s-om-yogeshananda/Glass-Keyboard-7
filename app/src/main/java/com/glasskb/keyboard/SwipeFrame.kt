package com.glasskb.keyboard

import android.content.Context
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import kotlin.math.abs

/**
 * ডানে-বামে টানলে পরের/আগের বিভাগে যায় (ইমোজি প্যানেলে ব্যবহৃত)।
 * ওপর-নিচ স্ক্রল ভেতরের ScrollView-ই সামলায়।
 */
class SwipeFrame(context: Context) : FrameLayout(context) {

    /** dir = +1 পরের বিভাগ (বামে টানলে), -1 আগের বিভাগ */
    var onSwipe: ((Int) -> Unit)? = null

    private val slop = ViewConfiguration.get(context).scaledTouchSlop
    private var downX = 0f
    private var downY = 0f
    private var dragging = false

    private fun content() = if (childCount > 0) getChildAt(0) else null

    private fun track(e: MotionEvent): Boolean {
        val dx = e.x - downX
        val dy = e.y - downY
        if (!dragging && abs(dx) > slop * 1.5f && abs(dx) > abs(dy) * 1.4f) {
            dragging = true
            parent?.requestDisallowInterceptTouchEvent(true)
        }
        return dragging
    }

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = e.x
                downY = e.y
                dragging = false
            }
            MotionEvent.ACTION_MOVE -> return track(e)
        }
        return dragging
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = e.x
                downY = e.y
                dragging = false
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (track(e)) {
                    val dx = e.x - downX
                    content()?.let {
                        it.translationX = dx * 0.5f
                        it.alpha = 1f - minOf(abs(dx) / width, 0.6f)
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (dragging) {
                    val dx = e.x - downX
                    dragging = false
                    if (e.actionMasked == MotionEvent.ACTION_UP && abs(dx) > width * 0.16f) {
                        onSwipe?.invoke(if (dx < 0) 1 else -1)
                    } else {
                        content()?.animate()?.translationX(0f)?.alpha(1f)?.setDuration(120)?.start()
                    }
                }
            }
        }
        return true
    }

    /** পুরোনো বিভাগ সরে যায়, তারপর end() চলে */
    fun slideOut(dir: Int, end: () -> Unit) {
        val c = content()
        if (c == null) {
            end()
            return
        }
        c.animate().translationX(-dir * width * 0.35f).alpha(0f).setDuration(90)
            .withEndAction { end() }.start()
    }

    /** নতুন বিভাগ উল্টো দিক থেকে ঢোকে */
    fun slideIn(dir: Int) {
        val c = content() ?: return
        c.translationX = dir * width * 0.35f
        c.alpha = 0f
        c.animate().translationX(0f).alpha(1f).setDuration(150).start()
    }
}

package com.glasskb.keyboard

import android.content.Context
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout

/**
 * কীবোর্ডের সব টাচ এখানে আসে। আঙুল ঠিক কোন কী-এর ওপর আছে সেটা এই ক্লাস নিজে হিসাব করে।
 *  - ফাঁকা জায়গায় পড়লে সবচেয়ে কাছের কী ধরে (মিস-টাচ কমে)
 *  - আঙুল সরিয়ে পাশের কী-তে নিলে সেই কী টাইপ হয় (ভুল কী চাপলে আঙুল না তুলেই ঠিক করা যায়)
 *  - একসাথে একাধিক আঙুল (দ্রুত টাইপ) ঠিকমতো চলে
 */
class KeyArea(context: Context) : FrameLayout(context) {

    var keysProvider: () -> List<KeyView> = { emptyList() }

    private val rects = ArrayList<Pair<KeyView, RectF>>()
    private var cacheOk = false
    private val active = HashMap<Int, KeyView>()
    private val loc = IntArray(2)
    private val density = resources.displayMetrics.density

    fun invalidateHits() {
        cacheOk = false
        releaseAll()
    }

    private fun releaseAll() {
        val list = ArrayList(active.values)
        active.clear()
        for (k in list) k.lift(false)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        cacheOk = false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean = true

    private fun ensureCache() {
        if (cacheOk) return
        rects.clear()
        for (k in keysProvider()) {
            if (!k.isShown || k.width == 0) continue
            var l = 0f
            var t = 0f
            var v: View = k
            while (v !== this) {
                l += v.left
                t += v.top
                val p = v.parent
                if (p is View) v = p else break
            }
            rects.add(Pair(k, RectF(l, t, l + k.width, t + k.height)))
        }
        cacheOk = true
    }

    private fun rectOf(k: KeyView): RectF? {
        for (p in rects) if (p.first === k) return p.second
        return null
    }

    private fun hit(x: Float, y: Float): KeyView? {
        ensureCache()
        var best: KeyView? = null
        var bd = Float.MAX_VALUE
        for (p in rects) {
            val r = p.second
            if (r.contains(x, y)) return p.first
            val dx = maxOf(maxOf(r.left - x, 0f), x - r.right)
            val dy = maxOf(maxOf(r.top - y, 0f), y - r.bottom)
            val d = dx * dx + dy * dy
            if (d < bd) {
                bd = d
                best = p.first
            }
        }
        val lim = 28f * density
        return if (bd <= lim * lim) best else null
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val i = e.actionIndex
                val id = e.getPointerId(i)
                getLocationOnScreen(loc)
                active.remove(id)?.lift(false)
                val k = hit(e.getX(i), e.getY(i))
                if (k != null) {
                    active[id] = k
                    k.press(loc[0] + e.getX(i))
                }
            }
            MotionEvent.ACTION_MOVE -> {
                getLocationOnScreen(loc)
                for (i in 0 until e.pointerCount) {
                    val id = e.getPointerId(i)
                    val k0 = active[id] ?: continue
                    val x = e.getX(i)
                    val y = e.getY(i)
                    val sx = loc[0] + x
                    if (k0.locked || k0.swipeable) {
                        k0.move(sx)
                        continue
                    }
                    ensureCache()
                    val r0 = rectOf(k0) ?: continue
                    val m = 7f * density
                    if (x < r0.left - m || x > r0.right + m || y < r0.top - m || y > r0.bottom + m) {
                        val k1 = hit(x, y)
                        if (k1 == null) {
                            active.remove(id)
                            k0.lift(false)
                        } else if (k1 !== k0) {
                            k0.lift(false)
                            active[id] = k1
                            k1.press(sx)
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val id = e.getPointerId(e.actionIndex)
                active.remove(id)?.lift(true)
            }
            MotionEvent.ACTION_CANCEL -> releaseAll()
        }
        return true
    }
}

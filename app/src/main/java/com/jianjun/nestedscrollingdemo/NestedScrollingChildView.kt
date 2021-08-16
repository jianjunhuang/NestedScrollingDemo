package com.jianjun.nestedscrollingdemo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.Scroller
import androidx.core.view.NestedScrollingChild3
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.ViewCompat
import kotlin.math.abs


class NestedScrollingChildView : View, NestedScrollingChild3 {

    private val childHelper = NestedScrollingChildHelper(this)
    private val imageRectF = RectF()
    private var image: Bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)

    private val offset = IntArray(2)
    private val consumed = IntArray(2)

    private var maxFlingVelocity = 0
    private var minFlingVelocity = 0
    private val scroller = Scroller(context)

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, -1)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        childHelper.isNestedScrollingEnabled = true
        val viewConfig = ViewConfiguration.get(context)

        maxFlingVelocity = viewConfig.scaledMaximumFlingVelocity
        minFlingVelocity = viewConfig.scaledMinimumFlingVelocity
    }

    override fun startNestedScroll(axes: Int, type: Int): Boolean {
        Log.i(TAG, "startNestedScroll: ")
        return childHelper.startNestedScroll(axes, type)
    }

    override fun stopNestedScroll(type: Int) {
        Log.i(TAG, "stopNestedScroll: ")
        childHelper.stopNestedScroll(type)
    }

    override fun hasNestedScrollingParent(type: Int): Boolean {
        Log.i(TAG, "hasNestedScrollingParent: ")
        return childHelper.hasNestedScrollingParent(type)
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?,
        type: Int,
        consumed: IntArray
    ) {
        Log.i(TAG, "dispatchNestedScroll: 1")
        childHelper.dispatchNestedScroll(
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            offsetInWindow,
            type,
            consumed
        )
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?,
        type: Int
    ): Boolean {
        Log.i(TAG, "dispatchNestedScroll: 2")
        return childHelper.dispatchNestedScroll(
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            offsetInWindow,
            type
        )
    }

    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?,
        type: Int
    ): Boolean {
        Log.i(TAG, "dispatchNestedPreScroll: ")
        return childHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type)
    }

    private var lastY = 0f
    private var velocityTracker: VelocityTracker? = null
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (velocityTracker == null)
            velocityTracker = VelocityTracker.obtain()

        velocityTracker?.addMovement(event)
        cancelFling()
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastY = event.y
                startNestedScroll(
                    ViewCompat.SCROLL_AXIS_VERTICAL,
                    ViewCompat.TYPE_TOUCH
                )
            }
            MotionEvent.ACTION_MOVE -> {
                var dy = lastY - event.y
                //ask parent weather consume
                if (dispatchNestedPreScroll(
                        0,
                        dy.toInt(),
                        consumed,
                        offset,
                        ViewCompat.TYPE_TOUCH
                    )
                ) {
                    Log.i(TAG, "onTouchEvent: preScrolled")
                    dy -= consumed[1]
                }
                //scroll yourself
                val consumedY = scrollYInternal(dy.toInt())
                postInvalidate()
                Log.i(TAG, "onTouchEvent: dy = $dy")
                Log.i(TAG, "onTouchEvent: before dispatch ${consumedY}, ${dy - consumedY}")
                lastY = event.y
                if (dispatchNestedScroll(
                        0,
                        consumedY.toInt(),
                        0,
                        (dy - consumedY).toInt(),
                        null,
                        ViewCompat.TYPE_TOUCH
                    )
                ) {
                    return false
                }
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                stopNestedScroll(ViewCompat.TYPE_TOUCH)
                //判断是否需要惯性滑动
                velocityTracker?.computeCurrentVelocity(1000, maxFlingVelocity.toFloat())
                fling(velocityTracker?.xVelocity ?: 0f, velocityTracker?.yVelocity ?: 0f)
                velocityTracker?.clear()
            }
        }
        return true
    }

    private fun fling(xVelocity: Float, yVelocity: Float) {
        Log.i(TAG, "fling: ${abs(yVelocity)}, ${abs(yVelocity) < minFlingVelocity}")
        if (abs(yVelocity) < minFlingVelocity) {
            return
        }

        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_NON_TOUCH)
        val velocityY = Math.max(-maxFlingVelocity, Math.min(yVelocity.toInt(), maxFlingVelocity))
        doFling(xVelocity.toInt(), velocityY)
    }

    private fun doFling(velocityX: Int, velocityY: Int) {
        Log.i(TAG, "doFling: $velocityX, $velocityY")
        isFling = true
        scroller.fling(
            0,
            0,
            velocityX,
            velocityY,
            Int.MIN_VALUE,
            Int.MAX_VALUE,
            Int.MIN_VALUE,
            Int.MAX_VALUE
        )
        postInvalidate()
    }

    override fun canScrollVertically(direction: Int): Boolean {
        return if (direction > 0) {
            imageRectF.bottom > height.toFloat()
        } else {
            imageRectF.top < 0f
        }
        return super.canScrollVertically(direction)
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        imageRectF.set(0f, 0f, width.toFloat(), height * 2f)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawBitmap(image, null, imageRectF, null)
    }

    private var isFling = false

    private var lastFlingY = 0
    override fun computeScroll() {
        Log.i(TAG, "computeScroll: ${scroller.computeScrollOffset()} $isFling")
        if (scroller.computeScrollOffset() && isFling) {
            val flingY = scroller.currY
            var dy = lastFlingY - flingY

            Log.i(TAG, "computeScroll: $dy")
            lastFlingY = flingY

            consumed[1] = 0
            if (dispatchNestedPreScroll(0, dy, consumed, offset, ViewCompat.TYPE_NON_TOUCH)) {
                dy -= consumed[1]
            }
            //scroll yourself
            val consumedY = scrollYInternal(dy)
            dispatchNestedScroll(
                0,
                consumedY,
                0,
                (dy - consumedY),
                null,
                ViewCompat.TYPE_NON_TOUCH
            )
            postInvalidate()
        } else {
            stopNestedScroll(ViewCompat.TYPE_NON_TOUCH)
            cancelFling()
        }
    }

    private fun scrollYInternal(dy: Int): Int {
        var consumedY = 0
        if (dy > 0) {
            //scroll top
            if (imageRectF.bottom != height.toFloat()) {
                var imageDy = dy
                if (imageRectF.bottom - imageDy < height) {
                    imageDy = imageRectF.bottom.toInt() - height
                }
                consumedY = imageDy
                imageRectF.offset(0f, -imageDy.toFloat())
            }
        } else {
            if (imageRectF.top != 0f) {
                var imageDy = dy
                if (imageRectF.top - imageDy > 0) {
                    imageDy = imageRectF.top.toInt()
                }
                consumedY = imageDy
                imageRectF.offset(0f, -imageDy.toFloat())
            }
        }
        return consumedY
    }

    fun cancelFling() {
        isFling = false
        lastFlingY = 0
    }

    fun isFling(): Boolean {
        return isFling
    }

    companion object {
        private const val TAG = "NestedChild"
    }
}
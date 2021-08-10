package com.jianjun.nestedscrollingdemo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.view.NestedScrollingChild3
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.ViewCompat

class NestedScrollingChildView : View, NestedScrollingChild3 {

    private val childHelper = NestedScrollingChildHelper(this)
    private val imageRectF = RectF()
    private var image: Bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)

    private val offset = IntArray(2)
    private val consumed = IntArray(2)

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, -1)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        childHelper.isNestedScrollingEnabled = true
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
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastY = event.y
                childHelper.startNestedScroll(
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
                var consumedY = 0f
                Log.i(TAG, "onTouchEvent: dy = $dy")
                if (dy > 0) {
                    //scroll top
                    if (imageRectF.bottom != height.toFloat()) {
                        var imageDy = dy
                        if (imageRectF.bottom - imageDy < height) {
                            imageDy = imageRectF.bottom - height
                        }
                        consumedY = imageDy
                        Log.i(TAG, "onTouchEvent: imageDy=$imageDy")
                        imageRectF.offset(0f, -imageDy)
                        invalidate()
                    }
                } else {
                    if (imageRectF.top != 0f) {
                        var imageDy = dy
                        if (imageRectF.top - imageDy > 0) {
                            imageDy = imageRectF.top
                        }
                        consumedY = imageDy
                        Log.i(TAG, "onTouchEvent: imageDy=$imageDy")
                        imageRectF.offset(0f, -imageDy)
                        invalidate()
                    }
                }
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
            }
        }
        return true
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

    companion object {
        private const val TAG = "NestedChild"
    }
}
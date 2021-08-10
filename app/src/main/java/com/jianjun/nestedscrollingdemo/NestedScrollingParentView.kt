package com.jianjun.nestedscrollingdemo

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.NestedScrollingParent3
import androidx.core.view.NestedScrollingParentHelper
import androidx.core.view.ViewCompat
import androidx.core.view.children


class NestedScrollingParentView : LinearLayout, NestedScrollingParent3 {

    private val parentHelper = NestedScrollingParentHelper(this)

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, -1)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
    }

    override fun canScrollVertically(direction: Int): Boolean {
        return true
    }

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        Log.i(TAG, "onStartNestedScroll: ")
        return (axes and ViewCompat.SCROLL_AXIS_VERTICAL) !== 0
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        parentHelper.onNestedScrollAccepted(child, target, axes, type)
        Log.i(TAG, "onNestedScrollAccepted: ")
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        parentHelper.onStopNestedScroll(target, type)
        Log.i(TAG, "onStopNestedScroll: ")
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        Log.i(TAG, "onNestedScroll: $dyUnconsumed")
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
        Log.i(TAG, "onNestedScroll: $dyUnconsumed")
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        Log.i(
            TAG,
            "onNestedPreScroll: ${target::class.simpleName}, $dx, $dy, consumedX=${consumed[0]}, consumedY=${consumed[1]}"
        )
        if (target == getChildAt(1)) {
            // scroll up
            if (dy > 0 && scrollY < getChildAt(0).height) {
                val realDy = if (dy + scrollY > getChildAt(0).height) {
                    getChildAt(0).height - scrollY
                } else {
                    dy
                }
                scrollBy(0, realDy)
                consumed[1] = realDy
            } else if (dy < 0 && !target.canScrollVertically(-1) && scrollY > 0) {
                val realDy = if (dy + scrollY < 0) {
                    -scrollY
                } else {
                    dy
                }
                scrollBy(0, realDy)
                consumed[1] = realDy
            }
        } else {
            if (dy > 0 && scrollY < target.height && !target.canScrollVertically(1)) {
                val realDy = if (dy + scrollY > target.height) {
                    target.height - scrollY
                } else {
                    dy
                }
                scrollBy(0, realDy)
                consumed[1] = realDy
            } else if (dy < 0 && scrollY > 0) {
                val realDy = if (dy + scrollY < 0) {
                    -scrollY
                } else {
                    dy
                }
                scrollBy(0, realDy)
                consumed[1] = realDy
            }
        }
    }

    companion object {
        private const val TAG = "NestedParent"
    }
}
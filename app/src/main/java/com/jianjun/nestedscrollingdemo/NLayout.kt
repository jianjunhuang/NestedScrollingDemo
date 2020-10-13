package com.jianjun.nestedscrollingdemo

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.*
import androidx.recyclerview.widget.RecyclerView

class NLayout : LinearLayout, NestedScrollingParent2
//    , NestedScrollingChild2
{

    private val parentHelper = NestedScrollingParentHelper(this)
    private val childHelper = NestedScrollingChildHelper(this)

    private var topView: View? = null
    private var bottomView: View? = null
    private var bottomViewHeight = 0

    companion object {
        const val TAG = "NLayout"
    }

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        orientation = VERTICAL
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        topView?.layoutParams?.let {
            it.height = measuredHeight
            topView?.layoutParams = it
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bottomView?.let {
            bottomViewHeight = it.measuredHeight
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        topView = getChildAt(0)
        bottomView = getChildAt(1)
    }

    override fun scrollTo(x: Int, y: Int) {
        var dstY = y
        if (dstY < 0) {
            dstY = 0
        }
        if(dstY > bottomViewHeight){
            dstY = bottomViewHeight
        }
        super.scrollTo(x, dstY)
    }

    /** NestedScrollingParent2 start **/

    /**
     * 即将开始嵌套滑动，此时嵌套滑动尚未开始，由子控件的 startNestedScroll 方法调用
     *
     * @param child  嵌套滑动对应的父类的子类(因为嵌套滑动对于的父控件不一定是一级就能找到的，可能挑了两级父控件的父控件，child的辈分>=target)
     * @param target 具体嵌套滑动的那个子类
     * @param axes   嵌套滑动支持的滚动方向
     * @param type   嵌套滑动的类型，有两种ViewCompat.TYPE_NON_TOUCH fling效果,ViewCompat.TYPE_TOUCH 手势滑动
     * @return true 表示此父类开始接受嵌套滑动，只有true时候，才会执行下面的 onNestedScrollAccepted 等操作
     */
    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        Log.i(TAG, "onStartNestedScroll: ")
        topView?.let {
            if (it is RecyclerView) {
                it.stopScroll()
            }
        }
//        bottomView?.stopNestedScroll()
        return (axes and ViewCompat.SCROLL_AXIS_VERTICAL) != 0
    }

    /**
     * 当onStartNestedScroll返回为true时，也就是父控件接受嵌套滑动时，该方法才会调用
     *
     * @param child
     * @param target
     * @param axes
     * @param type
     */
    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        parentHelper.onNestedScrollAccepted(child, target, axes, type)
    }

    /**
     * 停止滑动
     *
     * @param target
     * @param type
     */
    override fun onStopNestedScroll(target: View, type: Int) {
        if (type == ViewCompat.TYPE_NON_TOUCH) {

        }
        parentHelper.onStopNestedScroll(target, type)
    }

    /**
     * 在 onNestedPreScroll 中，父控件消耗一部分距离之后，剩余的再次给子控件，
     * 子控件消耗之后，如果还有剩余，则把剩余的再次还给父控件
     *
     * @param target       具体嵌套滑动的那个子类
     * @param dxConsumed   水平方向嵌套滑动的子控件滑动的距离(消耗的距离)
     * @param dyConsumed   垂直方向嵌套滑动的子控件滑动的距离(消耗的距离)
     * @param dxUnconsumed 水平方向嵌套滑动的子控件未滑动的距离(未消耗的距离)
     * @param dyUnconsumed 垂直方向嵌套滑动的子控件未滑动的距离(未消耗的距离)
     */
    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
        Log.i(TAG, "onNestedScroll: ")
        if (dyUnconsumed < 0) {
            //对于向下滑动
            if (target == bottomView) {
                topView?.scrollBy(0, dyUnconsumed)
            }
        } else {

        }
    }


    /**
     * 在子控件开始滑动之前，会先调用父控件的此方法，由父控件先消耗一部分滑动距离，并且将消耗的距离存在consumed中，传递给子控件
     * 在嵌套滑动的子View未滑动之前
     * ，判断父view是否优先与子view处理(也就是父view可以先消耗，然后给子view消耗）
     *
     * @param target   具体嵌套滑动的那个子类
     * @param dx       水平方向嵌套滑动的子View想要变化的距离
     * @param dy       垂直方向嵌套滑动的子View想要变化的距离 dy<0向下滑动 dy>0 向上滑动
     * @param consumed 这个参数要我们在实现这个函数的时候指定，回头告诉子View当前父View消耗的距离
     *                 consumed[0] 水平消耗的距离，consumed[1] 垂直消耗的距离 好让子view做出相应的调整
     * @param type     滑动类型，ViewCompat.TYPE_NON_TOUCH fling效果,ViewCompat.TYPE_TOUCH 手势滑动
     */
    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        Log.i(TAG, "onNestedPreScroll: ")
        //这里不管手势滚动还是fling都处理

        //对于底部布局
        val hideBottom = dy < 0 && scrollY > 0
        val showBottom = (dy > 0 && !target.canScrollVertically(1)
                && !(topView?.canScrollVertically(1) ?: true)) && scrollY < bottomViewHeight
        val cunsumedBottom = hideBottom || showBottom

        Log.i(
            TAG, "\nonNestedPreScroll: scrollY -> $scrollY " +
                    "\nbottomViewHeight -> $bottomViewHeight" +
                    "\n$dy\n$showBottom"
        )

        if (cunsumedBottom) {
            scrollBy(0, dy)
            consumed[1] = dy
        }
    }

    override fun getNestedScrollAxes(): Int {
        return parentHelper.nestedScrollAxes
    }
    /** NestedScrollingParent2 end **/

//    /** NestedScrollingChild2 start **/
//    /**
//     * 开始滑动前调用，在惯性滑动和触摸滑动前都会进行调用，此方法一般在 onInterceptTouchEvent或者onTouch中，通知父类方法开始滑动
//     * 会调用父类方法的 onStartNestedScroll onNestedScrollAccepted 两个方法
//     *
//     * @param axes 滑动方向
//     * @param type 开始滑动的类型 the type of input which cause this scroll event
//     * @return 有父视图并且开始滑动，则返回true 实际上就是看parent的 onStartNestedScroll 方法
//     */
//    override fun startNestedScroll(axes: Int, type: Int): Boolean {
//        return (!(topView?.canScrollVertically(-1) ?: true))
//    }
//
//    override fun stopNestedScroll(type: Int) {
//        childHelper.stopNestedScroll(type)
//    }
//
//    /**
//     * 判断当前子控件是否拥有嵌套滑动的父控件
//     */
//    override fun hasNestedScrollingParent(type: Int): Boolean {
//        Log.i(TAG, "hasNestedScrollingParent: ")
//        return childHelper.hasNestedScrollingParent(type)
//    }
//
//    /**
//     * 在dispatchNestedPreScroll 之后进行调用
//     * 当滑动的距离父控件消耗后，父控件将剩余的距离再次交个子控件，
//     * 子控件再次消耗部分距离后，又继续将剩余的距离分发给父控件,由父控件判断是否消耗剩下的距离。
//     * 如果四个消耗的距离都是0，则表示没有神可以消耗的了，会直接返回false，否则会调用父控件的
//     * onNestedScroll 方法，父控件继续消耗剩余的距离
//     * 会调用父控件的
//     *
//     * @param dxConsumed     水平方向嵌套滑动的子控件滑动的距离(消耗的距离)    dx<0 向右滑动 dx>0 向左滑动 （保持和 RecycleView 一致）
//     * @param dyConsumed     垂直方向嵌套滑动的子控件滑动的距离(消耗的距离)    dy<0 向下滑动 dy>0 向上滑动 （保持和 RecycleView 一致）
//     * @param dxUnconsumed   水平方向嵌套滑动的子控件未滑动的距离(未消耗的距离)dx<0 向右滑动 dx>0 向左滑动 （保持和 RecycleView 一致）
//     * @param dyUnconsumed   垂直方向嵌套滑动的子控件未滑动的距离(未消耗的距离)dy<0 向下滑动 dy>0 向上滑动 （保持和 RecycleView 一致）
//     * @param offsetInWindow 子控件在当前window的偏移量
//     * @return 如果返回true, 表示父控件又继续消耗了
//     */
//    override fun dispatchNestedScroll(
//        dxConsumed: Int,
//        dyConsumed: Int,
//        dxUnconsumed: Int,
//        dyUnconsumed: Int,
//        offsetInWindow: IntArray?,
//        type: Int
//    ): Boolean {
//        Log.i(TAG, "dispatchNestedScroll: ")
//        return childHelper.dispatchNestedScroll(
//            dxConsumed,
//            dyConsumed,
//            dxUnconsumed,
//            dyUnconsumed,
//            offsetInWindow,
//            type
//        )
//    }
//
//    /**
//     * 子控件在开始滑动前，通知父控件开始滑动，同时由父控件先消耗滑动时间
//     * 在子View的onInterceptTouchEvent或者onTouch中，调用该方法通知父View滑动的距离
//     * 最终会调用父view的 onNestedPreScroll 方法
//     *
//     * @param dx             水平方向嵌套滑动的子控件想要变化的距离 dx<0 向右滑动 dx>0 向左滑动 （保持和 RecycleView 一致）
//     * @param dy             垂直方向嵌套滑动的子控件想要变化的距离 dy<0 向下滑动 dy>0 向上滑动 （保持和 RecycleView 一致）
//     * @param consumed       父控件消耗的距离，父控件消耗完成之后，剩余的才会给子控件，子控件需要使用consumed来进行实际滑动距离的处理
//     * @param offsetInWindow 子控件在当前window的偏移量
//     * @param type           滑动类型，ViewCompat.TYPE_NON_TOUCH fling效果,ViewCompat.TYPE_TOUCH 手势滑动
//     * @return true    表示父控件进行了滑动消耗，需要处理 consumed 的值，false表示父控件不对滑动距离进行消耗，可以不考虑consumed数据的处理，此时consumed中两个数据都应该为0
//     */
//    override fun dispatchNestedPreScroll(
//        dx: Int,
//        dy: Int,
//        consumed: IntArray?,
//        offsetInWindow: IntArray?,
//        type: Int
//    ): Boolean {
//        Log.i(TAG, "dispatchNestedPreScroll: ")
//        //手指向下滑动，且
//        val hideBottom = dy < 0 && scrollY >= bottomViewHeight
//        return (!(topView?.canScrollVertically(-1) ?: true))
//    }
//    /** NestedScrollingChild2 end **/
    override fun canScrollVertically(direction: Int): Boolean {
        topView?.let {
            if (direction < 0) {
                return it.canScrollVertically(-1)
            } else if (direction > 0) {
                return it.canScrollVertically(1) && scrollY >= 0
            }
        }
        return super.canScrollVertically(direction)
    }
}
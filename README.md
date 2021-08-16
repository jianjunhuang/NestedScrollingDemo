# NestedScrollingDemo
Demo for [NestedScrollingChild3](https://developer.android.com/reference/kotlin/androidx/core/view/NestedScrollingChild3?hl=en) and [NestedScrollingParent3](https://developer.android.com/reference/kotlin/androidx/core/view/NestedScrollingParent3?hl=en)

## What is `NestedScrollingParent3` & `NestedScrollingChild3`
`NestedScrollingParent3` 和 `NestedScrollingChild3` 是两个接口, 分别继承自 `NestedScrollingParent` 和 `NestedScrollingChild`。最初是在 Android 5.0 时推出，用于解决子 View 和父 View 嵌套滑动时相互配合的问题，可以向下兼容。
> 应该是因为 Material Design 多了许多嵌套滑动的逻辑，所以增加的。

## How
`NestedScrollingParent3` & `NestedScrollingChild3` 中有许多的方法，看着就感觉十分复杂。但其实只要你知道了它整个滑动配合的流程，很多代码就能够信手捏来了。

而 Parent 和 Child 之间的通信你也不用操心，Android 提供了 `NestedScrollingChildHelper` 和 `NestedScrollingParentHelper` 你只需要在部分接口方法中调用 helper 就可以了。

编写的流程大致如下：
1. `ViewGroup` 实现 `NestedScrollingParent3` 接口
   1. 创建 `NestedScrollingParentHelper`
   2. 在 `onNestedScrollAccepted()`， `onStopNestedScroll()` 方法中，调用 parentHelper 的同名方法
   3. 在 `onStartNestedScroll()` ，根据传入的方向，返回是否要配合
2. 子 `View` 实现 `NestedScrollingChild3` 接口
   1. 创建 `NestedScrollingChildHelper`
   2. 在 `startNestedScroll()`, `stopNestedScroll()`, `hasNestedScrollingParent()`, `dispatchNestedScroll()`, `dispatchNestedPreScroll()` 方法中，调用 childHelper 的同名方法。
3. 重写 `onTouchEvent()` 方法 
   1. `ACTION_DOWN` 时，调用 `startNestedScroll()` 方法，传入需要配合的滑动方向
   2. `ACTION_MOVE` 时
      1. 计算滑动距离
      2. 调用 `dispatchNestedPreScroll()` 方法，询问 Parent 是否需要先消耗部分滑动距离
      3. 在 Parent 的 `onNestedPreScroll()` 中处理，如果消耗了需要更新 `consumed` 数组
      4. 更新剩余的滑动距离
      5. Child 消耗剩余滑动距离，自己滑动.
      6. Child 调用 `dispatchNestedScroll()` 方法，传递 Child 消耗的，和剩余的滑动数据.
   3. `ACTION_UP`
      1. Child 调用 `stopNestedScroll()`

至此，一个简单的滑动嵌套就完成了。

如果想要实现 `fling` 的效果
1. 接着上述 `ACTION_UP` 的代码中， 调用 `startNestedScroll()` 方法， 然后利用 `Scroller` 开始 `fling`
2. 在 `computeScroll()` 方法中重复上述 `ACTION_MOVE` 到 `ACTION_UP` 的操作。 

需要注意，滑动过程和 fling 的过程，在调用接口的方法时，`type` 参数是不一样的。

## Why 3
> https://juejin.cn/post/6844903761060577294


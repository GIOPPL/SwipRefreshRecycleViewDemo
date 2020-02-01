package com.gioppl.swiprefreshrecycleviewdemo

import android.content.Context
import android.os.AsyncTask
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewConfiguration
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.lang.Thread.sleep


class RefreshableViewList(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs), OnTouchListener {
    private var pullLength = 0
    private val header: View
    private var rv_main: RecyclerView? = null
    private var headerLayoutParams: MarginLayoutParams? = null
    private var mId = -1
    private var hideHeaderHeight = 0
    private var currentStatus = StatusRefresh.DOWN_FINISH
    private var lastStatus = currentStatus
    private var yDown = 0f
    private var touchSlop = 0
    private var iv_triangle: ImageView? = null
    private var mListener: RefreshCallBack? = null
    private var maxPull=0;
    internal enum class StatusRefresh{
        DOWN_FINISH,DOWN_PULL,DOWN_REFRESHING,DOWN_UP
    }


    /**
     * 当前是否可以下拉，只有ListView滚动到头的时候才允许下拉
     */
    private var ableToPull = false

    init {
        header = LayoutInflater.from(context).inflate(R.layout.pull_to_refresh, null, true)
        orientation = VERTICAL
        iv_triangle = header.findViewById(R.id.iv_triangle) as ImageView
        addView(header, 0)
    }

    var firstHideHead = true//第一次加载时隐藏头部
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        hideHeaderHeight = -header.height//1080,945
        pullLength = hideHeaderHeight / 4 * 3
        headerLayoutParams = header.layoutParams as MarginLayoutParams
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop
        headerLayoutParams!!.topMargin = hideHeaderHeight
        rv_main = getChildAt(1) as RecyclerView
        rv_main!!.setOnTouchListener(this)
        if (firstHideHead) {

            header.layoutParams = headerLayoutParams
            firstHideHead = !firstHideHead
        }
    }
    var distance=0;
    override fun onTouch(view: View, event: MotionEvent): Boolean {

        setIsAbleToPull(event)
        if (ableToPull) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    yDown = event.rawY
                    Log.i("MoveAction", "Down:$yDown")
                }
                MotionEvent.ACTION_MOVE -> {
                    val yMove = event.rawY
                    distance = (yMove - yDown).toInt()
                    // 如果手指是下滑状态，并且下拉头是完全隐藏的，就屏蔽下拉事件
                    if (distance <= pullLength && headerLayoutParams!!.topMargin <= hideHeaderHeight) return false
                    if (distance < touchSlop) return false
                    if (distance>0){//下拉
                        val top = (distance / 1.2 + hideHeaderHeight).toInt()
                        if (top <= 0) {
                            currentStatus=StatusRefresh.DOWN_PULL
                            headerLayoutParams!!.topMargin = top
                        } else {
                            currentStatus=StatusRefresh.DOWN_REFRESHING
                            headerLayoutParams!!.topMargin = 0
                        }
                        header.layoutParams = headerLayoutParams
                    }else{//上拉
                        headerLayoutParams!!.topMargin = hideHeaderHeight
                        header.layoutParams = headerLayoutParams
                    }


                }
                MotionEvent.ACTION_UP ->{
                    if (distance>pullLength){
                        currentStatus==StatusRefresh.DOWN_REFRESHING
                    }else{
                        currentStatus==StatusRefresh.DOWN_FINISH
                    }
                    when(currentStatus){
                        StatusRefresh.DOWN_REFRESHING-> {
                            TriangelRotate()
                            startRefresh()
                        }
                        StatusRefresh.DOWN_FINISH->{
                            HideHeaderTask().execute()
                        }
                    }
                }
            }
            if (currentStatus == StatusRefresh.DOWN_PULL||currentStatus==StatusRefresh.DOWN_REFRESHING) {
                rv_main!!.focusable = View.NOT_FOCUSABLE
                rv_main!!.isFocusableInTouchMode = false
                lastStatus = currentStatus
                return true
            }
        }
        return false
    }

    var preDegres = 0f
    private fun TriangelRotate() {
        val pivotX = iv_triangle!!.width / 2.toFloat()
        val pivotY = (iv_triangle!!.height / 1.6).toFloat()
        val animation = RotateAnimation(0f, 120f, pivotX, pivotY)
        animation.duration = 50
        animation.repeatMode = Animation.RESTART
        animation.repeatCount = Animation.INFINITE
        preDegres = 0f
        val linearInterpolator = LinearInterpolator()
        animation.interpolator = linearInterpolator
        iv_triangle!!.startAnimation(animation)
    }

    private fun setIsAbleToPull(event: MotionEvent) {
        val firstChild = rv_main!!.getChildAt(0)
        if (firstChild != null) {
            val lm = rv_main!!.layoutManager as LinearLayoutManager
            val firstVisiblePosition = lm.findFirstVisibleItemPosition()
            if (firstVisiblePosition == 0 && firstChild.top == 0) {
                if (!ableToPull)
                    yDown = event.rawY;
                ableToPull = true;
            } else {
                if (headerLayoutParams!!.topMargin != hideHeaderHeight) {
                    headerLayoutParams!!.topMargin = hideHeaderHeight
                }
                ableToPull = true
            }
        } else {
            ableToPull = true
        }
    }

    override fun requestLayout() {
        super.requestLayout()
    }

    public fun finishRefresh() {
        HideHeaderTask().execute()
    }


    public fun startRefresh() {
        mListener!!.onRefresh()
    }

    public interface RefreshCallBack {
        fun onRefresh()
        fun onFinished()
    }

    public fun setOnRefreshListener(id: Int, refreshCallBack: RefreshCallBack) {
        mListener = refreshCallBack
        mId = id
    }
    internal inner class HideHeaderTask : AsyncTask<Void?, Int?, Int>() {
        override fun doInBackground(vararg params: Void?): Int {
            var topMargin = headerLayoutParams!!.topMargin
            while (true) {
                topMargin = topMargin - 30
                if (topMargin <= hideHeaderHeight) {
                    topMargin = hideHeaderHeight
                    break
                }
                publishProgress(topMargin)
                sleep(10)
            }
            return topMargin
        }

        override fun onProgressUpdate(vararg topMargin: Int?) {
            headerLayoutParams!!.topMargin = topMargin[0]!!
            header.layoutParams = headerLayoutParams
        }

        override fun onPostExecute(topMargin: Int) {
            headerLayoutParams!!.topMargin = topMargin
            header.layoutParams = headerLayoutParams
            currentStatus = StatusRefresh.DOWN_FINISH
            //完成刷新、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、
            iv_triangle!!.clearAnimation()
        }


    }
}
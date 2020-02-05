package com.gioppl.swiprefreshrecycleviewdemo

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.view.MotionEvent.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.lang.Thread.sleep


class HorizontalSlideLinearLayout : LinearLayout {
    private var mSwipView:ViewGroup?=null
    private var mContext:Context?=null
    private var tv_delete:TextView?=null
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initView(context!!)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(context!!)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        initView(context!!)
    }
    private var firstInitLayout=true
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)

        if (firstInitLayout){
            touchSlop = ViewConfiguration.get(context).scaledTouchSlop
            firstInitLayout=false
            deleteWidth=innerView!!.width
        }
        maxScrollRange=mSwipView!!.measuredWidth
    }
    private fun setViewWH(){
        val w=this.getMeasuredWidth()
        val h=this.getMeasuredHeight()
        innerView!!.layoutParams.width= w

    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setViewWH()
        measureChildren(measureWidth(widthMeasureSpec),heightMeasureSpec)
//        setMeasuredDimension(measureWidth(widthMeasureSpec), heightMeasureSpec);
    }


    private var innerView:View?=null
    private var downX=0f
    private var moveX=0f
    private var deleteWidth=0
    private var maxScrollRange=0
    private var innerViewLayoutParams: MarginLayoutParams? = null
    private val scrollSpend=30
    private var touchSlop = 0
    private var windowsStatus=WindowsStatus.CLOSE
    private fun initView(context:Context) {
        mContext=context
        mSwipView= LayoutInflater.from(context).inflate(R.layout.horizontal_swip,null,true) as ViewGroup?
        tv_delete=mSwipView!!.findViewById(R.id.tv_delete)
    }
    var firstAddView=true
    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)
        if (firstAddView){
            firstAddView=false
            innerView= getChildAt(0)
            innerViewLayoutParams=innerView!!.layoutParams as MarginLayoutParams
            addView(mSwipView,1)
            val view=mSwipView!!.getChildAt(0)
            view.setOnClickListener(object :View.OnClickListener{
                override fun onClick(v: View?) {
//                    horizontalSlideOnClickListener!!.OnClick(v!!)
                    Toast.makeText(context,"点击事件",Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
    var distance=0f
    var preMargin=0
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when(ev.action){
            ACTION_DOWN->{
                downX=ev.rawX
//                log("ActionDown margin:${innerViewLayoutParams!!.leftMargin}")
            }
            ACTION_MOVE->{
//                log("ActionMove margin:${innerViewLayoutParams!!.leftMargin}")
                windowsStatus=WindowsStatus.MOVE
                moveX=ev.rawX
                distance=downX-moveX
//                if (distance <= maxScrollRange/2 && innerViewLayoutParams!!.leftMargin <= -maxScrollRange) return false
                if (windowsStatus==WindowsStatus.CLOSE||windowsStatus==WindowsStatus.MOVE){
                    if (distance>=0&&distance<=maxScrollRange){//打开的时候滑动，不能超过最大滑动距离
                        innerViewLayoutParams!!.leftMargin=-distance.toInt()
                        innerView!!.layoutParams=innerViewLayoutParams
                    }
                }else if (windowsStatus==WindowsStatus.OPEN){
                    if (distance<=0&&distance>=maxScrollRange){
                        innerViewLayoutParams!!.leftMargin=-distance.toInt()
                        innerView!!.layoutParams=innerViewLayoutParams
                    }
                }
                preMargin= innerViewLayoutParams!!.leftMargin
            }
            ACTION_UP->{
//                log("ActionUp margin:${innerViewLayoutParams!!.leftMargin}")
                innerView!!.focusable = View.NOT_FOCUSABLE
                innerView!!.isFocusableInTouchMode = false
                if (windowsStatus==WindowsStatus.MOVE){
                    if (distance<(maxScrollRange/2)){
                        windowsStatus==WindowsStatus.CLOSE
                        CloseWindows().execute()
                    }else{
                        windowsStatus==WindowsStatus.OPEN
                        OpenWindows().execute()
//                        innerView!!.layout()
                    }
                }

            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    inner class CloseWindows:AsyncTask<Void?,Int?,Int?>(){
            override fun doInBackground(vararg params: Void?): Int? {
            var scrollWidth=innerViewLayoutParams!!.leftMargin
            while (true){
                scrollWidth-=scrollSpend
                if (scrollWidth<=0){
                    scrollWidth=0
                    break
                }
                publishProgress(scrollWidth)
                sleep(10)
            }
            return scrollWidth
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            innerViewLayoutParams!!.leftMargin= values[0]!!
            innerView!!.layoutParams=innerViewLayoutParams
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            innerViewLayoutParams!!.leftMargin=0
            innerView!!.layoutParams=innerViewLayoutParams
        }

    }
    inner class OpenWindows:AsyncTask<Void?,Int?,Int?>(){
        val distination=-maxScrollRange
        @SuppressLint("WrongThread")
        override fun doInBackground(vararg params: Void?): Int? {
            var scrollWidth=innerViewLayoutParams!!.leftMargin
//            log("打开前leftMargin:$scrollWidth")
            while (true){
                scrollWidth-=scrollSpend
                if (scrollWidth<=distination){
                    scrollWidth=distination
                    break;
                }
                publishProgress(scrollWidth)
                sleep(10)
            }
            return scrollWidth
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            innerViewLayoutParams!!.leftMargin= values[0]!!
            innerView!!.layoutParams=innerViewLayoutParams
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            innerViewLayoutParams!!.leftMargin=distination
            innerView!!.layoutParams=innerViewLayoutParams
        }

    }



    private fun log(text:String,arg0:String="bbbb"){
        Log.i(arg0,text)
    }
    private fun measureWidth(widthMeasureSpec: Int): Int {
        var result = 0 //结果
        val specMode = MeasureSpec.getMode(widthMeasureSpec)
        val specSize = MeasureSpec.getSize(widthMeasureSpec)

        when (specMode) {
            MeasureSpec.AT_MOST -> {
                result=MeasureSpec.makeMeasureSpec(specSize,specMode)
            }
            MeasureSpec.EXACTLY -> {
                result=MeasureSpec.makeMeasureSpec(specSize,MeasureSpec.UNSPECIFIED)
            }
            MeasureSpec.UNSPECIFIED -> {
                result=MeasureSpec.makeMeasureSpec(specSize,specMode)
            }
        }
        return result
    }
    enum class WindowsStatus{
        CLOSE,OPEN,MOVE
    }


    private var horizontalSlideOnClickListener:HorizontalSlideOnClickListener?=null
    public fun bindHorizontalSlideOnClickListener(horizontalSlideOnClickListener:HorizontalSlideOnClickListener){
        this.horizontalSlideOnClickListener=horizontalSlideOnClickListener
    }
    public interface HorizontalSlideOnClickListener{
        fun OnClick(view: View)
    }
}
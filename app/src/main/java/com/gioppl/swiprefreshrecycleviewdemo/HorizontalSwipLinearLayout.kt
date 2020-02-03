package com.gioppl.swiprefreshrecycleviewdemo

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView


class HorizontalSwipLinearLayout : LinearLayout {
    private var mSwipView:View?=null
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
            firstInitLayout=false
            deleteWidth=btn_variety!!.width
        }
    }
    private fun setViewWH(){
        val w=this.getMeasuredWidth()
        val h=this.getMeasuredHeight()
        log("父布局w:$w,h:$h")
        btn_variety!!.layoutParams.width= w
        log("子控件w:${btn_variety!!.width},h:${btn_variety!!.height}")

    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setViewWH()
        measureChildren(measureWidth(widthMeasureSpec),heightMeasureSpec)
//        setMeasuredDimension(measureWidth(widthMeasureSpec), heightMeasureSpec);
    }


    private var btn_variety:Button?=null
    private var preX=0f
    private var endX=0f
    private var deleteWidth=0
    private fun initView(context:Context) {
        mContext=context
        mSwipView=LayoutInflater.from(context).inflate(R.layout.horizontal_swip,null,true)
        tv_delete=mSwipView!!.findViewById(R.id.tv_delete)
    }
    var firstAddView=true
    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)
        if (firstAddView){
            firstAddView=false
            btn_variety= getChildAt(0) as Button?
            addView(mSwipView,1)
        }
    }
    var firstTouch=true
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when(ev.action){
            ACTION_DOWN->{
                preX=ev.rawX
                log("MoveAction:Down")
            }
            ACTION_MOVE->{
                endX=ev.rawX
                val distance=preX-endX
                this!!.scrollTo(distance.toInt(),0)
                
            }
            ACTION_UP->{
                log("MoveAction:up")
            }
        }
        return super.onInterceptTouchEvent(ev)
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
}
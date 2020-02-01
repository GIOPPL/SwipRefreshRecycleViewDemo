package com.gioppl.swiprefreshrecycleviewdemo;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class RefreshableViewList2 extends LinearLayout implements View.OnTouchListener {
    /**
     * 下拉状态
     */
    public static final int STATUS_PULL_TO_REFRESH = 0;

    /**
     * 释放立即刷新状态
     */
    public static final int STATUS_RELEASE_TO_REFRESH = 1;

    /**
     * 正在刷新状态
     */
    public static final int STATUS_REFRESHING = 2;

    /**
     * 刷新完成或未刷新状态
     */
    public static final int STATUS_REFRESH_FINISHED = 3;

    /**
     * 下拉头部回滚的速度
     */
    public static final int SCROLL_SPEED = -30;


    /**
     * 下拉的长度
     */

    private int pullLength;


    /**
     * 下拉刷新的回调接口
     */
//    private PullToRefreshListener mListener;


    /**
     * 下拉头的View
     */
    private View header;

    /**
     * 需要去下拉刷新的RecyclerView
     */
    private RecyclerView rv_main;

    /**
     * 刷新时显示的进度条
     */
    // private ProgressBar progressBar;


    //三角形
    private ImageView iv_triangle;

    /**
     * 指示下拉和释放的箭头
     */
    // private ImageView arrow;

    /**
     * 指示下拉和释放的文字描述
     */
    //private TextView description;


    /**
     * 下拉头的布局参数
     */
    private MarginLayoutParams headerLayoutParams;


    /**
     * 为了防止不同界面的下拉刷新在上次更新时间上互相有冲突，使用id来做区分
     */
    private int mId = -1;

    /**
     * 下拉头的高度
     */
    private int hideHeaderHeight;

    /**
     * 当前处理什么状态，可选值有STATUS_PULL_TO_REFRESH, STATUS_RELEASE_TO_REFRESH,
     * STATUS_REFRESHING 和 STATUS_REFRESH_FINISHED
     */
    private int currentStatus = STATUS_REFRESH_FINISHED;
    ;

    /**
     * 记录上一次的状态是什么，避免进行重复操作
     */
    private int lastStatus = currentStatus;

    /**
     * 手指按下时的屏幕纵坐标
     */
    private float yDown;

    /**
     * 在被判定为滚动之前用户手指可以移动的最大值。
     */
    private int touchSlop;

    /**
     * 是否已加载过一次layout，这里onLayout中的初始化只需加载一次
     */
    private boolean loadOnce;

    /**
     * 当前是否可以下拉，只有ListView滚动到头的时候才允许下拉
     */
    private boolean ableToPull;

    float startY = 0, endY = 0;

    public RefreshableViewList2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        header = LayoutInflater.from(context).inflate(R.layout.pull_to_refresh, null, true);
        this.setFocusable(false);
        iv_triangle = header.findViewById(R.id.iv_triangle);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setOrientation(VERTICAL);
        addView(header, 0);
    }


    /**
     * 进行一些关键性的初始化操作，比如：将下拉头向上偏移进行隐藏，注册touch事件。
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        hideHeaderHeight = -header.getHeight();
        headerLayoutParams = (MarginLayoutParams) header.getLayoutParams();
        headerLayoutParams.topMargin = hideHeaderHeight+10;
        header.setLayoutParams(headerLayoutParams);
        rv_main = (RecyclerView) this.getChildAt(1);
        rv_main.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                yDown = event.getRawY();
                Log.i("MoveAction", "Down:" + yDown);
                break;
            case MotionEvent.ACTION_MOVE:
                float yMove = event.getRawY();
                int distance = (int) (yMove - yDown);
                Log.i("MoveAction", "Move:" + distance);
                headerLayoutParams.topMargin = distance;
                header.setLayoutParams(headerLayoutParams);
                break;
            case MotionEvent.ACTION_UP:
                Log.i("MoveAction", endY + "UP");
                break;
        }
        return true;
    }

}

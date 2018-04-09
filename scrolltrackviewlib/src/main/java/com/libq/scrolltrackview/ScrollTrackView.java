package com.libq.scrolltrackview;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;

/**
 * 描述:滑动音轨View
 * author: libq
 * date2018/4/2 0002.
 */

public class ScrollTrackView extends HorizontalScrollView {

    private Handler mScrollHandler;

    private OnScrollTrackListener mOnScrollTrackListener;
    private OnProgressRunListener mProgressRunListener;

    private int mBackgroundColor = Color.LTGRAY;
    private int mForegroundColor = Color.BLUE;
    private int mSpaceSize = 6;
    private int mTrackItemWidth=16;
    private int mDelayTime = 20;//ms
    private int mTrackFragmentCount = 10;

    /**
     * 滚动状态:
     * IDLE=滚动停止
     * TOUCH_SCROLL=手指拖动滚动
     * FLING=滚动
     */
    enum ScrollStatus {IDLE,TOUCH_SCROLL,FLING}

    /**
     * 记录当前滚动的距离
     */
    private int currentX = -9999999;

    /**
     * 当前滚动状态
     */
    private ScrollStatus scrollStatus = ScrollStatus.IDLE;
    //--------------------------------
    private Track track;
    private boolean disableTouch;
    private TrackMoveController moveController;

    private int audioDuration;

    public ScrollTrackView(Context context) {
        super(context);
        initView(context);
    }

    public ScrollTrackView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ScrollTrackView);

        mBackgroundColor = typedArray.getColor(R.styleable.ScrollTrackView_background_color, mBackgroundColor);
        mForegroundColor = typedArray.getColor(R.styleable.ScrollTrackView_foreground_color, mForegroundColor);
        int spsize = typedArray.getDimensionPixelSize(R.styleable.ScrollTrackView_space_size, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, getContext().getResources().getDisplayMetrics()));
        mSpaceSize = spsize==0?mSpaceSize:spsize;
        int tiw = typedArray.getDimensionPixelSize(R.styleable.ScrollTrackView_track_item_width, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, getContext().getResources().getDisplayMetrics()));
        mTrackItemWidth = tiw==0?mTrackItemWidth:tiw;

        mTrackFragmentCount = typedArray.getInteger(R.styleable.ScrollTrackView_track_fragment_count,mTrackFragmentCount);
        mDelayTime = typedArray.getInteger(R.styleable.ScrollTrackView_delay_time,mDelayTime);

        typedArray.recycle();
        initView(context);
    }

    public ScrollTrackView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(final Context context){

        track = new Track(context);
        track.setBackgroundColorInt(mBackgroundColor);
        track.setForegroundColor(mForegroundColor);
        track.setSpaceSize(mSpaceSize);
        track.setTrackFragmentCount(mTrackFragmentCount);
        track.setTrackItemWidth(mTrackItemWidth);

        HorizontalScrollView.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(track,lp);



        moveController = new TrackMoveController(mDelayTime, new TrackMoveController.OnProgressChangeListener() {
            @Override
            public void onProgressChange(int progress) {
                Message msg = progressHandler.obtainMessage(1);
                msg.arg1 = progress;
                progressHandler.sendMessage(msg);
            }

            @Override
            public void onProgressStart() {
                if(mProgressRunListener !=null){
                    mProgressRunListener.onTrackRun();
                }
            }
        });

        post(new Runnable() {
            @Override
            public void run() {
                //可视的时候开始走进度
                moveController.setScrollTrackViewWidth(getWidth());
                startMove();
            }
        });



        mScrollHandler = new Handler();

        //滑动状态监听
        mOnScrollTrackListener = new OnScrollTrackListener() {
            @Override
            public void onScrollChanged(ScrollStatus scrollStatus) {
                switch (scrollStatus){
                    case IDLE:
                        if(moveController!=null){
                            moveController.setScrollTrackStartX(getScrollX());
                            moveController.continueRun();
                        }
                        if(mProgressRunListener!=null){
                            mProgressRunListener.onTrackStartTimeChange(getStartTime());
                        }

                        break;
                    case FLING:
                        break;
                    case TOUCH_SCROLL:
                        if(moveController!=null){
                            moveController.pause();
                        }
                        break;
                    default:
                        break;
                }

            }
        };


    }

    public void setTrackTemplateData(float[] data){
        if(track!=null&&data!=null){
            track.setTrackTemplateData(data);
        }
    }

    public void setTrackFragmentCount(int count){
        if(track!=null){
            track.setTrackFragmentCount(count);
        }
    }

    //-------------scroll control-----------------
    private interface OnScrollTrackListener {
        void onScrollChanged(ScrollStatus scrollStatus);
    }

    private boolean isScrollChange = false;
    /**
     * 滚动监听runnable 方便获取滑动状态
     */
    private Runnable scrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (getScrollX()==currentX) {
                //滚动停止,取消监听线程
                scrollStatus = ScrollStatus.IDLE;
                if (mOnScrollTrackListener !=null) {
                    mOnScrollTrackListener.onScrollChanged(scrollStatus);
                }
                mScrollHandler.removeCallbacks(this);
                return;
            } else {
                isScrollChange = true;
                //手指离开屏幕,但是view还在滚动
                scrollStatus = ScrollStatus.FLING;
                if(mOnScrollTrackListener !=null){
                    mOnScrollTrackListener.onScrollChanged(scrollStatus);
                }
            }
            currentX = getScrollX();
            //滚动监听间隔:milliseconds
            mScrollHandler.postDelayed(this, 20);
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                this.scrollStatus = ScrollStatus.TOUCH_SCROLL;
                mOnScrollTrackListener.onScrollChanged(scrollStatus);
                mScrollHandler.removeCallbacks(scrollRunnable);
                break;
            case MotionEvent.ACTION_UP:
                mScrollHandler.post(scrollRunnable);
                break;
        }
        return super.onTouchEvent(ev);
    }


    /**
     * 进度控制
     */
    Handler progressHandler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==1){
                track.setProgress(msg.arg1);
            }
        }
    };


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (disableTouch) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 开始
     */
    public void startMove() {
        disableTouch = true;
        if (moveController != null) {
            moveController.start();

        }
    }

    /**
     * 停止
     */
    public void stopMove() {
        disableTouch = false;
        if (moveController != null) {
            moveController.stop();
        }
    }

    /**
     * 轨道开始播放到轨道结束监听
     */
    public interface OnProgressRunListener {
        void onTrackRun();
        void onTrackStartTimeChange(int ms);

    }

    public void setOnProgressRunListener(OnProgressRunListener listener){
        mProgressRunListener = listener;
    }

    /**
     * 设置音频总时间
     */
    public void setDuration(int ms){
        audioDuration = ms;
    }

    /**
     * 获取歌曲开始时间 (毫秒)
     */
    public int getStartTime(){
        float rate = Math.abs(getScrollX())/(track.getWidth()*1f);
        Log.e("xxx","scroll x = " + getScrollX() +" track width = "+track.getWidth());
        return (int)(audioDuration * rate);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopMove();
    }
}







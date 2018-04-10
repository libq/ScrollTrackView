package com.libq.scrolltrackview;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
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
    private boolean isAutoRun = true;//是否自动跑进度
    private boolean isLoopRun = false;//是否循环跑进度
    private int mCutDuration = 10 * 1000;//裁剪区间，也就是控件左边，跑到右边的时间
    private float mSpeed= 10;

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

        isAutoRun = typedArray.getBoolean(R.styleable.ScrollTrackView_auto_run,isAutoRun);
        mTrackFragmentCount = typedArray.getInteger(R.styleable.ScrollTrackView_track_fragment_count,mTrackFragmentCount);
        mCutDuration = typedArray.getInteger(R.styleable.ScrollTrackView_cut_duration,mCutDuration);
        isLoopRun = typedArray.getBoolean(R.styleable.ScrollTrackView_loop_run,isLoopRun);

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
        setSmoothScrollingEnabled(false);



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
                    mProgressRunListener.onTrackStart(getStartTime());
                }
            }

            @Override
            public void onProgressEnd() {
                if(mProgressRunListener !=null){
                    mProgressRunListener.onTrackEnd();
                }
            }
        });

        post(new Runnable() {
            @Override
            public void run() {
                //可视的时候开始走进度
                moveController.setScrollTrackViewWidth(getWidth());
                mSpeed = ((getWidth()*1f)/(mCutDuration*1f));//根据时间和控件的宽度计算速度
                float delayTime = 1f/mSpeed;//根据速度来算走每个像素点需要多久时间
                moveController.setDelayTime(Math.round(delayTime));//四舍五入
                moveController.setLoopRun(isLoopRun);
                if(isAutoRun){
                    startMove();
                }

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

    public void setCutDuration(int cutDuration){
        mCutDuration = cutDuration;
    }

    /**
     * 设置循环播放
     * @param isLoop
     */
    public void setLoopRun(boolean isLoop){
        isLoopRun = isLoop;
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

    /*@Override
    public void fling(int velocity) {
        super.fling(velocity / 1000);
    }*/



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
     * 重新开始播放
     */
    public void restartMove(){
        disableTouch = true;

        if (moveController != null) {
            scrollTo(0,0);
            smoothScrollTo(0, 0);
            moveController.restart();
            if(mProgressRunListener!=null){
                mProgressRunListener.onTrackStartTimeChange(0);
            }
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


    public void pauseMove(){
        disableTouch = false;
        if (moveController != null) {
            moveController.pause();
        }
    }

    /**
     * 轨道开始播放到轨道结束监听
     */
    public interface OnProgressRunListener {
        void onTrackStart(int ms);
        void onTrackStartTimeChange(int ms);
        void onTrackEnd();

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
        return (int)(audioDuration * rate);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopMove();
    }
}







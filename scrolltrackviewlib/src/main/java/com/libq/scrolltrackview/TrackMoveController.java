package com.libq.scrolltrackview;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 描述:track移动控制器
 * author: libq
 * date2018/3/31 0031.
 */

public class TrackMoveController {
    private Timer mTimer;
    private int mDelayTime = 10;//ms
    private int mProgress = 0;
    private int mScrollTrackViewWidth;
    private int mScrollTrackStartX = 0;
    private OnProgressChangeListener mListener;
    private boolean isCanRun = true;
    public TrackMoveController(int delayTime){
        mDelayTime = delayTime;
    }
    public TrackMoveController(int delayTime, OnProgressChangeListener listener){
        mDelayTime = delayTime;
        mListener = listener;
    }

    public synchronized void start() {
        if (mTimer == null) {
            mTimer = new Timer();
            mListener.onProgressStart();
            mTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (isCanRun) {
                        //移动到最右边的时候，重新从启始位置开始移动
                        if((mProgress-mScrollTrackStartX) >= mScrollTrackViewWidth){
                            mProgress = mScrollTrackStartX;
                            mListener.onProgressStart();
                        }
                        mProgress ++;
                        if(mListener!=null){
                            mListener.onProgressChange(mProgress);
                        }
                    }
                }
            }, mDelayTime,10 );
        }
    }

    public boolean isRunning() {
        return mTimer != null;
    }


    public synchronized void stop() {
        if (isRunning()) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    public synchronized void pause(){
        if(isRunning()){
            isCanRun = false;
        }
    }

    public synchronized void continueRun(){
        isCanRun = true;
        mProgress = mScrollTrackStartX;
    }


    public int getProgress(){
        return mProgress;
    }

    public interface OnProgressChangeListener{
        void onProgressChange(int progress);
        void onProgressStart();
    }

    public void setOnProgressChangeListener(OnProgressChangeListener listener){
        mListener = listener;
    }


    public void setScrollTrackViewWidth(int mScrollTrackViewWidth) {
        this.mScrollTrackViewWidth = mScrollTrackViewWidth;
    }

    public void setScrollTrackStartX(int x){
        this.mScrollTrackStartX = x;
    }




}

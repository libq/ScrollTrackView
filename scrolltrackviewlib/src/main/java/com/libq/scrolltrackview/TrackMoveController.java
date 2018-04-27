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
    private long mDelayTime = 10;//ms
    private int mProgress = 0;
    private int mScrollTrackViewWidth;
    private int mScrollTrackStartX = 0;
    private OnProgressChangeListener mListener;
    private boolean isCanRun = true;
    private boolean isLoopRun = false;
    private boolean isStarted = true;
    public TrackMoveController(int delayTime){
        mDelayTime = delayTime;
    }
    public TrackMoveController(int delayTime, OnProgressChangeListener listener){
        mDelayTime = delayTime;
        mListener = listener;
    }

    public void setDelayTime(long ms){
        mDelayTime = ms;
    }
    public void setLoopRun(boolean loop){
        isLoopRun = loop;
    }

    public synchronized void start() {
        if (mTimer == null) {
            mTimer = new Timer();
            mListener.onProgressStart();
            mTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (isCanRun) {
                        if (isLoopRun) {
                            //移动到最右边的时候，重新从启始位置开始移动
                            if((mProgress-mScrollTrackStartX) >= mScrollTrackViewWidth){
                                mProgress = mScrollTrackStartX;
                                mListener.onProgressEnd();
                                mListener.onProgressStart();
                            }
                            if (isProgressContinue) {
                                mProgress ++;
                            }

                            if(mListener!=null){
                                mListener.onProgressChange(mProgress);
                            }

                        }else{
                            //onProgressStart 只执行一次
                            if(isStarted){
                                mListener.onProgressStart();
                                isStarted = false;
                            }

                            if(mListener!=null){
                                mListener.onProgressChange(mProgress);
                            }

                            if((mProgress-mScrollTrackStartX) >= mScrollTrackViewWidth){
                                mListener.onProgressEnd();
                            }else{
                                mProgress ++;
                            }

                        }

                        /*//移动到最右边的时候，重新从启始位置开始移动
                        if((mProgress-mScrollTrackStartX) >= mScrollTrackViewWidth){
                            mProgress = mScrollTrackStartX;
                            mListener.onProgressStart();
                        }
                        mProgress ++;
                        if(mListener!=null){
                            mListener.onProgressChange(mProgress);
                        }*/
                    }
                }
            }, 0,mDelayTime );//延时时间，间隔时间
        }else{
            isCanRun = true;
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

    public synchronized void restart(){
        stop();
        mScrollTrackStartX = 0;
        mProgress = 0;
        isCanRun = true;
        start();
    }

    private boolean isProgressContinue = true;
    //绘制到当前位置然后暂停进度
    public void setProgressContinue(boolean bPause){
        isProgressContinue = bPause;
    }


    public int getProgress(){
        return mProgress;
    }

    public interface OnProgressChangeListener{
        void onProgressChange(int progress);
        void onProgressStart();
        void onProgressEnd();
    }

    public void setOnProgressChangeListener(OnProgressChangeListener listener){
        mListener = listener;
    }


    public void setScrollTrackViewWidth(int mScrollTrackViewWidth) {
        this.mScrollTrackViewWidth = mScrollTrackViewWidth;
    }

    /**
     * 设置当前进度条位置
     * @param positionX px
     */
    public void setCurrentProgressPosition(int positionX){
        mProgress = mScrollTrackStartX + positionX;
    }

    public void setScrollTrackStartX(int x){
        this.mScrollTrackStartX = x;
    }




}

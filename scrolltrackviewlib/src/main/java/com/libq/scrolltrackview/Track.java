package com.libq.scrolltrackview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * describ:
 *      这个效果类似抖音的音频截取控件，控件滑动的时候从控件左边可视区域重新跑进度，
 *      进度到可视区域最右边的时候，重新从可视区域最左边开始跑进度
 *
 *      ps1：为方便绘制，我这里用一个浮点数组来设置一组竖的矩形的长度在控件高度中的比例。
 *           后面都是以这个数组当成模板，重复画track
 *
 * author:libq
 * data:2018
 */
public class Track extends View {

    private int trackWidth = 0;//track 宽

    private Paint slipPaint = null;
    private Paint maskPaint = null;
    private Paint mTrackPaint = null;
    private Paint borderPaint = null;

    private int borderWidth = 5;
    private int progress = 0;

    private Bitmap shape = null;
    private Bitmap mask = null;

    private boolean isNewMask = true;
    private int trackTemplateCount;//track 模板的竖条的个数
    private int mBackgroundColor;
    private int mForegroundColor;
    private int mSpaceSize = 6;
    private int mTrackItemWidth = 6;
    private int mTrackFragmentCount;//track 片段个数
    private float[] mTrackTemplateData = {0.80f,0.70f,0.40f,0.60f,0.40f,0.30f,0.50f,0.70f,0.65f,0.90f};//track中一个片段中每个竖条的高度比例




    public void setBackgroundColorInt(int backgroundColor) {
        this.mBackgroundColor = backgroundColor;
        invalidate();
    }

    public void setForegroundColor(int foregroundColor) {
        this.mForegroundColor = foregroundColor;
        invalidate();
    }

    public void setSpaceSize(int spaceSize) {
        this.mSpaceSize = spaceSize;
        invalidate();
    }

    public void setTrackItemWidth(int trackItemWidth) {
        this.mTrackItemWidth = trackItemWidth;
        invalidate();
    }

    public void setTrackFragmentCount(int trackFragmentCount) {
        this.mTrackFragmentCount = trackFragmentCount;
        invalidate();
    }

    public void setTrackTemplateData(float[] mTrackTemplateData) {
        this.mTrackTemplateData = mTrackTemplateData;
        invalidate();
    }

    public Track(Context context){
        super(context);
        init();
    }

    public Track(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        init();

    }

    private void init(){
        slipPaint = new Paint();
        slipPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        slipPaint.setFilterBitmap(false);

        maskPaint = new Paint();
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        maskPaint.setFilterBitmap(false);

        mTrackPaint = new Paint();
        mTrackPaint.setAntiAlias(true);
        mTrackPaint.setStrokeWidth(mTrackItemWidth);
        mTrackPaint.setColor(Color.LTGRAY);
        mTrackPaint.setStyle(Paint.Style.FILL);
        mTrackPaint.setStrokeCap(Paint.Cap.ROUND);

        borderPaint = new Paint();
        borderPaint.setAntiAlias(true);
        borderPaint.setStrokeWidth(borderWidth);
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeCap(Paint.Cap.SQUARE);


        trackTemplateCount =  mTrackTemplateData.length;
    }



    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = 0;
        int height = 0;
        //获得宽度MODE
        int modeW = MeasureSpec.getMode(widthMeasureSpec);
        //获得宽度的值
        if (modeW == MeasureSpec.AT_MOST) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }
        if (modeW == MeasureSpec.EXACTLY) {
            width = widthMeasureSpec;
        }
        if (modeW == MeasureSpec.UNSPECIFIED) {
            trackWidth = mSpaceSize +(mTrackItemWidth+mSpaceSize )*trackTemplateCount*mTrackFragmentCount;
            width =  trackWidth;
        }
        //获得高度MODE
        int modeH = MeasureSpec.getMode(height);
        //获得高度的值
        if (modeH == MeasureSpec.AT_MOST) {
            height = MeasureSpec.getSize(heightMeasureSpec);
        }
        if (modeH == MeasureSpec.EXACTLY) {
            height = heightMeasureSpec;
        }
        if (modeH == MeasureSpec.UNSPECIFIED) {
            //ScrollView和HorizontalScrollView
            height = MeasureSpec.getSize(heightMeasureSpec);
        }
        //设置宽度和高度
        setMeasuredDimension(width, height);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {

            drawTrack(canvas,mBackgroundColor);
            int layer = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);
            drawTrack(canvas,mForegroundColor);
            //切割
            if (shape == null || shape.isRecycled()) {
                shape = getShape(getWidth(), getHeight());
            }
            canvas.drawBitmap(shape, 0, 0, slipPaint);

            //画透明格子
            if (isNewMask) {
                mask = getMask(getWidth(), getHeight());
                isNewMask = false;
            }
            canvas.drawBitmap(mask, 0, 0, maskPaint);

            canvas.restoreToCount(layer);




        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap getShape(int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        RectF localRectF = new RectF(0, 0, width, height);
        Paint paint = new Paint();
        paint.setAntiAlias(true); //去锯齿
        canvas.drawRect(localRectF,paint);

        return bitmap;
    }

    private Bitmap getMask(int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true); //去锯齿
        paint.setColor(mForegroundColor);
        //paint.setAlpha(160);

        canvas.drawRect(0, 0, progress, height, paint);

        return bitmap;
    }



    public void setProgress(int progress) {
        this.progress = progress;
        isNewMask = true;
        this.invalidate();
    }





    //轨道-----------------------------------------------------------------------------
    private void drawTrack(Canvas canvas, int color) {

        mTrackPaint.setColor(color);

        if (trackTemplateCount <= 0)
            return;
        int cy = canvas.getHeight() / 2 ;

        for (int j = 0;j<mTrackFragmentCount;j++) {
            for (int i = 0; i < trackTemplateCount; i++) {
                int x =  mSpaceSize +(mTrackItemWidth+mSpaceSize )* i + (mTrackItemWidth+mSpaceSize)*trackTemplateCount*j;
                canvas.drawLine(x, cy - mTrackTemplateData[i]*getHeight()/2, x, cy + mTrackTemplateData[i]*getHeight()/2, mTrackPaint);
            }
        }

    }





}

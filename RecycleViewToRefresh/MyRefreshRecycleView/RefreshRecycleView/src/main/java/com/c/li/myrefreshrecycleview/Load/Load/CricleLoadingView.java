package com.c.li.myrefreshrecycleview.Load.Load;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

import com.c.li.myrefreshrecycleview.R;

public class CricleLoadingView extends View {

    public static final int DEFAULT_SIZE = 30;
    private int mIndCartColor;
    private Paint mPaint;
    private BaseProgressCotroller mIndicatorController;
    private boolean mHasAnimation;

    public CricleLoadingView(Context context) {
        super(context);
        init(null);
    }

    public CricleLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CricleLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CricleLoadingView);
        mIndCartColor = a.getColor(R.styleable.CricleLoadingView_progress_color, Color.WHITE);
        a.recycle();
        mPaint = new Paint();
        mPaint.setColor(mIndCartColor);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mIndicatorController = new CirclePragress();
        mIndicatorController.setTarget(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int with = measureDimension(dpTopx(DEFAULT_SIZE),widthMeasureSpec);
        int height = measureDimension(dpTopx(DEFAULT_SIZE),heightMeasureSpec);
        setMeasuredDimension(with,height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawIndicator(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!mHasAnimation){
            mHasAnimation = true;
            applyAnimtion();
        }
    }

    @Override
    public void setVisibility(int visibility) {
        if (getVisibility() != visibility){
            super.setVisibility(visibility);
            if (visibility == GONE || visibility == VISIBLE){
                mIndicatorController.setAnimationStatus(BaseProgressCotroller.AnimStatus.END);
            }else {
                mIndicatorController.setAnimationStatus(BaseProgressCotroller.AnimStatus.START);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mIndicatorController.setAnimationStatus(BaseProgressCotroller.AnimStatus.CANCEL);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mIndicatorController.setAnimationStatus(BaseProgressCotroller.AnimStatus.START);
    }

    private void drawIndicator(Canvas canvas){
        mIndicatorController.draw(canvas,mPaint);
    }

    private int measureDimension(int defaltsize, int measurSpec){
        int result = defaltsize;
        int specMode = MeasureSpec.getMode(measurSpec);
        int specSize = MeasureSpec.getSize(measurSpec);
        if (specMode == MeasureSpec.EXACTLY){
            result = specSize;
        }else if (specMode == MeasureSpec.AT_MOST){
            result = Math.min(defaltsize,specSize);
        }else {
            result = defaltsize;
        }
        return result;
    }
    private int dpTopx(int dpValue){
        return (int) (getContext().getResources().getDisplayMetrics().density*dpValue);
    }
    private void applyAnimtion(){
        mIndicatorController.initAnimation();
    }
}
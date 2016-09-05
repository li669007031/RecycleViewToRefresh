package com.c.li.myrefreshrecycleview.Load.Load;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/8/31.
 */
public class CirclePragress extends BaseProgressCotroller{
    public static final float SCALE=1.0f;

    public static final int ALPHA=255;

    float[] scaleFloats=new float[]{SCALE,
            SCALE,
            SCALE,
            SCALE,
            SCALE,
            SCALE,
            SCALE,
            SCALE};

    int[] alphas=new int[]{ALPHA,
            ALPHA,
            ALPHA,
            ALPHA,
            ALPHA,
            ALPHA,
            ALPHA,
            ALPHA};

    @Override
    public void draw(Canvas canvas, Paint paint) {
        float radius = getWidth()/10;
        for (int i = 0;i < 8;i++){
            canvas.save();
            Point point = circleAt(getWidth(),getHeight(),getWidth()/2-radius,i*(Math.PI/4));
            canvas.translate(point.x,point.y);
            canvas.scale(scaleFloats[i],scaleFloats[i]);
            paint.setAlpha(alphas[i]);
            canvas.drawCircle(0,0,radius,paint);
            canvas.restore();
        }
    }
    Point circleAt(int width,int height,float radius,double angle){
        float x= (float) (width/2+radius*(Math.cos(angle)));
        float y= (float) (height/2+radius*(Math.sin(angle)));
        return new Point(x,y);
    }

    @Override
    public List<Animator> createAnimation() {
        List<Animator> animators = new ArrayList<>();
        int[] delays = {0,120,240,360,480,600,720,840};
        for (int i = 0;i < 8;i++){
            final int indext = i;
            ValueAnimator scalAnim = ValueAnimator.ofFloat(1,0.4f,1);
            scalAnim.setDuration(1000);
            scalAnim.setRepeatCount(-1);
            scalAnim.setStartDelay(delays[i]);
            scalAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    scaleFloats[indext] = (float) valueAnimator.getAnimatedValue();
                    postInvalidate();
                }
            });
            scalAnim.start();

            ValueAnimator alphAnim = ValueAnimator.ofInt(255,77,255);
            alphAnim.setDuration(1000);
            alphAnim.setRepeatCount(-1);
            alphAnim.setStartDelay(delays[i]);
            alphAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    alphas[indext] = (int) valueAnimator.getAnimatedValue();
                    postInvalidate();
                }
            });
            alphAnim.start();
            animators.add(scalAnim);
            animators.add(alphAnim);
        }
        return animators;
    }
    final class Point{
        public float x;
        public float y;

        public Point(float x, float y){
            this.x=x;
            this.y=y;
        }
    }
}

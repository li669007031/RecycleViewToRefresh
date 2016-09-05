package com.c.li.myrefreshrecycleview.Load.View;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.c.li.myrefreshrecycleview.Load.Load.CricleLoadingView;
import com.c.li.myrefreshrecycleview.R;


/**
 * Created by Administrator on 2016/8/31.
 */
public class MyHeadView extends LinearLayout{
    private static final int ROTATE_ANIM_DURATION = 180;
    //正在刷新的状态
    public final static int STATE_REFRESHING = 2;
    //刷新结束的状态
    public final static int STATE_DONE = 3;
    //没有任何的状态
    public final static int STATE_NORMAL = 0;
    //准备刷新的状态
    private final int STATE_RELEASE_TO_REFRESH = 1;

    private LinearLayout mContentView;
    private TextView tv_refresh_head;
    private ImageView iv_refresh_head;
    private CricleLoadingView pb_refresh_head;
    private RotateAnimation mRatateUpAnima;
    private RotateAnimation mRotateDownAnim;
    private int mStatus;
    private int mMeasureHeigh;


    public MyHeadView(Context context) {
        super(context);
    }

    public MyHeadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContentView = (LinearLayout) View.inflate(context, R.layout.head_recycleview_refresh,null);
        //宽高
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        //设置距离父布局的距离
        lp.setMargins(0,0,0,0);
        //设置布局宽高属性
        this.setLayoutParams(lp);
        //设置内容距离边界距离
        this.setPadding(0,0,0,0);
        //添加内容布局
        addView(mContentView,new LayoutParams(LayoutParams.MATCH_PARENT,0));
        //设置内容位于布局的下方
        setGravity(Gravity.BOTTOM);
        tv_refresh_head = (TextView) findViewById(R.id.tv_y_recycleview_head_refresh_status);
        iv_refresh_head = (ImageView) findViewById(R.id.iv_y_recycleview_head_refresh_status);
        pb_refresh_head = (CricleLoadingView) findViewById(R.id.pb_y_recycleview_head_refresh_progressbar);
        //定义一个向上翻转的动画
        mRatateUpAnima = new RotateAnimation(0.0f,-180.0f, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        mRatateUpAnima.setDuration(ROTATE_ANIM_DURATION);
        //翻转后保持翻转后的状态
        mRatateUpAnima.setFillAfter(true);

        mRotateDownAnim = new RotateAnimation(-180.0f,0.0f, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateDownAnim.setFillAfter(true);
        //测量
        measure(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        //获取高度
        mMeasureHeigh = getMeasuredHeight();

    }
    public void setStatus(int status){
        //如果状态一直就不需要改变
        if (status == mStatus){
            return;
        }
        //设置刷新状态
        if (status == STATE_REFRESHING){
            //设置箭头图片不可见
            iv_refresh_head.clearAnimation();
            iv_refresh_head.setVisibility(View.INVISIBLE);
            //设置进度条可见
            pb_refresh_head.setVisibility(View.VISIBLE);
        }else if (status == STATE_DONE){
            //刷新结束状态
            iv_refresh_head.setVisibility(View.VISIBLE);
            pb_refresh_head.setVisibility(View.INVISIBLE);
        }else {
            //正常状态
            iv_refresh_head.setVisibility(View.VISIBLE);
            pb_refresh_head.setVisibility(View.INVISIBLE);
        }

        switch (status){
            case STATE_NORMAL:
                //如果当前状态是装备刷新就开始动画
                if (mStatus == STATE_RELEASE_TO_REFRESH){
                    iv_refresh_head.startAnimation(mRotateDownAnim);
                }
                //如果当前状态是正在刷新就停止动画
                if (mStatus == STATE_REFRESHING){
                    iv_refresh_head.clearAnimation();
                }
                tv_refresh_head.setText("下拉刷新");
                break;
            case STATE_RELEASE_TO_REFRESH:
                if (mStatus != STATE_RELEASE_TO_REFRESH){
                    iv_refresh_head.clearAnimation();
                    iv_refresh_head.setAnimation(mRatateUpAnima);
                    tv_refresh_head.setText("松开刷新");
                }
                break;
            case STATE_REFRESHING:
                tv_refresh_head.setText("正在刷新");
                break;
            case STATE_DONE:
                tv_refresh_head.setText("刷新完成");
                break;
            default:
        }
        mStatus = status;
    }
    /**
     * 返回当前状态值
     */
    public int getStaus(){return mStatus;}

    //释放意图
    public boolean releaseAction(){
        //是否在刷新
        boolean isOnRefresh = false;
        //获取显示高度
        int heigh = getVisibleHeigh();
        if (heigh == 0){
            isOnRefresh =false;
        }
        if (getVisibleHeigh() > mMeasureHeigh && mStatus < STATE_REFRESHING){
            setStatus(STATE_REFRESHING);
            isOnRefresh = true;
        }
        if (heigh < mMeasureHeigh && mStatus == STATE_REFRESHING){}
        int desHeight = 0;
        if (mStatus == STATE_REFRESHING){
            desHeight = mMeasureHeigh;
        }
        smoothScrollTo(desHeight);
        return isOnRefresh;
    }

    //移动的距离
    public void onMove(float delta){
        if (getVisibleHeigh() > 0 || delta > 0){
            setVisbleHeigh((int) delta + getVisibleHeigh());
            if (mStatus <= STATE_RELEASE_TO_REFRESH){
                if (getVisibleHeigh() >= mMeasureHeigh){
                    setStatus(STATE_RELEASE_TO_REFRESH);
                }else {
                    setStatus(STATE_NORMAL);
                }
            }
        }
    }
    //改变布局高度的方法
    public void smoothScrollTo(int height){
        ValueAnimator animator = ValueAnimator.ofInt(getVisibleHeigh(),height);
        animator.setDuration(300).start();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                setVisbleHeigh((int) valueAnimator.getAnimatedValue());
            }
        });
    }
    //设置高度为0并回复正常状态
    public void reset(){
        smoothScrollTo(0);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setStatus(STATE_NORMAL);
            }
        },500);
    }
    //刷新完成
    public void refreshComplet(){
        setStatus(STATE_DONE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                reset();
            }
        },200);
    }

    public void setVisbleHeigh(int height){
        if (height < 0){height = 0;}
        LayoutParams lp = (LayoutParams) mContentView.getLayoutParams();
        lp.height = height;
        mContentView.setLayoutParams(lp);
    }
    public int getVisibleHeigh(){
        LayoutParams lp = (LayoutParams) mContentView.getLayoutParams();
        return lp.height;
    }
}

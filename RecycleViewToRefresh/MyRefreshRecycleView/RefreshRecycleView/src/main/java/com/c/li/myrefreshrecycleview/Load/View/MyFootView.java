package com.c.li.myrefreshrecycleview.Load.View;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.c.li.myrefreshrecycleview.Load.Load.CricleLoadingView;
import com.c.li.myrefreshrecycleview.R;


/**
 * Created by Administrator on 2016/9/1.
 */
public class MyFootView extends LinearLayout {
    //加载中
    public final static int STATE_LOADING = 0;
    //加载完成
    public final static int STATE_COMPLETE = 1;
    //正常状态
    public final static int STATE_NOMORE = 2;
    private CricleLoadingView bp_refresh_foot;
    private TextView tv_refresh_foot;

    public MyFootView(Context context) {
        super(context);
        init(context);
    }

    public MyFootView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);

    }

    private void init(Context context) {
        //设置内部内容居中
        setGravity(Gravity.CENTER);
        //设置宽高
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        //设置布局
        View mContentView = View.inflate(context, R.layout.foot_recycleview_refresh,null);
        bp_refresh_foot = (CricleLoadingView) findViewById(R.id.bp_recycleview_foot);
        tv_refresh_foot = (TextView) findViewById(R.id.tv_recycleview_foot);
        addView(mContentView);
    }
    public void setStatus(int status){
        switch (status){
            case STATE_LOADING:
                bp_refresh_foot.setVisibility(View.VISIBLE);
                tv_refresh_foot.setText("正在加载");
                break;
            case STATE_COMPLETE:
                tv_refresh_foot.setText("正在加载");
                this.setVisibility(View.GONE);
                break;
            case STATE_NOMORE:
                tv_refresh_foot.setText("没有更多了");
                bp_refresh_foot.setVisibility(View.GONE);
                this.setVisibility(View.VISIBLE);
                break;
        }
    }


}

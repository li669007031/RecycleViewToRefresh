package com.c.li.myrefreshrecycleview.Load.View;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/1.
 */
public class MyRecycleViewRefresh extends RecyclerView {
    /**
     * 是否可以刷新的常量
     */
    private boolean pullRefreshEnabled = true;
    /**
     * 是否可以加载更多的常量
     */
    private boolean loadMoreEnabled = true;
    //加载数据中的状态
    private boolean isLoadingData = false;
    //没有数据的状态
    private boolean isNoMore = false;
    //头布局集合
    private List<View> mRefreshHeads = new ArrayList<>();
    //脚布局集合
    private List<View> mRefreshFoots = new ArrayList<>();
    //当前头布局
    private MyHeadView myHeadView;
    //监听器
    private OnRefreshAndLoadMoreListener refreshAndLoadMoreListener;
    //adapter没有数据的时候显示,类似于listView的emptyView
    private View mEmptyView;
    /**
     * 数据观察者
     */
    private final RecyclerView.AdapterDataObserver mDataObserver = new DataObserver();
    //最后的坐标
    private float mLastY = -1;
    //阻率
    private static final float DRAG_RATE = 2;
    //判断item类型的标识
    private static final int TYPE_REFRESH_HEADER = -5;
    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_FOOTER = -3;
    private static List<Integer> sHeaderTypes = new ArrayList<>();
    private static final int HEADER_INIT_INDEX = 10000;

    private WrapAdapter wrapAdapter;
    public MyRecycleViewRefresh(Context context) {
        super(context);
    }

    public MyRecycleViewRefresh(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyRecycleViewRefresh(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        if (pullRefreshEnabled){
            //获得头布局
            MyHeadView headView = new MyHeadView(getContext());
            //添加到头布局集合
            mRefreshHeads.add(0,headView);
            myHeadView = headView;
        }
        MyFootView footView = new MyFootView(getContext());
        mRefreshFoots.add(footView);
        mRefreshFoots.get(0).setVisibility(GONE);
    }
    //当滚动状态改变的时候
    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        if (state == RecyclerView.SCROLL_STATE_IDLE && refreshAndLoadMoreListener != null && !isLoadingData && loadMoreEnabled){
            LayoutManager layoutManager = getLayoutManager();
            int lastVisibleItemPostion;
            if (layoutManager instanceof GridLayoutManager){
                lastVisibleItemPostion = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
            }else if (layoutManager instanceof StaggeredGridLayoutManager){
                int[] into = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
                ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(into);
                lastVisibleItemPostion = findMax(into);
            }else {
                lastVisibleItemPostion = ((LinearLayoutManager)layoutManager).findLastVisibleItemPosition();
            }

            if (layoutManager.getChildCount() > 0 && lastVisibleItemPostion >= layoutManager.getItemCount()-1
                    && layoutManager.getItemCount() > layoutManager.getChildCount() &&
                    !isNoMore && myHeadView.getStaus() < MyHeadView.STATE_REFRESHING){
                View footVIew = mRefreshFoots.get(0);
                isLoadingData = true;
                if (footVIew instanceof MyFootView){
                    ((MyFootView) footVIew).setStatus(MyFootView.STATE_LOADING);
                }else {
                    footVIew.setVisibility(View.VISIBLE);
                }
                refreshAndLoadMoreListener.onLoadMore();
            }

        }
    }
    private int findMax(int[] into){
        int max = into[0];
        for (int valu : into) {
            if (valu > max) {
                max = valu;
            }
        }
        return max;
    }

    //触摸事件监听
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (mLastY == -1){
            mLastY = e.getRawY();
        }
        switch (e.getAction()){
            case MotionEvent.ACTION_DOWN:
                mLastY = e.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float deltay = e.getRawY() - mLastY;
                mLastY = e.getRawY();
                if (isOnTop() && pullRefreshEnabled){
                    myHeadView.onMove(deltay/DRAG_RATE);
                    if (myHeadView.getVisibleHeigh() > 0 && myHeadView.getStaus() < myHeadView.STATE_REFRESHING){
                        return false;
                    }
                }
                break;
            default:
                //复位
                mLastY = -1;
                if (isOnTop() && pullRefreshEnabled){
                    if (myHeadView.releaseAction()){
                        if (refreshAndLoadMoreListener != null){
                            refreshAndLoadMoreListener.onRefresh();
                        }
                    }
                }
                break;
        }
        return super.onTouchEvent(e);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
    }

    public class WrapAdapter extends RecyclerView.Adapter<ViewHolder>{
        private RecyclerView.Adapter adapter;
        private int mCurrentPosition;
        private int headerPosition = 1;

        @Override
        public int getItemViewType(int position) {
            if (isRefreshHead(position)){
                return TYPE_REFRESH_HEADER;
            }
            if (isHeader(position)){
                position = position - 1;
                return sHeaderTypes.get(position);
            }
            if (isFooter(position)){
                return TYPE_FOOTER;
            }
            int adjPosition = position - mRefreshHeads.size();
            int adapterCount;
            if (adapter != null){
                adapterCount = adapter.getItemCount();
                if (adjPosition < adapterCount){
                    return adapter.getItemViewType(adjPosition);
                }
            }
            return TYPE_NORMAL;
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
            if (manager instanceof GridLayoutManager){
                final GridLayoutManager gridManager = (GridLayoutManager) manager;
                gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return (isHeader(position) || isFooter(position))
                                ? gridManager.getSpanCount() : 1;
                    }
                });
            }
        }

        @Override
        public void onViewAttachedToWindow(ViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams
                    && (isHeader(holder.getLayoutPosition()) || isFooter(holder.getLayoutPosition()))){
                StaggeredGridLayoutManager.LayoutParams stagLp = (StaggeredGridLayoutManager.LayoutParams) lp;
                stagLp.setFullSpan(true);
            }
        }

        public boolean isFooter(int position){
            return position < getItemCount() && position >= getItemCount() - mRefreshFoots.size();
        }
        public boolean isHeader(int position){return position >= 0 && position < mRefreshHeads.size();}

        public boolean isRefreshHead(int posit){return posit == 0;}

        public boolean isContentHeader(int position){
            return position >= 1 && position < mRefreshHeads.size();
        }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_REFRESH_HEADER){
                mCurrentPosition++;
                return new SimpleViewHolder(mRefreshHeads.get(0));
            }else if (isContentHeader(mCurrentPosition)){
                if (viewType == sHeaderTypes.get(mCurrentPosition - 1)){
                    mCurrentPosition++;
                    return new SimpleViewHolder(mRefreshHeads.get(headerPosition++));
                }
            }else if (viewType == TYPE_FOOTER){
                return new SimpleViewHolder(mRefreshFoots.get(0));
            }
            return adapter.onCreateViewHolder(parent,viewType);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (isHeader(position)){
                return;
            }
            int adjposition = position - mRefreshHeads.size();
            int adapterCount;
            if (adapter != null){
                adapterCount = adapter.getItemCount();
                if (adjposition < adapterCount){
                    adapter.onBindViewHolder(holder,adjposition);
                    return;
                }
            }
        }

        @Override
        public int getItemCount() {
            if (adapter != null){
                return mRefreshHeads.size() + mRefreshFoots.size() + adapter.getItemCount();
            }else {
                return mRefreshHeads.size() + mRefreshFoots.size();
            }
        }

        @Override
        public long getItemId(int position) {
            if (adapter != null && position >= mRefreshHeads.size()){
                int adjposition = position - mRefreshHeads.size();
                int adapterCount = adapter.getItemCount();
                if (adjposition < adapterCount){
                    return adapter.getItemId(adjposition);
                }
            }
            return -1;
        }

        @Override
        public void registerAdapterDataObserver(AdapterDataObserver observer) {
            if (adapter != null){
                adapter.registerAdapterDataObserver(observer);
            }

        }

        @Override
        public void unregisterAdapterDataObserver(AdapterDataObserver observer) {
            if (adapter != null){
                adapter.unregisterAdapterDataObserver(observer);
            }
        }

        public class SimpleViewHolder extends RecyclerView.ViewHolder{

            public SimpleViewHolder(View itemView) {
                super(itemView);
            }
        }
    }

    /**
     * 写一个数据观察者
     * @return
     */
    public class DataObserver extends RecyclerView.AdapterDataObserver{
        @Override
        public void onChanged() {
            Adapter adapter = getAdapter();
            if (adapter != null && mEmptyView != null){
                int emptyCount = 0;
                if (pullRefreshEnabled){
                    emptyCount++;
                }
                if (loadMoreEnabled){
                    emptyCount++;
                }
                if (adapter.getItemCount() == emptyCount){
                    mEmptyView.setVisibility(View.VISIBLE);
                    MyRecycleViewRefresh.this.setVisibility(View.GONE);
                }else {
                    mEmptyView.setVisibility(View.GONE);
                    MyRecycleViewRefresh.this.setVisibility(View.VISIBLE);
                }
            }
            if (wrapAdapter != null){
                wrapAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            wrapAdapter.notifyItemRangeInserted(positionStart,itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            wrapAdapter.notifyItemRangeChanged(positionStart,itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            wrapAdapter.notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            wrapAdapter.notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            wrapAdapter.notifyItemMoved(fromPosition, toPosition);
        }
    }

    //设置没有更多数据
    public void setNomore(boolean nomore){
        this.isNoMore = nomore;
        View footView = mRefreshFoots.get(0);
        ((MyFootView)footView).setStatus(isNoMore ? MyFootView.STATE_NOMORE : MyFootView.STATE_COMPLETE);
    }
    //获取一个空布局
    public View getmEmptyView(){
        return mEmptyView;
    }
    //设置空布局
    public void setmEmptyView(View emptyView){
        mEmptyView = emptyView;
        mDataObserver.onChanged();
    }
    //还原所有状态
    public void resetStatus(){
        setloadMoreComplet();
        refreshComplet();
    }
    //设置刷新完成
    public void refreshComplet(){
        myHeadView.refreshComplet();
    }
    //设置加载更多完成
    public void setloadMoreComplet(){
        isLoadingData = false;
        View footView = mRefreshFoots.get(0);
        if (footView instanceof MyFootView){
            ((MyFootView)footView).setStatus(MyFootView.STATE_COMPLETE);
        }else {
            footView.setVisibility(View.GONE);
        }
    }
    /**
     * 设置刷新和加载更多的监听器
     *
     * @param refreshAndLoadMoreListener 监听器
     */
    public void setRefreshAndLoadMoreListener(OnRefreshAndLoadMoreListener refreshAndLoadMoreListener) {
        this.refreshAndLoadMoreListener = refreshAndLoadMoreListener;
    }

    /**
     * 设置是否启用下拉刷新功能
     *
     * @param isEnabled
     */
    public void setReFreshEnabled(boolean isEnabled) {
        pullRefreshEnabled = isEnabled;
    }

    /**
     * 设置是否启用上拉加载功能
     *
     * @param isEnabled
     */
    public void setLoadMoreEnabled(boolean isEnabled) {
        loadMoreEnabled = isEnabled;
        //如果不启用加载更多功能,就隐藏脚布局
        if (!isEnabled && mRefreshFoots.size() > 0) {
            mRefreshFoots.get(0).setVisibility(GONE);
        }
    }

    /**
     * 添加头布局
     *
     * @param headView 刷新头布局
     */
    public void addHeadView(View headView) {
        if (pullRefreshEnabled && !(mRefreshHeads.get(0) instanceof MyHeadView)) {
            MyHeadView refreshHeader = new MyHeadView(getContext());
            mRefreshHeads.add(0, refreshHeader);
            myHeadView = refreshHeader;
        }
        mRefreshHeads.add(headView);
        sHeaderTypes.add(HEADER_INIT_INDEX + mRefreshHeads.size());
    }

    /**
     * 添加底部布局
     *
     * @param footView
     */
    private void addFootView(MyFootView footView) {
        mRefreshFoots.clear();
        mRefreshFoots.add(footView);
    }

    public boolean isOnTop(){
        return !(myHeadView == null || mRefreshHeads.isEmpty()) && mRefreshHeads.get(0).getParent() != null;
    }
    //刷新和加载更多的监听
    public interface OnRefreshAndLoadMoreListener{
        void onRefresh();
        void onLoadMore();
    }

}

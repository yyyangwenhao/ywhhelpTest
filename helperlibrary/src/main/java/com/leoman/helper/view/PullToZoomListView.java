package com.leoman.helper.view;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.leoman.helper.R;
import com.leoman.helper.util.DisplayUtil;

/**
 * 下拉header放大
 */

public class PullToZoomListView extends ListView implements AbsListView.OnScrollListener {

    private View footerView;
    private TextView footTv;
    private View preTv;
    private boolean isNomore = false;
    private boolean isLoad = false;
    private static final Interpolator sInterpolator = new Interpolator() {
        public float getInterpolation(float paramAnonymousFloat) {
            float f = paramAnonymousFloat - 1.0F;
            return 1.0F + f * (f * (f * (f * f)));
        }
    };
    int mActivePointerId = -1;
    private FrameLayout mHeaderContainer;
    private int mHeaderHeight;

    public int getmHeaderHeight() {
        return mHeaderHeight;
    }

    /**
     * 设置头部的高度
     *
     * @param mHeaderHeight
     */
    public void setmHeaderHeight(int mHeaderHeight) {
        this.mHeaderHeight = mHeaderHeight;
        LayoutParams lp = new LayoutParams(DisplayUtil.dip2px(mContext, LayoutParams.MATCH_PARENT), mHeaderHeight);
        getHeaderContainer().setLayoutParams(lp);
    }

    private ImageView mHeaderImage;
    float mLastMotionY = -1.0F;
    float mLastScale = -1.0F;
    float mMaxScale = -1.0F;
    private OnScrollListener mOnScrollListener;
    private ScalingRunnalable mScalingRunnalable;
    private int mScreenHeight;
    private ImageView mShadow;

    private Context mContext;

    public PullToZoomListView(Context paramContext) {
        super(paramContext);
        init(paramContext);
        mContext = paramContext;
    }

    public PullToZoomListView(Context paramContext,
                              AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        init(paramContext);
        mContext = paramContext;
    }

    public PullToZoomListView(Context paramContext,
                              AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
        init(paramContext);
        mContext = paramContext;
    }

    private void endScraling() {
//        if (this.mHeaderContainer.getBottom() >= this.mHeaderHeight)
//            Log.d("mmm", "endScraling");
        this.mScalingRunnalable.startAnimation(200L);
    }

    private void init(Context paramContext) {
        DisplayMetrics localDisplayMetrics = new DisplayMetrics();
        ((Activity) paramContext).getWindowManager().getDefaultDisplay()
                .getMetrics(localDisplayMetrics);
        this.mScreenHeight = localDisplayMetrics.heightPixels;
        this.mHeaderContainer = new FrameLayout(paramContext);

        this.mHeaderImage = new ImageView(paramContext);
        int i = localDisplayMetrics.widthPixels;
        setHeaderViewSize(i, (int) (9.0F * (i / 16.0F)));
        this.mShadow = new ImageView(paramContext);
        FrameLayout.LayoutParams localLayoutParams = new FrameLayout.LayoutParams(-1, -2);
        localLayoutParams.gravity = Gravity.CENTER;
        this.mShadow.setLayoutParams(localLayoutParams);
        this.mHeaderContainer.addView(this.mHeaderImage);
        this.mHeaderContainer.addView(this.mShadow);
//		addHeaderView(this.mHeaderContainer);
        footerView = LayoutInflater.from(paramContext).inflate(R.layout.zoomlistview_footer, null);
        footTv = (TextView) footerView.findViewById(R.id.zoomlistview_footer_hint_textview);
        preTv = footerView.findViewById(R.id.zoomlistview_footer_progressbar);
//        footTv.setText("查看更多");
        addFooterView(footerView);
        this.mScalingRunnalable = new ScalingRunnalable();
        super.setOnScrollListener(this);
        footerView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (!isNomore) {
                    footTv.setText("加载中");
                    preTv.setVisibility(View.VISIBLE);
                    startLoadMore();
                }
            }
        });
    }

    private void onSecondaryPointerUp(MotionEvent paramMotionEvent) {
        int i = (paramMotionEvent.getAction()) >> 8;
        if (paramMotionEvent.getPointerId(i) == this.mActivePointerId)
            if (i != 0) {
                this.mLastMotionY = paramMotionEvent.getY(0);
                this.mActivePointerId = paramMotionEvent.getPointerId(0);
                return;
            }
    }

    private void reset() {
        this.mActivePointerId = -1;
        this.mLastMotionY = -1.0F;
        this.mMaxScale = -1.0F;
        this.mLastScale = -1.0F;
    }

    public ImageView getHeaderView() {
        return this.mHeaderImage;
    }


    public FrameLayout getHeaderContainer() {
        return mHeaderContainer;
    }

    public void setHeaderView() {
        addHeaderView(this.mHeaderContainer);
    }

//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//
//        final int action = ev.getAction() ;
//
//        float mInitialMotionY = 0;
//        float mLastMotionY = 0;
//
//        boolean isIntercept = false;
//        switch (action) {
//            case MotionEvent.ACTION_DOWN:
//
//                mLastMotionY = ev.getY();
//                break;
//
//            case MotionEvent.ACTION_MOVE:
//                mInitialMotionY = ev.getY();
//                if (Math.abs(mInitialMotionY - mLastMotionY) > 10) {
//                    isIntercept = false;
//                }
//                break;
//
//        }
//
//        return isIntercept;
//    }

    protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2,
                            int paramInt3, int paramInt4) {
        super.onLayout(paramBoolean, paramInt1, paramInt2, paramInt3, paramInt4);
        if (this.mHeaderHeight == 0)
            this.mHeaderHeight = this.mHeaderContainer.getHeight();
    }

    @Override
    public void onScroll(AbsListView paramAbsListView, int paramInt1,
                         int paramInt2, int paramInt3) {

        float f = this.mHeaderHeight - this.mHeaderContainer.getBottom();
        if ((f > 0.0F) && (f < this.mHeaderHeight)) {
//            Log.d("mmm", "1");
            int i = (int) (0.65D * f);
            this.mHeaderImage.scrollTo(0, -i);
        } else if (this.mHeaderImage.getScrollY() != 0) {
//            Log.d("mmm", "2");
            this.mHeaderImage.scrollTo(0, 0);
        }
        if (this.mOnScrollListener != null) {
            this.mOnScrollListener.onScroll(paramAbsListView, paramInt1,
                    paramInt2, paramInt3);
        }
    }

    public void onScrollStateChanged(AbsListView paramAbsListView, int paramInt) {
        if (this.mOnScrollListener != null) {
            this.mOnScrollListener.onScrollStateChanged(paramAbsListView, paramInt);
        }
        if (paramAbsListView.getLastVisiblePosition() == (getCount() - 1)) {
            if (isNomore) {
                footerView.setVisibility(View.GONE);
                footerView.setPadding(0, DisplayUtil.setInt(-200), 0, 0);
            } else {
                footerView.setVisibility(View.VISIBLE);
                footerView.setPadding(0, 0, 0, 0);
                footTv.setText("加载中");
                preTv.setVisibility(View.VISIBLE);
                startLoadMore();
            }
        }
    }

    public void setfootviewGone(){
        footTv.setVisibility(GONE);
    }

    public void setLoadFinish(boolean flag) {
        footTv.setText("查看更多");
        preTv.setVisibility(View.INVISIBLE);
        isLoad = false;
        isNomore = flag;
        if (isNomore) {
            footerView.setVisibility(View.GONE);
            footerView.setPadding(0, DisplayUtil.setInt(-200), 0, 0);
        } else {
            footerView.setVisibility(View.VISIBLE);
            footerView.setPadding(0, 0, 0, 0);
        }
    }

    public boolean onTouchEvent(MotionEvent paramMotionEvent) {
//        Log.d("mmm", "" + (0xFF & paramMotionEvent.getAction()));
        switch (0xFF & paramMotionEvent.getAction()) {
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_DOWN:
                if (!this.mScalingRunnalable.mIsFinished) {
                    this.mScalingRunnalable.abortAnimation();
                }
                this.mLastMotionY = paramMotionEvent.getY();
                this.mActivePointerId = paramMotionEvent.getPointerId(0);
                this.mMaxScale = (this.mScreenHeight / this.mHeaderHeight);
                this.mLastScale = (this.mHeaderContainer.getBottom() / this.mHeaderHeight);
                break;
            case MotionEvent.ACTION_MOVE:
//                Log.d("mmm", "mActivePointerId" + mActivePointerId);
                int j = paramMotionEvent.findPointerIndex(this.mActivePointerId);
                if (j == -1) {
//                    Log.e("PullToZoomListView", "Invalid pointerId="
//                            + this.mActivePointerId + " in onTouchEvent");
                } else {
                    if (this.mLastMotionY == -1.0F)
                        this.mLastMotionY = paramMotionEvent.getY(j);
                    if (this.mHeaderContainer.getBottom() >= this.mHeaderHeight) {
                        ViewGroup.LayoutParams localLayoutParams = this.mHeaderContainer
                                .getLayoutParams();
                        float f = ((paramMotionEvent.getY(j) - this.mLastMotionY + this.mHeaderContainer
                                .getBottom()) / this.mHeaderHeight - this.mLastScale)
                                / 2.0F + this.mLastScale;
                        if ((this.mLastScale <= 1.0D) && (f < this.mLastScale)) {
                            localLayoutParams.height = this.mHeaderHeight;
                            this.mHeaderContainer
                                    .setLayoutParams(localLayoutParams);
                            return super.onTouchEvent(paramMotionEvent);
                        }
                        this.mLastScale = Math.min(Math.max(f, 1.0F),
                                this.mMaxScale);
                        localLayoutParams.height = ((int) (this.mHeaderHeight * this.mLastScale));
                        if (localLayoutParams.height < this.mScreenHeight)
                            this.mHeaderContainer
                                    .setLayoutParams(localLayoutParams);
                        this.mLastMotionY = paramMotionEvent.getY(j);
                        return true;
                    }
                    this.mLastMotionY = paramMotionEvent.getY(j);
                }
                break;
            case MotionEvent.ACTION_UP:
                reset();
                if (this.mHeaderContainer.getBottom() > this.mHeaderHeight) {
                    endScraling();
                    return true;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                int i = paramMotionEvent.getActionIndex();
                this.mLastMotionY = paramMotionEvent.getY(i);
                this.mActivePointerId = paramMotionEvent.getPointerId(i);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                onSecondaryPointerUp(paramMotionEvent);


                try {
                    this.mLastMotionY = paramMotionEvent.getY(paramMotionEvent
                            .findPointerIndex(this.mActivePointerId));
                } catch (IllegalArgumentException e) {
                    // TODO: handle exception
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
        }
        return super.onTouchEvent(paramMotionEvent);
    }

    public void setHeaderViewSize(int paramInt1, int paramInt2) {
        Object localObject = this.mHeaderContainer.getLayoutParams();
        if (localObject == null)
            localObject = new LayoutParams(paramInt1, paramInt2);
        ((ViewGroup.LayoutParams) localObject).width = paramInt1;
        ((ViewGroup.LayoutParams) localObject).height = paramInt2;
        this.mHeaderContainer
                .setLayoutParams((ViewGroup.LayoutParams) localObject);
        this.mHeaderHeight = paramInt2;
    }

    public void setOnScrollListener(
            OnScrollListener paramOnScrollListener) {
        this.mOnScrollListener = paramOnScrollListener;
    }

    public void setShadow(int paramInt) {
        this.mShadow.setBackgroundResource(paramInt);
    }

    class ScalingRunnalable implements Runnable {
        long mDuration;
        boolean mIsFinished = true;
        float mScale;
        long mStartTime;

        ScalingRunnalable() {
        }

        public void abortAnimation() {
            this.mIsFinished = true;
        }

        public boolean isFinished() {
            return this.mIsFinished;
        }

        public void run() {
            float f2;
            ViewGroup.LayoutParams localLayoutParams;
            if ((!this.mIsFinished) && (this.mScale > 1.0D)) {
                float f1 = ((float) SystemClock.currentThreadTimeMillis() - (float) this.mStartTime)
                        / (float) this.mDuration;
                f2 = this.mScale - (this.mScale - 1.0F)
                        * PullToZoomListView.sInterpolator.getInterpolation(f1);
                localLayoutParams = PullToZoomListView.this.mHeaderContainer
                        .getLayoutParams();
                if (f2 > 1.0F) {
//                    Log.d("mmm", "f2>1.0");
                    localLayoutParams.height = PullToZoomListView.this.mHeaderHeight;
                    localLayoutParams.height = ((int) (f2 * PullToZoomListView.this.mHeaderHeight));
                    PullToZoomListView.this.mHeaderContainer
                            .setLayoutParams(localLayoutParams);
                    PullToZoomListView.this.post(this);
                    return;
                }
                this.mIsFinished = true;
            }
        }

        public void startAnimation(long paramLong) {
            this.mStartTime = SystemClock.currentThreadTimeMillis();
            this.mDuration = paramLong;
            this.mScale = ((float) (PullToZoomListView.this.mHeaderContainer
                    .getBottom()) / PullToZoomListView.this.mHeaderHeight);
            this.mIsFinished = false;
            PullToZoomListView.this.post(this);
        }
    }

    private void startLoadMore() {
        if (mListViewListener != null && !isLoad) {
            isLoad = true;
            mListViewListener.onLoadMore();
        }
    }

    public void setPullToZoomListViewListener(PullToZoomListViewListener l) {
        mListViewListener = l;
    }


    public interface PullToZoomListViewListener {
        public void onLoadMore();
    }

    private PullToZoomListViewListener mListViewListener;
}

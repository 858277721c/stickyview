package com.fanwe.lib.stickyview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

public class FStickyWrapper extends ViewGroup
{
    private int mHeightMeasured;
    private WeakReference<View> mSticky;

    private final int[] mLocation = new int[2];
    private int mLastY = -1;

    public FStickyWrapper(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setPadding(0, 0, 0, 0);
    }

    public View getSticky()
    {
        return mSticky == null ? null : mSticky.get();
    }

    /**
     * 更新位置，并返回此次位置和上一次位置之间的偏移量
     *
     * @return
     */
    int updateLocation()
    {
        getLocationOnScreen(mLocation);

        int delta = 0;
        if (mLastY >= 0)
            delta = mLocation[1] - mLastY;

        mLastY = mLocation[1];
        return delta;
    }

    int getLocation()
    {
        return mLocation[1];
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom)
    {
        super.setPadding(0, 0, 0, 0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int width = getSuggestedMinimumWidth();
        int height = getSuggestedMinimumHeight();

        final View child = getChildAt(0);
        if (child != null && child.getVisibility() != GONE)
        {
            measureChild(child, widthMeasureSpec, heightMeasureSpec);

            width = Math.max(width, child.getMeasuredWidth());
            height = Math.max(height, child.getMeasuredHeight());
        }

        width = Utils.getMeasureSize(width, widthMeasureSpec);
        height = Utils.getMeasureSize(height, heightMeasureSpec);
        setMeasuredDimension(width, height);

        mHeightMeasured = getMeasuredHeight();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        final View child = getChildAt(0);
        if (child != null && child.getVisibility() != GONE)
            child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());
    }

    @Override
    public void onViewAdded(View child)
    {
        super.onViewAdded(child);
        if (getChildCount() > 1)
            throw new RuntimeException("FStickyWrapper can only add one child");

        mSticky = new WeakReference<>(child);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onViewRemoved(View child)
    {
        super.onViewRemoved(child);
        setHeight(mHeightMeasured);
    }

    private void setHeight(int height)
    {
        final ViewGroup.LayoutParams params = getLayoutParams();
        if (params == null)
            return;

        if (params.height != height)
        {
            params.height = height;
            setLayoutParams(params);
        }
    }
}

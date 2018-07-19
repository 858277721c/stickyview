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

    private int mCurrentY;
    private int mLastY = -1;
    private int mDeltaY;

    public FStickyWrapper(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setPadding(0, 0, 0, 0);
    }

    View getSticky()
    {
        return mSticky == null ? null : mSticky.get();
    }

    void updateLocation()
    {
        getLocationOnScreen(mLocation);
        mCurrentY = mLocation[1];

        if (mLastY >= 0)
            mDeltaY = mCurrentY - mLastY;
        mLastY = mCurrentY;
    }

    int getLocation()
    {
        return mCurrentY;
    }

    int getLocationDelta()
    {
        final int delta = mDeltaY;
        mDeltaY = 0;
        return delta;
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
            final ViewGroup.LayoutParams params = child.getLayoutParams();
            child.measure(getChildMeasureSpec(widthMeasureSpec, 0, params.width),
                    getChildMeasureSpec(heightMeasureSpec, 0, params.height));

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

        checkChild(child);
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

    private static void checkChild(View view)
    {
        if (view instanceof ViewGroup)
        {
            if (view instanceof FStickyLayout)
                throw new RuntimeException("FStickyLayout found");
            if (view instanceof FStickyWrapper)
                throw new RuntimeException("FStickyWrapper found");

            final ViewGroup viewGroup = (ViewGroup) view;
            final int count = viewGroup.getChildCount();
            for (int i = 0; i < count; i++)
            {
                final View child = viewGroup.getChildAt(i);
                checkChild(child);
            }
        }
    }
}

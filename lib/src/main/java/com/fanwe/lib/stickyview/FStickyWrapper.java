package com.fanwe.lib.stickyview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

public class FStickyWrapper extends ViewGroup
{
    private final int[] mLocation = new int[2];
    private int mHeightMeasured;
    private WeakReference<View> mSticky;

    public FStickyWrapper(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setPadding(0, 0, 0, 0);
    }

    View getSticky()
    {
        return mSticky == null ? null : mSticky.get();
    }

    int[] getLocation()
    {
        getLocationOnScreen(mLocation);
        return mLocation;
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom)
    {
        super.setPadding(0, 0, 0, 0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        final int widthDefault = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int heightDefault = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        final View child = getChildAt(0);
        if (child != null)
        {
            final ViewGroup.LayoutParams params = child.getLayoutParams();
            child.measure(getChildMeasureSpec(widthMeasureSpec, 0, params.width),
                    getChildMeasureSpec(heightMeasureSpec, 0, params.height));

            final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int width = 0;
            if (widthMode == MeasureSpec.EXACTLY)
            {
                width = widthSize;
            } else
            {
                width = Math.max(widthDefault, child.getMeasuredWidth());
                if (widthMode == MeasureSpec.AT_MOST)
                    width = Math.min(width, widthSize);
            }

            final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            final int heightSize = MeasureSpec.getSize(heightMeasureSpec);
            int height = 0;
            if (heightMode == MeasureSpec.EXACTLY)
            {
                height = heightSize;
            } else
            {
                height = Math.max(heightDefault, child.getMeasuredHeight());
                if (heightMode == MeasureSpec.AT_MOST)
                    height = Math.min(height, heightSize);
            }
            setMeasuredDimension(width, height);
        } else
        {
            setMeasuredDimension(widthDefault, heightDefault);
        }

        mHeightMeasured = getMeasuredHeight();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        final View child = getChildAt(0);
        if (child != null)
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

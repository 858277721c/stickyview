package com.fanwe.lib.stickyview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.lang.ref.WeakReference;

public class FStickyWrapper extends FrameLayout
{
    private final int[] mLocation = new int[2];
    private int mHeightMeasured;
    private WeakReference<View> mSticky;

    public FStickyWrapper(Context context, AttributeSet attrs)
    {
        super(context, attrs);
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
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mHeightMeasured = getMeasuredHeight();
    }

    @Override
    public void onViewAdded(View child)
    {
        super.onViewAdded(child);
        if (getChildCount() > 1)
            throw new RuntimeException("FStickyWrapper can only add one child");

        checkChild(child);
        mSticky = new WeakReference<>(child);
        restoreHeight();
    }

    @Override
    public void onViewRemoved(View child)
    {
        super.onViewRemoved(child);
        saveHeight();
    }

    private void saveHeight()
    {
        final ViewGroup.LayoutParams params = getLayoutParams();
        if (params == null)
            return;

        final int height = mHeightMeasured;
        if (params.height != height)
        {
            params.height = height;
            setLayoutParams(params);
        }
    }

    private void restoreHeight()
    {
        final ViewGroup.LayoutParams params = getLayoutParams();
        if (params == null)
            return;

        final int height = ViewGroup.LayoutParams.WRAP_CONTENT;
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

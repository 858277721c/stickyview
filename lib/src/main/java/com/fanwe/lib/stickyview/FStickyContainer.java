package com.fanwe.lib.stickyview;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import java.util.Iterator;
import java.util.List;

class FStickyContainer extends ViewGroup
{
    private final int[] mLocation = new int[2];

    public FStickyContainer(Context context)
    {
        super(context);
        setPadding(0, 0, 0, 0);
        setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom)
    {
        super.setPadding(0, 0, 0, 0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int width = 0;
        int height = 0;

        final int count = getChildCount();
        for (int i = 0; i < count; i++)
        {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE)
                continue;

            final ViewGroup.LayoutParams params = child.getLayoutParams();
            child.measure(getChildMeasureSpec(widthMeasureSpec, 0, params.width),
                    getChildMeasureSpec(heightMeasureSpec, 0, params.height));

            width = Math.max(width, child.getMeasuredWidth());
            height += child.getMeasuredHeight();
        }

        width = Utils.getMeasureSize(Math.max(width, getSuggestedMinimumWidth()), widthMeasureSpec);
        height = Utils.getMeasureSize(Math.max(height, getSuggestedMinimumHeight()), heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        int top = 0;

        final int count = getChildCount();
        for (int i = 0; i < count; i++)
        {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE)
                continue;

            if (i == 0)
                top = child.getTop();

            child.layout(0, top, child.getMeasuredWidth(), top + child.getMeasuredHeight());
            top = child.getBottom();
        }
    }

    public void performSticky(List<FStickyWrapper> listWrapper)
    {
        if (listWrapper == null || listWrapper.isEmpty())
            return;

        getLocationOnScreen(mLocation);

        final Iterator<FStickyWrapper> it = listWrapper.iterator();
        while (it.hasNext())
        {
            final FStickyWrapper item = it.next();
            if (item.getSticky() == null)
            {
                it.remove();
                continue;
            }

            performStickyInternal(item);
        }
    }

    private void performStickyInternal(FStickyWrapper wrapper)
    {
        final View child = wrapper.getSticky();
        if (child.getParent() == this)
        {
            // check remove
            final View lastChild = getChildAt(getChildCount() - 1);
            if (child == lastChild)
            {
                if (wrapper.getLocation()[1] > getBoundY(false))
                {
                    addViewTo(child, wrapper);
                }
            }
        } else
        {
            // check sticky
            if (wrapper.getLocation()[1] <= getBoundY(true))
            {
                addViewTo(child, this);
            }
        }
    }

    private int getBoundY(boolean sticky)
    {
        final int count = getChildCount();
        if (count <= 0)
            return mLocation[1];

        final View lastChild = getChildAt(count - 1);
        final int y = mLocation[1] + (sticky ? lastChild.getBottom() : lastChild.getTop());
        return y;
    }

    private static void addViewTo(View child, ViewGroup parent)
    {
        final ViewParent childParent = child.getParent();
        if (childParent == parent)
            return;

        try
        {
            if (childParent instanceof ViewGroup)
                ((ViewGroup) childParent).removeView(child);

            parent.addView(child);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

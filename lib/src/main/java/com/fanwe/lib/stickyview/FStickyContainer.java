package com.fanwe.lib.stickyview;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class FStickyContainer extends ViewGroup
{
    private final int[] mLocation = new int[2];
    private final List<FStickyWrapper> mListWrapper = new ArrayList<>();
    private FStickyWrapper mTarget;

    private int mTotalHeight;
    private int mMinY;
    private int mMaxY;
    private boolean mIsReadyToMove;

    private boolean mIsDebug;

    public FStickyContainer(Context context)
    {
        super(context);
        setPadding(0, 0, 0, 0);
    }

    public void setDebug(boolean debug)
    {
        mIsDebug = debug;
    }

    private String getDebugTag()
    {
        return "FSticky";
    }

    public void addSticky(FStickyWrapper wrapper)
    {
        if (wrapper == null)
            return;
        if (mListWrapper.contains(wrapper))
            return;

        mListWrapper.add(wrapper);
    }

    public void removeSticky(FStickyWrapper wrapper)
    {
        if (wrapper == null)
            return;

        if (mListWrapper.remove(wrapper))
        {
            final View sticky = wrapper.getSticky();
            final int index = indexOfChild(sticky);
            if (index >= 0)
            {
                removeViewAt(index);
                wrapper.addView(sticky);
            }
        }
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom)
    {
        super.setPadding(0, 0, 0, 0);
    }

    @Override
    public void setLayoutParams(LayoutParams params)
    {
        if (params != null)
        {
            params.width = LayoutParams.MATCH_PARENT;
            params.height = LayoutParams.MATCH_PARENT;
        }
        super.setLayoutParams(params);
    }

    @Override
    public void onViewAdded(View child)
    {
        super.onViewAdded(child);
        if (mIsDebug)
            Log.i(getDebugTag(), "onViewAdded: " + child + " count:" + getChildCount());
        setReadyToMove(false);
    }

    @Override
    public void onViewRemoved(View child)
    {
        super.onViewRemoved(child);
        if (mIsDebug)
            Log.i(getDebugTag(), "onViewRemoved: " + child + " count:" + getChildCount());
        setReadyToMove(false);

        FStickyWrapper target = null;

        final int count = getChildCount();
        if (count > 0)
        {
            final View lastChild = getChildAt(count - 1);
            for (FStickyWrapper item : mListWrapper)
            {
                if (item.getSticky() == lastChild)
                {
                    target = item;
                    break;
                }
            }
        }

        setTarget(target);
    }

    private void setReadyToMove(boolean readyToMove)
    {
        mIsReadyToMove = readyToMove;
        if (mIsDebug)
            Log.e(getDebugTag(), "setReadyToMove: " + readyToMove + (readyToMove ? (" (" + mMinY + "," + mMaxY + ")") : ""));
    }

    private void setTarget(FStickyWrapper target)
    {
        if (mTarget != target)
        {
            mTarget = target;
            if (mIsDebug)
                Log.i(getDebugTag(), "setTarget: " + (target == null ? "null" : target.getSticky()));
        }
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

            measureChild(child, widthMeasureSpec, heightMeasureSpec);

            width = Math.max(width, child.getMeasuredWidth());
            height += child.getMeasuredHeight();
        }

        if (count > 0)
        {
            mTotalHeight = height;
            mMinY = getChildAt(count - 1).getMeasuredHeight() - mTotalHeight;
            mMaxY = 0;
            setReadyToMove(true);
        }

        width = Utils.getMeasureSize(width, widthMeasureSpec);
        height = Utils.getMeasureSize(height, heightMeasureSpec);
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

    public void performSticky()
    {
        if (mListWrapper == null || mListWrapper.isEmpty())
            return;

        getLocationOnScreen(mLocation);

        final Iterator<FStickyWrapper> it = mListWrapper.iterator();
        while (it.hasNext())
        {
            final FStickyWrapper wrapper = it.next();
            final View sticky = wrapper.getSticky();
            if (sticky == null)
            {
                it.remove();
                continue;
            }

            if (sticky.getParent() != this)
            {
                wrapper.updateLocation();
                if (wrapper.getLocation() <= getBoundSticky(true))
                {
                    addViewTo(sticky, this);
                    setTarget(wrapper);
                }
            }
        }

        moveViews();
    }

    private int getBoundSticky(boolean bottom)
    {
        final int count = getChildCount();
        if (count <= 0)
            return mLocation[1];

        final View lastChild = getChildAt(count - 1);
        return mLocation[1] + (bottom ? lastChild.getBottom() : lastChild.getTop());
    }

    private void moveViews()
    {
        if (!mIsReadyToMove)
            return;

        final int count = getChildCount();
        if (count <= 0)
            return;

        final FStickyWrapper target = mTarget;
        if (target == null)
            return;

        target.updateLocation();
        final int delta = target.getLocationDelta();
        if (delta == 0)
            return;

        final View firstChild = getChildAt(0);
        final int legalDelta = getLegalDelta(firstChild.getTop(), mMinY, mMaxY, delta);
        if (legalDelta == 0)
        {
            // (legalDelta == 0) 已经不能拖动，检查是否需要移除Sticky
            if (delta > 0)
            {
                final int firstTop = firstChild.getTop();
                final int targetLocation = target.getLocation();
                final int bound = getBoundSticky(false);
                if (firstTop >= 0 && targetLocation > bound)
                {
                    if (mIsDebug)
                        Log.i(getDebugTag(), "try remove child: " + target.getSticky());
                    addViewTo(target.getSticky(), target);
                }
            }

            return;
        }

        boolean offset = false;
        if (legalDelta < 0)
            offset = target.getLocation() < getBoundSticky(false);
        else
            offset = target.getLocation() > getBoundSticky(false);

        if (offset)
        {
            for (int i = 0; i < count; i++)
            {
                getChildAt(i).offsetTopAndBottom(legalDelta);
            }
        }
    }

    private static void addViewTo(View child, ViewGroup parent)
    {
        if (child == null)
            return;

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

    private static int getLegalDelta(int current, int min, int max, int delta)
    {
        if (delta == 0)
            return 0;

        final int future = current + delta;
        if (future < min)
        {
            delta += (min - future);
        } else if (future > max)
        {
            delta += (max - future);
        }
        return delta;
    }
}

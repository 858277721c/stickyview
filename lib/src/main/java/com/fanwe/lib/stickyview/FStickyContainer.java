package com.fanwe.lib.stickyview;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class FStickyContainer extends ViewGroup
{
    private static final int MAX_STICKY = 1;

    private final int[] mLocation = new int[2];
    private final List<FStickyWrapper> mListWrapper = new ArrayList<>();

    private FStickyWrapper mTarget;
    private final Map<View, FStickyWrapper> mMapSticky = new HashMap<>();

    private final List<View> mListChildrenLast = new ArrayList<>();

    private int mMinYForTargetSticky;
    private int mMaxYForTargetSticky;
    private boolean mIsReadyToMove;

    private boolean mIsDebug;

    public FStickyContainer(Context context)
    {
        super(context);
    }

    public void setDebug(boolean debug)
    {
        mIsDebug = debug;
    }

    private String getDebugTag()
    {
        return "FSticky";
    }

    public void addStickyWrapper(FStickyWrapper wrapper)
    {
        if (wrapper == null)
            return;
        if (mListWrapper.contains(wrapper))
            return;

        mListWrapper.add(wrapper);
    }

    public void removeStickyWrapper(FStickyWrapper wrapper)
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
        mMapSticky.remove(child);
        mListChildrenLast.remove(child);

        final View lastChild = getChildAt(getChildCount() - 1);
        final FStickyWrapper target = lastChild == null ? null : mMapSticky.get(lastChild);

        setTarget(target);
    }

    private void setReadyToMove(boolean readyToMove)
    {
        mIsReadyToMove = readyToMove;
        if (mIsDebug)
            Log.e(getDebugTag(), "setReadyToMove: " + readyToMove + (readyToMove ? (" (" + mMinYForTargetSticky + "," + mMaxYForTargetSticky + ")") : ""));
    }

    private void setTarget(FStickyWrapper target)
    {
        if (mTarget != target)
        {
            mTarget = target;

            if (target != null)
            {
                mMapSticky.put(target.getSticky(), target);
            }

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

        if (mTarget != null && mTarget.getSticky() != null)
        {
            if (count > MAX_STICKY)
            {
                final List<View> list = getChildrenFromLast(MAX_STICKY + 1);
                if (list.size() != (MAX_STICKY + 1))
                    throw new RuntimeException();

                int max = 0;
                for (int i = 0; i < list.size() - 1; i++)
                {
                    final View item = list.get(i);
                    max += item.getMeasuredHeight();
                }

                mMaxYForTargetSticky = max;
                mMinYForTargetSticky = max - list.get(0).getMeasuredHeight();

                setReadyToMove(true);
            }
        }

        width = Utils.getMeasureSize(width, widthMeasureSpec);
        height = Utils.getMeasureSize(height, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        final int count = getChildCount();
        if (count < 0)
            return;

        if (count <= MAX_STICKY)
        {
            int top = 0;
            for (int i = 0; i < count; i++)
            {
                final View item = getChildAt(i);
                item.layout(0, top, item.getMeasuredWidth(), top + item.getMeasuredHeight());
                top = item.getBottom();

                if (mIsDebug)
                    Log.i(getDebugTag(), "onLayout order:" + item.getTop() + "," + item.getBottom() + " index:" + i);
            }
        } else
        {
            View lastChild = null;
            for (int i = 0; i < count; i++)
            {
                final View item = getChildAt(i);
                if (lastChild != null)
                {
                    int top = lastChild.getBottom();
                    item.layout(0, top, item.getMeasuredWidth(), top + item.getMeasuredHeight());
                    lastChild = item;
                } else
                {
                    if (i == count - (MAX_STICKY + 1))
                    {
                        item.layout(0, item.getTop(), item.getMeasuredWidth(), item.getTop() + item.getMeasuredHeight());
                        lastChild = item;
                    } else
                    {
                        item.layout(0, -item.getMeasuredHeight(), item.getMeasuredWidth(), 0);
                    }
                }

                if (mIsDebug)
                    Log.i(getDebugTag(), "onLayout:" + item.getTop() + "," + item.getBottom() + " index:" + i);
            }
        }
    }

    private int getBoundSticky(boolean bottom)
    {
        final int count = getChildCount();
        if (count <= 0)
            return mLocation[1];

        final View lastChild = getChildAt(count - 1);
        return mLocation[1] + (bottom ? lastChild.getBottom() : lastChild.getTop());
    }

    public void performSticky()
    {
        if (mListWrapper.isEmpty())
            return;

        getLocationOnScreen(mLocation);

        for (final FStickyWrapper item : mListWrapper)
        {
            if (!Utils.isViewAttached(item))
                continue;

            final View sticky = item.getSticky();
            if (sticky == null)
                continue;

            if (sticky.getParent() != this)
            {
                item.updateLocation();
                final int location = item.getLocation();
                final int bound = getBoundSticky(true);
                if (location <= bound)
                {
                    if (mIsDebug)
                        Log.i(getDebugTag(), "try add sticky:" + location + "," + bound + " " + sticky);

                    post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            addViewTo(sticky, FStickyContainer.this);
                            setTarget(item);
                        }
                    });
                }
            }
        }

        moveViews();
    }

    private void moveViews()
    {
        final FStickyWrapper target = mTarget;
        if (target == null)
            return;

        final View targetSticky = target.getSticky();
        if (targetSticky == null)
            return;

        final int delta = target.updateLocation();
        if (delta == 0)
            return;

        if (mIsReadyToMove)
        {
            final int legalDelta = getLegalDelta(targetSticky.getTop(), mMinYForTargetSticky, mMaxYForTargetSticky, delta);
            if (legalDelta == 0)
            {
                // 已经不能拖动，检查是否需要移除
                detachIfNeed(delta, target, targetSticky);
                return;
            }

            final int location = target.getLocation();
            final int bound = getBoundSticky(false);

            final boolean offset = legalDelta < 0 ? location < bound : location > bound;

            if (offset)
            {
                final List<View> list = getChildrenFromLast(MAX_STICKY + 1);
                for (View item : list)
                {
                    item.offsetTopAndBottom(legalDelta);
                }
            }
        } else
        {
            detachIfNeed(delta, target, targetSticky);
        }
    }

    private void detachIfNeed(int delta, final FStickyWrapper target, final View targetSticky)
    {
        if (delta > 0)
        {
            final int location = target.getLocation();
            final int bound = getBoundSticky(false);
            if (location > bound)
            {
                if (mIsDebug)
                    Log.i(getDebugTag(), "try remove child:" + location + "," + bound + " " + targetSticky);

                post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        addViewTo(targetSticky, target);
                    }
                });
            }
        }
    }

    private List<View> getChildrenFromLast(int count)
    {
        mListChildrenLast.clear();

        final int childCount = getChildCount();
        if (count > 0 && childCount > 0)
        {
            int start = childCount - count;
            if (start < 0)
                start = 0;

            for (int i = start; i < childCount; i++)
            {
                final View child = getChildAt(i);
                if (child == null)
                    throw new NullPointerException();

                mListChildrenLast.add(child);
            }
        }

        return mListChildrenLast;
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

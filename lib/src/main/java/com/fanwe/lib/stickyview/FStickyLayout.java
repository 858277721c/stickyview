package com.fanwe.lib.stickyview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FStickyLayout extends FrameLayout
{
    private final FStickyContainer mStickyContainer;
    private final List<FStickyWrapper> mListWrapper = new ArrayList<>();

    public FStickyLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mStickyContainer = new FStickyContainer(context);
        mStickyContainer.setOrientation(LinearLayout.VERTICAL);
    }

    public void addSticky(FStickyWrapper wrapper)
    {
        if (wrapper == null)
            return;
        if (wrapper.getChildCount() != 1)
            throw new IllegalArgumentException("FStickyWrapper's child not found");
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
            final int index = mStickyContainer.indexOfChild(sticky);
            if (index >= 0)
            {
                mStickyContainer.removeViewAt(index);
                wrapper.addView(sticky);
            }
        }
    }

    public void findAllSticky()
    {
        final View child = getChildAt(0);
        if (child == null)
            return;

        final List<FStickyWrapper> listWrapper = getAllWrapper(child);
        for (FStickyWrapper item : listWrapper)
        {
            addSticky(item);
        }
    }

    @Override
    public void onViewAdded(View child)
    {
        super.onViewAdded(child);
        if (getChildCount() > 2)
            throw new RuntimeException("can not add more child");

        if (child != mStickyContainer)
            addView(mStickyContainer);
    }

    @Override
    public void onViewRemoved(View child)
    {
        super.onViewRemoved(child);
        if (child != mStickyContainer)
            removeView(mStickyContainer);
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        final ViewTreeObserver observer = getViewTreeObserver();
        if (observer.isAlive())
            observer.addOnPreDrawListener(mOnPreDrawListener);
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        final ViewTreeObserver observer = getViewTreeObserver();
        if (observer.isAlive())
            observer.removeOnPreDrawListener(mOnPreDrawListener);
    }

    private final ViewTreeObserver.OnPreDrawListener mOnPreDrawListener = new ViewTreeObserver.OnPreDrawListener()
    {
        @Override
        public boolean onPreDraw()
        {
            if (!mListWrapper.isEmpty())
            {
                mStickyContainer.updateLocation();
                checkSticky();
            }
            return true;
        }
    };

    private void checkSticky()
    {
        final Iterator<FStickyWrapper> it = mListWrapper.iterator();
        while (it.hasNext())
        {
            final FStickyWrapper item = it.next();
            if (item.getSticky() == null)
            {
                it.remove();
                continue;
            }

            mStickyContainer.performSticky(item);
        }
    }

    private static List<FStickyWrapper> getAllWrapper(View view)
    {
        final List<FStickyWrapper> list = new ArrayList<>();

        if (view instanceof ViewGroup && !(view instanceof FStickyLayout))
        {
            if (view instanceof FStickyWrapper)
            {
                list.add((FStickyWrapper) view);
            } else
            {
                final ViewGroup viewGroup = (ViewGroup) view;
                final int count = viewGroup.getChildCount();
                for (int i = 0; i < count; i++)
                {
                    final View child = viewGroup.getChildAt(i);
                    list.addAll(getAllWrapper(child));
                }
            }
        }
        return list;
    }
}

package com.fanwe.lib.stickyview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FStickyLayout extends FrameLayout
{
    private final FStickyContainer mStickyContainer;
    private final List<FStickyWrapper> mListSticky = new ArrayList<>();

    public FStickyLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mStickyContainer = new FStickyContainer(context);
        mStickyContainer.setOrientation(LinearLayout.VERTICAL);
    }

    public void addSticky(FStickyWrapper view)
    {
        if (view == null)
            return;
        if (view.getChildCount() != 1)
            throw new IllegalArgumentException("view must add one child");
        if (mListSticky.contains(view))
            return;

        mListSticky.add(view);
    }

    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();
        if (getChildCount() > 2)
            throw new RuntimeException("FStickyWrapper can only add one child");

        addView(mStickyContainer);
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
            if (!mListSticky.isEmpty())
            {
                mStickyContainer.updateLocation();
                checkSticky();
            }
            return true;
        }
    };

    private void checkSticky()
    {
        final Iterator<FStickyWrapper> it = mListSticky.iterator();
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
}

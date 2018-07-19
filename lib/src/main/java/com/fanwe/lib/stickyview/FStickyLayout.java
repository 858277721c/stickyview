package com.fanwe.lib.stickyview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

public class FStickyLayout extends FrameLayout
{
    private final FStickyContainer mStickyContainer;
    private final boolean mAutoFind;

    public FStickyLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mStickyContainer = new FStickyContainer(context);

        if (attrs != null)
        {
            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.lib_sticky_sticky_layout);
            mAutoFind = a.getBoolean(R.styleable.lib_sticky_sticky_layout_autoFind, false);
            a.recycle();
        } else
        {
            mAutoFind = false;
        }
    }

    public void addSticky(FStickyWrapper wrapper)
    {
        mStickyContainer.addSticky(wrapper);
    }

    public void removeSticky(FStickyWrapper wrapper)
    {
        mStickyContainer.removeSticky(wrapper);
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
        {
            addView(mStickyContainer);
            if (mAutoFind)
                findAllSticky();
        }
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
            mStickyContainer.performSticky();
            return true;
        }
    };

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

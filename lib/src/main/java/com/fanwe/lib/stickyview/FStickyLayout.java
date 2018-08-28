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
        mStickyContainer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

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

    /**
     * 设置调试模式，日志tag:{@link FStickyContainer#getDebugTag()}
     *
     * @param debug
     */
    public void setDebug(boolean debug)
    {
        mStickyContainer.setDebug(debug);
    }

    /**
     * 添加Sticky
     *
     * @param wrapper
     */
    public void addStickyWrapper(FStickyWrapper wrapper)
    {
        mStickyContainer.addStickyWrapper(wrapper);
    }

    /**
     * 移除Sticky
     *
     * @param wrapper
     */
    public void removeStickyWrapper(FStickyWrapper wrapper)
    {
        mStickyContainer.removeStickyWrapper(wrapper);
    }

    /**
     * 设置显示粘在顶部的最大数量，默认显示1个
     *
     * @param count
     */
    public void setMaxStickyCount(int count)
    {
        mStickyContainer.setMaxStickyCount(count);
    }

    /**
     * 添加当前对象下的所有Sticky
     */
    public void findAllStickyWrapper()
    {
        final View child = getChildAt(0);
        if (child == null)
            return;

        final List<FStickyWrapper> listWrapper = getAllWrapper(child);
        for (FStickyWrapper item : listWrapper)
        {
            addStickyWrapper(item);
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
                findAllStickyWrapper();
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

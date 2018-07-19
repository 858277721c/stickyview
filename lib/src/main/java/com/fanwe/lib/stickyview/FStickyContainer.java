package com.fanwe.lib.stickyview;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;

class FStickyContainer extends LinearLayout
{
    private final int[] mLocation = new int[2];

    public FStickyContainer(Context context)
    {
        super(context);
        setOrientation(VERTICAL);
    }

    public void updateLocation()
    {
        getLocationOnScreen(mLocation);
    }

    public void performSticky(FStickyWrapper wrapper)
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
        final int childCount = getChildCount();
        int boundY = 0;

        if (childCount > 0)
        {
            final View lastChild = getChildAt(childCount - 1);
            boundY = mLocation[1] + (sticky ? lastChild.getBottom() : lastChild.getTop());
        } else
        {
            boundY = mLocation[1];
        }

        return boundY;
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

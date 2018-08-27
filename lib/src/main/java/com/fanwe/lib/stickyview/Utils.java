package com.fanwe.lib.stickyview;

import android.os.Build;
import android.view.View;

class Utils
{
    public static int getMeasureSize(int size, int measureSpec)
    {
        int result = 0;

        final int modeSpec = View.MeasureSpec.getMode(measureSpec);
        final int sizeSpec = View.MeasureSpec.getSize(measureSpec);

        switch (modeSpec)
        {
            case View.MeasureSpec.UNSPECIFIED:
                result = size;
                break;
            case View.MeasureSpec.EXACTLY:
                result = sizeSpec;
                break;
            case View.MeasureSpec.AT_MOST:
                result = Math.min(size, sizeSpec);
                break;
        }
        return result;
    }

    public static boolean isViewAttached(View view)
    {
        if (view == null)
            return false;

        if (Build.VERSION.SDK_INT >= 19)
            return view.isAttachedToWindow();
        else
            return view.getWindowToken() != null;
    }
}

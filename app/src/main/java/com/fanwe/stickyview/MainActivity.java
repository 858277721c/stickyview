package com.fanwe.stickyview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.fanwe.lib.stickyview.FStickyLayout;

public class MainActivity extends AppCompatActivity
{
    private FStickyLayout mStickyLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStickyLayout = findViewById(R.id.sticky_layout);
        mStickyLayout.findAllSticky();
    }
}

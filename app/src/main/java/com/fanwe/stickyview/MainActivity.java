package com.fanwe.stickyview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.fanwe.lib.stickyview.FStickyLayout;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    private FStickyLayout mStickyLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStickyLayout = findViewById(R.id.sticky_layout);
        mStickyLayout.setDebug(true);
    }

    @Override
    public void onClick(View v)
    {

    }
}

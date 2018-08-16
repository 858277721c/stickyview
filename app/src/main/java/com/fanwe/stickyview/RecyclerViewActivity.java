package com.fanwe.stickyview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.Button;

import com.fanwe.lib.adapter.FSimpleRecyclerAdapter;
import com.fanwe.lib.adapter.viewholder.FRecyclerViewHolder;
import com.fanwe.lib.stickyview.FStickyLayout;
import com.fanwe.lib.stickyview.FStickyWrapper;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewActivity extends AppCompatActivity
{
    private FStickyLayout mStickyLayout;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_recyclerview);
        mStickyLayout = findViewById(R.id.sticky_layout);
        mStickyLayout.setDebug(true);

        mRecyclerView = findViewById(R.id.recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);

        List<DataModel> list = new ArrayList<>();
        for (int i = 0; i < 100; i++)
        {
            DataModel model = new DataModel();
            model.name = String.valueOf(i);
            list.add(model);
        }
        mAdapter.getDataHolder().setData(list);
    }

    private final FSimpleRecyclerAdapter<DataModel> mAdapter = new FSimpleRecyclerAdapter<DataModel>()
    {
        @Override
        public int getLayoutId(ViewGroup parent, int viewType)
        {
            return R.layout.item_list;
        }

        @Override
        public void onBindData(FRecyclerViewHolder<DataModel> holder, int position, DataModel model)
        {
            Button btn = holder.get(R.id.btn);
            Button btn_sticky = holder.get(R.id.btn_sticky);
            FStickyWrapper sticky_wrapper = holder.get(R.id.sticky_wrapper);

            btn.setText(model.toString());
            btn_sticky.setText(model.toString());

            if (position == 3)
                mStickyLayout.addSticky(sticky_wrapper);
        }
    };


}

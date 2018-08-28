package com.fanwe.stickyview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;

import com.fanwe.lib.adapter.FSimpleRecyclerAdapter;
import com.fanwe.lib.adapter.viewholder.FRecyclerViewHolder;
import com.fanwe.lib.stickyview.FStickyLayout;
import com.fanwe.lib.stickyview.FStickyWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        private final Map<DataModel, FStickyWrapper> mMapWrapper = new HashMap<>();

        @Override
        public int getLayoutId(ViewGroup parent, int viewType)
        {
            return R.layout.item_list;
        }

        @Override
        public void onBindData(FRecyclerViewHolder<DataModel> holder, int position, DataModel model)
        {
            Button btn = holder.get(R.id.btn);
            btn.setText(model.toString());

            ViewGroup fl_content = holder.get(R.id.fl_content);
            fl_content.removeAllViews();

            FStickyWrapper sticky_wrapper = mMapWrapper.get(model);
            if (sticky_wrapper == null)
            {
                sticky_wrapper = (FStickyWrapper) LayoutInflater.from(getContext()).inflate(R.layout.item_sticky, fl_content, false);
                mMapWrapper.put(model, sticky_wrapper);
            }
            fl_content.addView(sticky_wrapper);

            Button btn_sticky = sticky_wrapper.getSticky().findViewById(R.id.btn_sticky);
            btn_sticky.setText(model.toString());

            mStickyLayout.addStickyWrapper(sticky_wrapper);
        }
    };

}

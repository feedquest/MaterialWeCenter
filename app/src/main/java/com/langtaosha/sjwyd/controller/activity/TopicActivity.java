package com.langtaosha.sjwyd.controller.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.util.TypedValue;

import com.langtaosha.sjwyd.Client;
import com.langtaosha.sjwyd.Config;
import com.langtaosha.sjwyd.R;
import com.langtaosha.sjwyd.controller.adapter.ExploreViewAdapter;
import com.langtaosha.sjwyd.models.ExploreItem;
import com.langtaosha.sjwyd.models.Responses;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TopicActivity extends AppCompatActivity {

    List<ExploreItem> mList = new ArrayList<>();
    ExploreViewAdapter mAdapter;
    @BindView(R.id.post_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    //初始化一定处于刷新状态
    private boolean loading = true;
    //记录当前已经加载的页数
    private int page = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ActionBar ab = getSupportActionBar();
        if (ab != null)
            ab.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        setTitle(intent.getStringExtra(Config.INT_TOPIC_NAME));
        final int id = intent.getIntExtra(Config.INT_TOPIC_ID, -1);

        // 实例化刷新布局
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_red_light,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light);
        TypedValue typed_value = new TypedValue();
        this.getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
        mSwipeRefreshLayout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typed_value.resourceId));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 下拉刷新
                page = 1;
                // 下拉刷新
                new LoadPostsTask(id).execute();
            }
        });
        mSwipeRefreshLayout.setRefreshing(true);

        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ExploreViewAdapter(this, mList);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());


        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int visibleItemCount = mLayoutManager.getChildCount();
                int totalItemCount = mLayoutManager.getItemCount();
                int pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();
                if (!loading) {
                    //当前不在加载，才进行新的加载
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        // 达到底部加载更多

                        //设置flag，标记当前正在刷新(加载）
                        loading = true;
                        mSwipeRefreshLayout.setRefreshing(true);
                        new LoadPostsTask(id).execute();
                    }
                }
            }
        });
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        new LoadPostsTask(id).execute();
    }

    class LoadPostsTask extends AsyncTask<Void, Void, Void> {

        int topicID;
        Responses<ExploreItem> responses;

        LoadPostsTask(int id) {
            topicID = id;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            responses = Client.getInstance().posts(topicID, page);
            if (responses.getErrno() == 1) {
                if (page < 2)
                    mList.clear();
                mList.addAll(responses.getRsm());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mSwipeRefreshLayout.setRefreshing(false);
            mAdapter.notifyDataSetChanged();
            //加载完成，更新flag
            page++;
            loading = false;
        }
    }
}

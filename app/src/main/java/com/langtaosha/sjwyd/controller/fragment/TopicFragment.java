package com.langtaosha.sjwyd.controller.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.langtaosha.sjwyd.Client;
import com.langtaosha.sjwyd.R;
import com.langtaosha.sjwyd.controller.adapter.TopicViewAdapter;
import com.langtaosha.sjwyd.models.Responses;
import com.langtaosha.sjwyd.models.Topic;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TopicFragment extends Fragment {

    private static int POST_ACTIVITY = 1;
    @BindView(R.id.dynamic_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    private List<Topic> mList = new ArrayList<>();
    private TopicViewAdapter mAdapter;
    //初始化一定处于刷新状态
    private boolean loading = true;
    //记录当前已经加载的页数
    private int page = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        // 实例化刷新布局
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_red_light,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light);
        TypedValue typed_value = new TypedValue();
        getActivity().getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
        mSwipeRefreshLayout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typed_value.resourceId));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 下拉刷新
                page = 1;
                // 下拉刷新
                new LoadTopicList().execute();
            }
        });
        mSwipeRefreshLayout.setRefreshing(true);
        // 实例化RecyclerView
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new TopicViewAdapter(getActivity(), mList);
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
                        new LoadTopicList().execute();
                    }
                }
            }
        });
        // 开始载入问题操作
        new LoadTopicList().execute();
    }

    /**
     * 加载话题页面的异步任务
     */
    private class LoadTopicList extends AsyncTask<Void, Integer, Integer> {

        private Responses<Topic> responses;

        @Override
        protected Integer doInBackground(Void... params) {
            responses = Client.getInstance().hotTopics(page);
            if (responses.getErrno() == 1) {
                List<Topic> rsm = responses.getRsm();
                if (page < 2)
                    mList.clear();
                mList.addAll(rsm);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            mSwipeRefreshLayout.setRefreshing(false);
            mAdapter.notifyDataSetChanged();
            //加载完成，更新flag
            page++;
            loading = false;
        }
    }
}

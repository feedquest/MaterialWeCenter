package com.langtaosha.sjwyd.controller.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.langtaosha.sjwyd.Client;
import com.langtaosha.sjwyd.R;
import com.langtaosha.sjwyd.controller.adapter.DynamicViewAdapter;
import com.langtaosha.sjwyd.models.Dynamic;
import com.langtaosha.sjwyd.models.Responses;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class HomeFragment extends Fragment {

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.dynamic_list)
    RecyclerView mRecyclerView;
    private List<Dynamic> mList = new ArrayList<>();
    private DynamicViewAdapter mAdapter;
    private DrawerLayout drawerLayout;
    private Client client = Client.getInstance();
    //初始化一定处于刷新状态
    private boolean loading = true;
    //记录当前已经加载的页数
    private int page = 0;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        // 实例化刷新布局
        mAdapter = new DynamicViewAdapter(getActivity(), mList);
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
                page = 0;
                new LoadDynamicList().execute();
            }
        });
        mSwipeRefreshLayout.setRefreshing(true);
        // 实例化RecyclerView
        mRecyclerView.setAdapter(mAdapter);
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
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
                        new LoadDynamicList().execute();
                    }
                }
            }
        });
        // 开始载入动态操作
        new LoadDynamicList().execute();
    }

    private class LoadDynamicList extends AsyncTask<Void, Void, Void> {

        private Responses<Dynamic> responses;

        @Override
        protected Void doInBackground(Void... voids) {
            responses = client.getDynamic(page);
            if (responses.getErrno() == 1) {
                List<Dynamic> rsm = responses.getRsm();
                if (page == 0)
                    mList.clear();
                mList.addAll(rsm);
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

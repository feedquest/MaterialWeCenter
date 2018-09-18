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
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.langtaosha.sjwyd.Client;
import com.langtaosha.sjwyd.Config;
import com.langtaosha.sjwyd.R;
import com.langtaosha.sjwyd.controller.adapter.QuestionAdapter;
import com.langtaosha.sjwyd.models.QuestionDetail;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class QuestionActivity extends AppCompatActivity {

    private final int ScrollOffset = 4;
    private boolean isFirstRefresh = true;
    private int questionID;
    private QuestionDetail questionDetail;
    private QuestionAdapter questionDetailAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    @BindView(R.id.button_publish)
    FloatingActionButton btnPublish;
    private Client client = Client.getInstance();

    private static final int POST_ANSWER = 2;

    @OnClick(R.id.button_publish)
    void answer() {
        Intent intent = new Intent(this, PostAnswerActivity.class);
        intent.putExtra(Config.INT_QUESTION_ID, questionID);
        intent.putExtra(Config.INT_QUESTION_TITLE, questionDetail.getQuestion_info().getQuestion_content());
        startActivityForResult(intent, POST_ANSWER);
//        String text = answerContent.getText().toString();
//        if (!text.isEmpty()){
//            new PublishTask().execute();
//        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        ButterKnife.bind(this);

        //init toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_questionDetail);
        setSupportActionBar(toolbar);
        setTitle("");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ActionBar ab = getSupportActionBar();
        if (ab != null)
            ab.setDisplayHomeAsUpEnabled(true);

        //get intent
        Intent mIntent = getIntent();
        questionID = mIntent.getIntExtra(Config.INT_QUESTION_ID, -1);

        //init swipeRefreshLayout
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout_question);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_red_light,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light);
        TypedValue typed_value = new TypedValue();
        getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize,
                typed_value, true);
        swipeRefreshLayout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize
                (typed_value.resourceId));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 下拉刷新
                new LoadAnswers().execute();
            }
        });

        //init recyclerView
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView_answerList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // 自动隐藏发布按钮
                if (Math.abs(dy) > ScrollOffset)
                    if (dy > 0)
                        btnPublish.hide(true);
                    else
                        btnPublish.show(true);
            }
        });
        swipeRefreshLayout.setRefreshing(true);
        new LoadAnswers().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_question, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            //返回顶部
            case R.id.action_toTop:
                recyclerView.smoothScrollToPosition(0);
                break;
            //刷新
            case R.id.action_refresh:
                swipeRefreshLayout.setRefreshing(true);
                new LoadAnswers().execute();
                break;
            case R.id.action_share:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, Config.HOST_NAME + "?/question/" + questionID);
                sendIntent.setType("text/html");
                startActivity(sendIntent);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //发布答案成功后刷新
        if (requestCode == POST_ANSWER && resultCode == PostAnswerActivity.POST_ANSWER_POS)
            new LoadAnswers().execute();
    }

    //异步获取答案列表
    private class LoadAnswers extends AsyncTask<Integer, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            try {
                Log.e("Load", "load has started");
                questionDetail = (QuestionDetail) client.getQuestion(questionID).getRsm();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            questionDetailAdapter = new QuestionAdapter
                    (QuestionActivity.this, questionDetail);
            recyclerView.setAdapter(questionDetailAdapter);
            questionDetailAdapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
            if (isFirstRefresh) {
                isFirstRefresh = false;
            } else Toast.makeText(QuestionActivity.this, "更新完成", Toast.LENGTH_SHORT).show();
        }
    }

}

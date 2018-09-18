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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.langtaosha.sjwyd.Client;
import com.langtaosha.sjwyd.Config;
import com.langtaosha.sjwyd.R;
import com.langtaosha.sjwyd.controller.adapter.ArticleAdapter;
import com.langtaosha.sjwyd.models.Article;
import com.langtaosha.sjwyd.models.Response;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ArticleActivity extends AppCompatActivity {

    private static final int REQUEST_COMMENT = 1;

    private final int ScrollOffset = 4;
    private boolean isFirstRefresh = true;
    private boolean isBtnClose;
    private int articleID;
    private Article article;
    private ArticleAdapter articleAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private Client client = Client.getInstance();
    private ImageButton publish;
    private EditText answerContent;
    @BindView(R.id.button_publish)
    FloatingActionButton btnPublish;


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
        articleID = mIntent.getIntExtra(Config.INT_ARTICLE_ID, -1);

        // answerContent=(EditText) findViewById(R.id.edit_content_answer);

//        publish=(ImageButton) findViewById(R.id.imageButton_publishAnswer);
//
//        publish.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String text = answerContent.getText().toString();
//                if(!text.isEmpty()){
//                    new CommentTask().execute();
//                }
//            }
//        });

        //init fab
        // floatingActionButton=(FABRevealLayout ) findViewById(R.id.fab_reveal_layout);
//        floatingActionButton.setOnRevealChangeListener(new OnRevealChangeListener() {
//            @Override
//            public void onMainViewAppeared(FABRevealLayout fabRevealLayout, View mainView) {
//                isBtnClose=true;
//            }
//
//            @Override
//            public void onSecondaryViewAppeared(FABRevealLayout fabRevealLayout, View secondaryView) {
//                isBtnClose=false;
//            }
//        });

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
                new LoadArticle().execute();
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
        new LoadArticle().execute();
    }

    @OnClick(R.id.button_publish)
    void comment() {
        Intent intent = new Intent(ArticleActivity.this, PostCommentActivity.class);
        intent.putExtra(Config.INT_ARTICLE_ID, articleID);
        intent.putExtra(Config.INT_ARTICLE_TITLE, article.getArticle_info().getTitle());
        startActivityForResult(intent, REQUEST_COMMENT);
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
                new LoadArticle().execute();
                break;
            case R.id.action_share:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, Config.HOST_NAME + "?/article/" + articleID);
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
        new LoadArticle().execute();
    }

    //异步获取答案列表
    private class LoadArticle extends AsyncTask<Integer, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            try {
                Log.e("Load", "load has started");
                article = client.getArticle(articleID).getRsm();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            articleAdapter = new ArticleAdapter(ArticleActivity.this, article);
            recyclerView.setAdapter(articleAdapter);
            articleAdapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
            if (isFirstRefresh) {
                isFirstRefresh = false;
            } else Toast.makeText(ArticleActivity.this, "更新完成", Toast.LENGTH_SHORT).show();
        }
    }

    //异步发布答案
    private class CommentTask extends AsyncTask<Void, Void, Void> {

        String content;
        Response<Object> response;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            content = answerContent.getText().toString();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Client client = Client.getInstance();
            response = client.saveComment(articleID, content);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (response == null) // 未知错误
                Toast.makeText(ArticleActivity.this, "未知错误", Toast.LENGTH_SHORT).show();
            else if (response.getErrno() == 1) { // 发布成功
                Toast.makeText(ArticleActivity.this, "回答成功", Toast.LENGTH_SHORT).show();
                new LoadArticle().execute();
            } else                // 显示错误
                Toast.makeText(ArticleActivity.this, response.getErr(), Toast.LENGTH_SHORT).show();

            isBtnClose = true;
            answerContent.setText("");
        }
    }


}

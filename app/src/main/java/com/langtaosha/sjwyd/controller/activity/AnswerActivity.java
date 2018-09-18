package com.langtaosha.sjwyd.controller.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.nispok.snackbar.Snackbar;
import com.langtaosha.sjwyd.Client;
import com.langtaosha.sjwyd.Config;
import com.langtaosha.sjwyd.R;
import com.langtaosha.sjwyd.controller.adapter.AnswerDetailAdapter;
import com.langtaosha.sjwyd.models.AnswerComment;
import com.langtaosha.sjwyd.models.AnswerDetail;
import com.langtaosha.sjwyd.models.Response;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

public class AnswerActivity extends AppCompatActivity implements View.OnClickListener {

    private int answerID;
    private Client client = Client.getInstance();
    private AnswerDetail answerDetail;
    private List<AnswerComment> answerComments;
    private RecyclerView recyclerView;
    private AnswerDetailAdapter answerDetailAdapter;
    private EditText content;
    private Button publish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer);
        ButterKnife.bind(this);

        //获取intent
        Intent mIntent = getIntent();
        answerID = mIntent.getIntExtra("answerID", -1);

        //实例化recyclerView
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView_answer);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        //实例化EdiText和发布按钮
        content = (EditText) findViewById(R.id.edit_content_answerComment);
        publish = (Button) findViewById(R.id.btn_add_answerComment);
        publish.setOnClickListener(this);

        new LoadAnswerDetail().execute();
    }

    @Override
    public void onClick(View v) {
        String mContent = content.getText().toString();
        switch (v.getId()) {
            //发布评论
            case R.id.btn_add_answerComment:
                if (!mContent.isEmpty()) {
                    new PublishTask().execute();
                }
                break;
            default:
                break;
        }
    }

    //异步获取回答详细信息

    private class LoadAnswerDetail extends AsyncTask<Integer, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            try {
                answerDetail = client.getAnswer(answerID).getRsm();
                answerComments = client.getAnswerComments(answerID).getRsm();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            answerDetailAdapter = new AnswerDetailAdapter(AnswerActivity.this, answerDetail, answerComments);
            recyclerView.setAdapter(answerDetailAdapter);
            answerDetailAdapter.notifyDataSetChanged();
        }
    }

    //异步进行评论

    private class PublishTask extends AsyncTask<Void, Void, Void> {

        String mContent;
        Response<PublishAnswerComment> result2;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mContent = content.getText().toString();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Client client = Client.getInstance();
            ArrayList<String> strs = new ArrayList<>();
            strs.add(answerID + "");
            strs.add(mContent);
            result2 = client.postAction(Config.ActionType.PUBLISH_ANSWER_COMMENT, PublishAnswerComment.class, strs);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            answerDetailAdapter.notifyDataSetChanged();
            if (result2 == null) // 未知错误
                Snackbar.with(AnswerActivity.this).text("未知错误").show(AnswerActivity.this);
            else if (result2.getErrno() == 1) { // 发布成功
                new LoadAnswerDetail().execute();
                content.setText("");
            } else                // 显示错误
                Snackbar.with(AnswerActivity.this).text(result2.getErr()).show(AnswerActivity.this);
        }
    }

    //评论成功返回的json对象

    public static class PublishAnswerComment {

        private int item_id;
        private String type_name;

        public void setItem_id(int item_id) {
            this.item_id = item_id;
        }

        public void setType_name(String type_name) {
            this.type_name = type_name;
        }

        public int getItem_id() {
            return item_id;
        }

        public String getType_name() {
            return type_name;
        }
    }
}

package com.langtaosha.sjwyd.controller.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nispok.snackbar.Snackbar;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.langtaosha.sjwyd.Client;
import com.langtaosha.sjwyd.R;
import com.langtaosha.sjwyd.Util;
import com.langtaosha.sjwyd.models.PublishQuestion;
import com.langtaosha.sjwyd.models.Response;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.mthli.knife.KnifeText;
import me.gujun.android.taggroup.TagGroup;

public class PostActivity extends AppCompatActivity {

    private ArrayList<String> topics = new ArrayList<>();
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tag_group)
    TagGroup tagGroup;
    @BindView(R.id.edit_title)
    MaterialEditText editTitle;
    @BindView(R.id.edit_content)
    KnifeText editContent;
    @BindView(R.id.edit_topic)
    MaterialEditText editTopic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        ButterKnife.bind(this);
        // 初始化工具栏
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
        // 实例化控件
        editTitle.validate("\\w{5,}", "标题长度不能少于5个字");
        tagGroup.setOnTagClickListener(new TagGroup.OnTagClickListener() {
            @Override
            public void onTagClick(String tag) {
                topics.remove(tag);
                tagGroup.setTags(topics);
            }
        });
    }

    @OnClick(R.id.btn_add_topic)
    void addTopic() {
        topics.add(editTopic.getText().toString());
        tagGroup.setTags(topics);
        editTopic.setText("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_post:
                if (editTitle.validate())
                    new PublishTask().execute();
                break;
            case R.id.action_undo:
                editContent.undo();
                break;
            case R.id.action_redo:
                editContent.redo();
                break;
            case R.id.action_bold:
                editContent.bold(!editContent.contains(KnifeText.FORMAT_BOLD));
                break;
            case R.id.action_italic:
                editContent.italic(!editContent.contains(KnifeText.FORMAT_ITALIC));
                break;
            case R.id.action_quote:
                editContent.quote(!editContent.contains(KnifeText.FORMAT_QUOTE));
                break;
            case R.id.action_list_bulleted:
                editContent.bullet(!editContent.contains(KnifeText.FORMAT_BULLET));
                break;
            case R.id.action_insert_link:
                new MaterialDialog.Builder(this)
                        .title("插入链接")
                        .content("编辑链接地址")
                        .input("链接地址", "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                editContent.link(input.toString());
                            }
                        }).show();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 发布问题的异步任务
     */
    class PublishTask extends AsyncTask<Void, Void, Void> {

        private String title, content;
        private Response<PublishQuestion> result2;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            content = Util.htmlToBBcode(editContent.toHtml());
//            Log.d("CONTENT", content);
            title = editTitle.getText().toString();
        }

        @Override
        protected Void doInBackground(Void... params) {
            result2 = Client.getInstance().publishQuestion(title, content, topics);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (result2 == null) // 未知错误
                Snackbar.with(PostActivity.this).text("未知错误").show(PostActivity.this);
            else if (result2.getErrno() == 1)    // 发布成功
                PostActivity.this.finish();
            else                // 显示错误
                Snackbar.with(PostActivity.this).text(result2.getErr()).show(PostActivity.this);
        }

    }
}

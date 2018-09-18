package com.langtaosha.sjwyd.controller.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.langtaosha.sjwyd.Config;
import com.langtaosha.sjwyd.R;
import com.langtaosha.sjwyd.controller.activity.ArticleActivity;
import com.langtaosha.sjwyd.controller.activity.QuestionActivity;
import com.langtaosha.sjwyd.controller.activity.UserActivity;
import com.langtaosha.sjwyd.models.Dynamic;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class DynamicViewAdapter extends RecyclerView.Adapter<DynamicViewAdapter.ViewHolder> {

    private boolean questionOnly(int n) {
        return Dynamic.ADD_QUESTION <= n && n <= Dynamic.DEL_REDIRECT_QUESTION;
    }

    private boolean questionAndAnswer(int n) {
        return Dynamic.ANSWER_QUESTION <= n && n <= Dynamic.ADD_UNUSEFUL;
    }

    private boolean topic(int n) {
        return Dynamic.ADD_TOPIC <= n && n <= Dynamic.DELETE_RELATED_TOPIC;
    }

    private boolean articleOnly(int n) {
        return Dynamic.ADD_ARTICLE <= n && n <= Dynamic.ADD_AGREE_ARTICLE;
    }

    private boolean articleAndComment(int n) {
        return n == Dynamic.ADD_COMMENT_ARTICLE;
    }

    private static SparseArray<String> action;

    static {
        action = new SparseArray<String>() {
            {
                put(Dynamic.ADD_QUESTION, "发起了问题");
                put(Dynamic.MOD_QUESTON_TITLE, "修改了问题标题");
                put(Dynamic.MOD_QUESTION_DESCRI, "修改了问题描述");
                put(Dynamic.ADD_REQUESTION_FOCUS, "关注了该问题");
                put(Dynamic.REDIRECT_QUESTION, "设置了问题重定向");
                put(Dynamic.MOD_QUESTION_CATEGORY, "修改了问题分类");
                put(Dynamic.MOD_QUESTION_ATTACH, "修改了问题附件");
                put(Dynamic.DEL_REDIRECT_QUESTION, "删除了问题重定向");

                put(Dynamic.ANSWER_QUESTION, "回答了问题");
                put(Dynamic.ADD_AGREE, "赞同了回答");
                put(Dynamic.ADD_USEFUL, "感谢了作者");
                put(Dynamic.ADD_UNUSEFUL, "认为问题没有帮助");

                put(Dynamic.ADD_TOPIC, "创建了话题");
                put(Dynamic.MOD_TOPIC, "修改了话题");
                put(Dynamic.MOD_TOPIC_DESCRI, "修改了话题描述");
                put(Dynamic.MOD_TOPIC_PIC, "修改了话题图片");
                put(Dynamic.DELETE_TOPIC, "删除了话题");
                put(Dynamic.ADD_TOPIC_FOCUS, "添加了话题关注");
                put(Dynamic.ADD_RELATED_TOPIC, "添加了相关话题");
                put(Dynamic.DELETE_RELATED_TOPIC, "删除了相关话题");

                put(Dynamic.ADD_ARTICLE, "添加了文章");
                put(Dynamic.ADD_AGREE_ARTICLE, "赞同了文章");
                put(Dynamic.ADD_COMMENT_ARTICLE, "评论了文章");
            }
        };
    }


    private List<Dynamic> mList;
    private Context mContext;

    public DynamicViewAdapter(Context context, List<Dynamic> list) {
        this.mContext = context;
        this.mList = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dynamic, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Dynamic dynamic = mList.get(position);

        String avatarFile = dynamic.getUser_info().getAvatar_file();
        if (avatarFile != null && !avatarFile.isEmpty())
            Picasso.with(mContext)
                    .load(avatarFile)
                    .into(holder.avatarImg);
        final int associateActionType = dynamic.getAssociate_action();
        holder.dynamicUserName.setText(dynamic.getUser_info().getUser_name() + " " + action.get(associateActionType));

        if (questionOnly(associateActionType))
            setQuestionView(holder, dynamic);
        else if (questionAndAnswer(associateActionType))
            setQuestionAndAnswerView(holder, dynamic);
        else if (topic(associateActionType))
            setTopicView(holder, dynamic);
        else if (articleOnly(associateActionType))
            setArticleView(holder, dynamic);
        else if (articleAndComment(associateActionType))
            setArticleAndCommentView(holder, dynamic);
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    //101 - 110
    private void setQuestionView(ViewHolder holder, final Dynamic dynamic) {
        holder.dynamicContent.setVisibility(View.GONE);
        holder.dynamicTitle.setText(dynamic.getQuestion_info().getQuestion_content());
        holder.dynamicInfo.setText(dynamic.getQuestion_info().getAgree_count() + "次赞同 • "
                + dynamic.getQuestion_info().getAnswer_count() + "次回答");
        holder.dynamicTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, QuestionActivity.class);
                intent.putExtra("questionID", dynamic.getQuestion_info().getQuestion_id());
                mContext.startActivity(intent);
            }
        });
        holder.avatarImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, UserActivity.class);
                intent.putExtra("uid", dynamic.getUser_info().getUid());
                mContext.startActivity(intent);
            }
        });
    }

    //201 - 207
    private void setQuestionAndAnswerView(ViewHolder holder, final Dynamic dynamic) {
        holder.dynamicTitle.setText(Html.fromHtml(dynamic.getQuestion_info().getQuestion_content()));
        CharSequence message = Html.fromHtml(dynamic.getAnswer_info().getAnswer_content());
        if (message.length() > Config.MAX_LENGTH) {
            message = message.subSequence(0, Config.MAX_LENGTH);
            message = message + "...";
        }
        holder.dynamicContent.setVisibility(View.VISIBLE);
        holder.dynamicContent.setText(message);
        holder.dynamicInfo.setText(dynamic.getAnswer_info().getAgree_count() + "次赞同 • "
                + dynamic.getAnswer_info().getAgainst_count() + "次反对");
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, QuestionActivity.class);
                intent.putExtra("questionID", dynamic.getQuestion_info().getQuestion_id());
                mContext.startActivity(intent);
            }
        };
        holder.dynamicTitle.setOnClickListener(clickListener);
        holder.dynamicContent.setOnClickListener(clickListener);
        holder.avatarImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, UserActivity.class);
                intent.putExtra("uid", dynamic.getUser_info().getUid());
                mContext.startActivity(intent);
            }
        });
    }

    private void setTopicView(ViewHolder holder, Dynamic dynamic) {
        // TODO: 2016/1/30 话题view的填充。。。
        setQuestionView(holder, dynamic);
    }

    //501 - 502
    private void setArticleView(ViewHolder holder, final Dynamic dynamic) {
        holder.dynamicTitle.setText(Html.fromHtml(dynamic.getArticle_info().getTitle()));
        CharSequence message = Html.fromHtml(dynamic.getArticle_info().getMessage());
        if (message.length() > Config.MAX_LENGTH) {
            message = message.subSequence(0, Config.MAX_LENGTH);
            message = message + "...";
        }
        holder.dynamicContent.setVisibility(View.VISIBLE);
        holder.dynamicContent.setText(message);
        holder.dynamicInfo.setText(dynamic.getArticle_info().getViews() + "次浏览 • "
                + dynamic.getArticle_info().getComments() + "次回复");
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ArticleActivity.class);
                intent.putExtra(Config.INT_ARTICLE_ID, dynamic.getArticle_info().getId());
                mContext.startActivity(intent);
            }
        };
        holder.dynamicTitle.setOnClickListener(clickListener);
        holder.dynamicContent.setOnClickListener(clickListener);
    }

    //503
    private void setArticleAndCommentView(ViewHolder holder, final Dynamic dynamic) {
        holder.dynamicTitle.setText("文章： " + dynamic.getArticle_info().getTitle());
        holder.dynamicContent.setVisibility(View.VISIBLE);
        holder.dynamicContent.setText(dynamic.getComment_info().getMessage());
        holder.dynamicInfo.setText(dynamic.getComment_info().getVotes() + "个赞");
        holder.dynamicTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ArticleActivity.class);
                intent.putExtra(Config.INT_ARTICLE_ID, dynamic.getArticle_info().getId());
                mContext.startActivity(intent);
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.avatar_img)
        CircleImageView avatarImg;
        @BindView(R.id.dynamic_user_name)
        TextView dynamicUserName;
        @BindView(R.id.dynamic_title)
        TextView dynamicTitle;
        @BindView(R.id.dynamic_content)
        TextView dynamicContent;
        @BindView(R.id.dynamic_info)
        TextView dynamicInfo;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}

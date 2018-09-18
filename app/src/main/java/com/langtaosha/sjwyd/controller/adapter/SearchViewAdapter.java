package com.langtaosha.sjwyd.controller.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.langtaosha.sjwyd.Config;
import com.langtaosha.sjwyd.R;
import com.langtaosha.sjwyd.controller.activity.ArticleActivity;
import com.langtaosha.sjwyd.controller.activity.QuestionActivity;
import com.langtaosha.sjwyd.controller.activity.TopicActivity;
import com.langtaosha.sjwyd.controller.activity.UserActivity;
import com.langtaosha.sjwyd.models.SearchResult;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class SearchViewAdapter extends RecyclerView.Adapter<SearchViewAdapter.ViewHolder> {

    private List<SearchResult> mList;
    private Context mContext;

    public SearchViewAdapter(Context context, List<SearchResult> list) {
        this.mContext = context;
        this.mList = list;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final SearchResult result = mList.get(position);
        holder.title.setText(result.getName());
        switch (result.getType()) {
            case "users":
                Picasso.with(mContext).load(result.getDetail().getAvatar_file()).into(holder.avatarImg);
                holder.description.setText(String.format(Locale.CHINA, "%d次赞同 • %d次感谢 • %d位关注着",
                        result.getDetail().getAgree_count(),
                        result.getDetail().getThanks_count(),
                        result.getDetail().getFans_count()));
                holder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, UserActivity.class);
                        intent.putExtra("uid", result.getSearch_id());
                        mContext.startActivity(intent);
                    }
                });
                break;
            case "topics":
                Picasso.with(mContext).load(result.getDetail().getTopic_pic()).into(holder.avatarImg);
                holder.avatarImg.setDisableCircularTransformation(true);
                holder.description.setText(result.getDetail().getTopic_description());
                holder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, TopicActivity.class);
                        intent.putExtra(Config.INT_TOPIC_ID, result.getSearch_id());
                        intent.putExtra(Config.INT_TOPIC_NAME, result.getName());
                        mContext.startActivity(intent);
                    }
                });
                break;
            case "questions":
                holder.avatarImg.setVisibility(View.GONE);
                holder.description.setText(String.format(Locale.CHINA, "%d个回答 • %d次关注",
                        result.getDetail().getAgree_count(),
                        result.getDetail().getFocus_count()));
                holder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, QuestionActivity.class);
                        intent.putExtra(Config.INT_QUESTION_ID, result.getSearch_id());
                        intent.putExtra(Config.INT_QUESTION_TITLE, result.getName());
                        mContext.startActivity(intent);
                    }
                });
                break;
            case "articles":
                holder.avatarImg.setVisibility(View.GONE);
                holder.description.setText(String.format(Locale.CHINA, "%d次评论 • %d次浏览",
                        result.getDetail().getComments(),
                        result.getDetail().getViews()));
                holder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, ArticleActivity.class);
                        intent.putExtra(Config.INT_ARTICLE_ID, result.getSearch_id());
                        intent.putExtra(Config.INT_ARTICLE_TITLE, result.getName());
                        mContext.startActivity(intent);
                    }
                });
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_search, viewGroup, false));
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.card)
        CardView cardView;
        @BindView(R.id.avatar_img)
        CircleImageView avatarImg;
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.description)
        TextView description;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}

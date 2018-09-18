package com.langtaosha.sjwyd.controller.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.langtaosha.sjwyd.Config;
import com.langtaosha.sjwyd.R;
import com.langtaosha.sjwyd.controller.activity.ArticleActivity;
import com.langtaosha.sjwyd.controller.activity.QuestionActivity;
import com.langtaosha.sjwyd.controller.activity.UserActivity;
import com.langtaosha.sjwyd.models.ExploreItem;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ExploreViewAdapter extends RecyclerView.Adapter<ExploreViewAdapter.ViewHolder> {

    private List<ExploreItem> mList;
    private Context mContext;

    public ExploreViewAdapter(Context context, List<ExploreItem> list) {
        this.mContext = context;
        this.mList = list;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ExploreItem exploreItem = mList.get(position);
        // Load avatar
        String avatarFile = exploreItem.getUser_info().getAvatar_file();
        if (!avatarFile.isEmpty())
            Picasso.with(mContext)
                    .load(avatarFile)
                    .into(holder.avatarImg);
        // 加载内容
        if (exploreItem.getPost_type().equals(ExploreItem.POST_TYPE_QUESTION)) {
            holder.questionTitle.setText(Html.fromHtml(exploreItem.getQuestion_content()));
            holder.questionInfo.setText(String.valueOf(exploreItem.getFocus_count()) + "人关注 • "
                    + String.valueOf(exploreItem.getAnswer_count()) + "个回答 • "
                    + String.valueOf(exploreItem.getView_count()) + "次浏览");
            holder.questionTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, QuestionActivity.class);
                    intent.putExtra("questionID", exploreItem.getQuestion_id());
                    mContext.startActivity(intent);
                }
            });
            holder.avatarImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, UserActivity.class);
                    intent.putExtra("uid", exploreItem.getUser_info().getUid());
                    mContext.startActivity(intent);
                }
            });
        } else {
            holder.questionTitle.setText(Html.fromHtml(exploreItem.getTitle()));
            holder.questionInfo.setText(String.valueOf(exploreItem.getVotes()) + "人赞同 • "
                    + String.valueOf(exploreItem.getViews()) + "次浏览");
            holder.questionTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ArticleActivity.class);
                    intent.putExtra(Config.INT_ARTICLE_ID, exploreItem.getId());
                    mContext.startActivity(intent);
                }
            });
            holder.avatarImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, UserActivity.class);
                    intent.putExtra("uid", exploreItem.getUser_info().getUid());
                    mContext.startActivity(intent);
                }
            });
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_question, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView avatarImg;
        private TextView questionTitle;
        private TextView questionInfo;

        public ViewHolder(View itemView) {
            super(itemView);
            avatarImg = (CircleImageView) itemView.findViewById(R.id.avatar_img);
            questionTitle = (TextView) itemView.findViewById(R.id.question_title);
            questionInfo = (TextView) itemView.findViewById(R.id.question_info);
        }
    }
}

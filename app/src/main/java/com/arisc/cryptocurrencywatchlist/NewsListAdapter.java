package com.arisc.cryptocurrencywatchlist;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class NewsListAdapter extends RecyclerView.Adapter<NewsListAdapter.NewsListViewHolder> {

    private List<NewsEntry> mData = new ArrayList<>();

    @NonNull
    @Override
    public NewsListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_list_item,parent,false);
        NewsListViewHolder vh = new NewsListViewHolder(itemView,parent.getContext());

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull NewsListViewHolder holder, int position) {
        NewsEntry entry = mData.get(position);
        holder.setValues(entry);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setData(List<NewsEntry> data){
        this.mData = data;
        notifyDataSetChanged();
    }

    public static class NewsListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private final TextView mTtxtTitle;
        private final TextView mTxtAuthor;
        private final TextView mTxtDescription;
        private final TextView mTxtSource;
        private final ImageView mImgNews;

        private String mUrl;

        private Context mContext;

        public NewsListViewHolder(View itemView,Context context) {
            super(itemView);
            mTtxtTitle = itemView.findViewById(R.id.txtNewsTitle);
            mTxtAuthor = itemView.findViewById(R.id.txtNewsAuthor);
            mTxtDescription = itemView.findViewById(R.id.txtNewsDescription);

            mTxtSource = itemView.findViewById(R.id.txtNewsSource);
            mTxtSource.setOnClickListener(this);

            mImgNews = itemView.findViewById(R.id.imgNews);

            mContext = context;

        }

        public void setValues(NewsEntry newsEntry) {
            String title = newsEntry.getTitle();
            mTtxtTitle.setText(title);

            String author = newsEntry.getAuthor();
            mTxtAuthor.setText(author);

            String description = newsEntry.getDescription();
            mTxtDescription.setText(description);

            String source = "Source: " + newsEntry.getSource();
            mTxtSource.setText(source);

            String imageUrl = newsEntry.getImageUrl();
            Uri uri = Uri.parse(imageUrl);
            Picasso.get().load(uri).fit().into(mImgNews);

            mUrl = newsEntry.getUrl();
        }

        @Override
        public void onClick(View v) {
            Uri uri = Uri.parse(mUrl);
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
            mContext.startActivity(intent);
        }
    }
}

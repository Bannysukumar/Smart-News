package com.way2.news.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.way2.news.R;
import com.way2.news.activities.AddNewsActivity;
import com.way2.news.models.News;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminNewsAdapter extends RecyclerView.Adapter<AdminNewsAdapter.AdminNewsViewHolder> {
    private List<News> newsList;
    private Context context;

    public AdminNewsAdapter(List<News> newsList, Context context) {
        this.newsList = newsList;
        this.context = context;
    }

    @NonNull
    @Override
    public AdminNewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_news, parent, false);
        return new AdminNewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminNewsViewHolder holder, int position) {
        News article = newsList.get(position);
        holder.bind(article);
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public class AdminNewsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView ivNewsImage, ivBreaking, ivTrending;
        private TextView tvTitle, tvSummary, tvCategory, tvAuthor, tvPublishedAt, tvStats;
        private View itemView;

        public AdminNewsViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            
            ivNewsImage = itemView.findViewById(R.id.iv_admin_news_image);
            ivBreaking = itemView.findViewById(R.id.iv_admin_breaking);
            ivTrending = itemView.findViewById(R.id.iv_admin_trending);
            tvTitle = itemView.findViewById(R.id.tv_admin_news_title);
            tvSummary = itemView.findViewById(R.id.tv_admin_news_summary);
            tvCategory = itemView.findViewById(R.id.tv_admin_news_category);
            tvAuthor = itemView.findViewById(R.id.tv_admin_news_author);
            tvPublishedAt = itemView.findViewById(R.id.tv_admin_news_published);
            tvStats = itemView.findViewById(R.id.tv_admin_news_stats);
            
            itemView.setOnClickListener(this);
        }

        public void bind(News article) {
            // Set title
            tvTitle.setText(article.getTitle());
            
            // Set summary
            tvSummary.setText(article.getSummary());
            
            // Set category
            tvCategory.setText(article.getCategory());
            
            // Set author
            tvAuthor.setText("By " + article.getAuthor());
            
            // Set published date
            if (article.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                tvPublishedAt.setText(sdf.format(article.getTimestamp()));
            }
            
            // Set stats
            String stats = "👁️ " + article.getViewCount() + " | 👍 0 | 📤 0";
            tvStats.setText(stats);
            
            // Set breaking news indicator
            ivBreaking.setVisibility(article.isBreaking() ? View.VISIBLE : View.GONE);
            
            // Set trending indicator (News model doesn't have trending field)
            ivTrending.setVisibility(View.GONE);
            
            // Load image
            if (article.getImageUrl() != null && !article.getImageUrl().isEmpty()) {
                Glide.with(context)
                    .load(article.getImageUrl())
                    .placeholder(R.drawable.ic_news_placeholder)
                    .error(R.drawable.ic_news_placeholder)
                    .into(ivNewsImage);
            } else {
                ivNewsImage.setImageResource(R.drawable.ic_news_placeholder);
            }
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                News article = newsList.get(position);
                
                // Open edit news activity
                Intent intent = new Intent(context, AddNewsActivity.class);
                intent.putExtra("news_id", article.getId());
                context.startActivity(intent);
            }
        }
    }
}

package com.way2.news.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.way2.news.R;
import com.way2.news.activities.NewsDetailsActivity;
import com.way2.news.models.News;
import com.way2.news.utils.PreferenceManager;
import com.way2.news.utils.LikeAnimationHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {
    private List<News> newsList;
    private Context context;
    private PreferenceManager preferenceManager;
    private OnNewsClickListener onNewsClickListener;

    public interface OnNewsClickListener {
        void onNewsClick(News news);
        void onBookmarkClick(News news);
        void onShareClick(News news);
        void onLikeClick(News news);
        void onDislikeClick(News news);
        void onCommentClick(News news);
        void onDownloadClick(News news);
        void onMoreOptionsClick(News news);
    }

    public NewsAdapter(Context context, List<News> newsList) {
        this.context = context;
        this.newsList = newsList;
        this.preferenceManager = new PreferenceManager(context);
    }

    public void setOnNewsClickListener(OnNewsClickListener listener) {
        this.onNewsClickListener = listener;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        News news = newsList.get(position);
        holder.bind(news);
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public void updateNewsList(List<News> newNewsList) {
        this.newsList = newNewsList;
        notifyDataSetChanged();
    }

    public class NewsViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private ImageView newsImage;
        private TextView newsTitle;
        private TextView newsSummary;
        private TextView newsCategory;
        private TextView newsTime;
        private ImageView bookmarkIcon;
        private ImageView shareIcon;
        private ImageView likeIcon;
        private ImageView dislikeIcon;
        private ImageView commentIcon;
        private ImageView downloadIcon;
        private ImageView moreOptionsIcon;
        private TextView likeCount;
        private TextView dislikeCount;
        private TextView commentCount;
        private View breakingNewsBadge;
        // Author UI
        private ImageView authorAvatar;
        private TextView authorName;
        private android.widget.Button btnFollow;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_news);
            newsImage = itemView.findViewById(R.id.iv_news_image);
            newsTitle = itemView.findViewById(R.id.tv_news_title);
            newsSummary = itemView.findViewById(R.id.tv_news_summary);
            newsCategory = itemView.findViewById(R.id.tv_news_category);
            newsTime = itemView.findViewById(R.id.tv_news_time);
            bookmarkIcon = itemView.findViewById(R.id.iv_bookmark);
            shareIcon = itemView.findViewById(R.id.iv_share);
            likeIcon = itemView.findViewById(R.id.iv_like);
            dislikeIcon = itemView.findViewById(R.id.iv_dislike);
            commentIcon = itemView.findViewById(R.id.iv_comment);
            downloadIcon = itemView.findViewById(R.id.iv_download);
            moreOptionsIcon = itemView.findViewById(R.id.iv_more_options);
            likeCount = itemView.findViewById(R.id.tv_like_count);
            dislikeCount = itemView.findViewById(R.id.tv_dislike_count);
            commentCount = itemView.findViewById(R.id.tv_comment_count);
            breakingNewsBadge = itemView.findViewById(R.id.view_breaking_badge);
            authorAvatar = itemView.findViewById(R.id.iv_author_avatar);
            authorName = itemView.findViewById(R.id.tv_author_name);
            btnFollow = itemView.findViewById(R.id.btn_follow);

            // Set click listeners
            cardView.setOnClickListener(v -> {
                if (onNewsClickListener != null) {
                    onNewsClickListener.onNewsClick(newsList.get(getAdapterPosition()));
                }
            });

            bookmarkIcon.setOnClickListener(v -> {
                if (onNewsClickListener != null) {
                    onNewsClickListener.onBookmarkClick(newsList.get(getAdapterPosition()));
                }
            });

            shareIcon.setOnClickListener(v -> {
                if (onNewsClickListener != null) {
                    onNewsClickListener.onShareClick(newsList.get(getAdapterPosition()));
                }
            });

            likeIcon.setOnClickListener(v -> {
                if (onNewsClickListener != null) {
                    // Add like animation
                    LikeAnimationHelper.animateLike(likeIcon, likeCount, true, context);
                    onNewsClickListener.onLikeClick(newsList.get(getAdapterPosition()));
                }
            });

            dislikeIcon.setOnClickListener(v -> {
                if (onNewsClickListener != null) {
                    // Add dislike animation
                    LikeAnimationHelper.animateDislike(dislikeIcon, dislikeCount, true, context);
                    onNewsClickListener.onDislikeClick(newsList.get(getAdapterPosition()));
                }
            });

            commentIcon.setOnClickListener(v -> {
                if (onNewsClickListener != null) {
                    onNewsClickListener.onCommentClick(newsList.get(getAdapterPosition()));
                }
            });

            downloadIcon.setOnClickListener(v -> {
                if (onNewsClickListener != null) {
                    onNewsClickListener.onDownloadClick(newsList.get(getAdapterPosition()));
                }
            });

            moreOptionsIcon.setOnClickListener(v -> {
                if (onNewsClickListener != null) {
                    onNewsClickListener.onMoreOptionsClick(newsList.get(getAdapterPosition()));
                }
            });
        }

        public void bind(News news) {
            // Set news title
            newsTitle.setText(news.getTitle());

            // Set news summary
            newsSummary.setText(news.getSummary());

            // Set category
            newsCategory.setText(news.getCategory());

            // Set time
            if (news.getTimestamp() != null && newsTime != null) {
                String timeAgo = getTimeAgo(news.getTimestamp());
                newsTime.setText(timeAgo);
            }

            // Load image using Glide
            if (news.getImageUrl() != null && !news.getImageUrl().isEmpty()) {
                Glide.with(context)
                    .load(news.getImageUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_news_placeholder)
                    .error(R.drawable.ic_news_placeholder)
                    .into(newsImage);
            } else {
                newsImage.setImageResource(R.drawable.ic_news_placeholder);
            }

            // Show/hide breaking news badge
            if (news.isBreaking()) {
                breakingNewsBadge.setVisibility(View.VISIBLE);
            } else {
                breakingNewsBadge.setVisibility(View.GONE);
            }

            // Set bookmark icon state
            if (news.isBookmarked()) {
                bookmarkIcon.setImageResource(R.drawable.ic_bookmark_filled);
            } else {
                bookmarkIcon.setImageResource(R.drawable.ic_bookmark_outline);
            }

            // Set interaction counts
            if (likeCount != null) likeCount.setText(String.valueOf(news.getLikeCount()));
            if (dislikeCount != null) dislikeCount.setText(String.valueOf(news.getDislikeCount()));
            if (commentCount != null) commentCount.setText(String.valueOf(news.getCommentCount()));

            // Set like/dislike icon states
            if (likeIcon != null) {
                if (news.isLiked()) {
                    likeIcon.setImageResource(android.R.drawable.arrow_up_float);
                    likeIcon.setColorFilter(context.getResources().getColor(R.color.colorPrimary));
                } else {
                    likeIcon.setImageResource(android.R.drawable.arrow_up_float);
                    likeIcon.setColorFilter(null);
                }
            }

            if (dislikeIcon != null) {
                if (news.isDisliked()) {
                    dislikeIcon.setImageResource(android.R.drawable.arrow_down_float);
                    dislikeIcon.setColorFilter(context.getResources().getColor(R.color.colorAccent));
                } else {
                    dislikeIcon.setImageResource(android.R.drawable.arrow_down_float);
                    dislikeIcon.setColorFilter(null);
                }
            }

            // Bind author info and follow button
            String authorId = news.getAuthorId();

            if (authorId == null || authorId.isEmpty()) {
                authorName.setText("Unknown");
                btnFollow.setVisibility(View.GONE);
            } else {
                // Hide follow for self
                String myUid = preferenceManager.getUserId();
                if (myUid != null && myUid.equals(authorId)) {
                    btnFollow.setVisibility(View.GONE);
                } else {
                    btnFollow.setVisibility(View.VISIBLE);
                }

                // Load basic author profile
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users").document(authorId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        String display = doc.getString("displayName");
                        String username = doc.getString("username");
                        String photo = doc.getString("photoUrl");
                        authorName.setText(username != null && !username.isEmpty() ? username : (display != null ? display : "User"));
                        if (photo != null && !photo.isEmpty()) {
                            try {
                                Glide.with(context).load(photo).placeholder(R.drawable.ic_profile_placeholder).error(R.drawable.ic_profile_placeholder).into(authorAvatar);
                            } catch (Throwable ignored) {}
                        } else {
                            authorAvatar.setImageResource(R.drawable.ic_profile_placeholder);
                        }
                    });

                // Reflect follow state
                final String finalAuthorId = authorId;
                updateFollowButton(finalAuthorId);
                btnFollow.setOnClickListener(v -> {
                    // Optimistic UI toggle
                    CharSequence current = btnFollow.getText();
                    boolean isFollowingNow = current != null && current.toString().equalsIgnoreCase("Following");
                    btnFollow.setText(isFollowingNow ? "Follow" : "Following");
                    toggleFollow(finalAuthorId);
                });

                // Open public profile when tapping avatar or username
                View.OnClickListener openProfile = v -> {
                    try {
                        Intent intent = new Intent(context, com.way2.news.activities.UserProfileActivity.class);
                        intent.putExtra("userId", finalAuthorId);
                        context.startActivity(intent);
                    } catch (Exception ignored) {}
                };
                if (authorAvatar != null) authorAvatar.setOnClickListener(openProfile);
                if (authorName != null) authorName.setOnClickListener(openProfile);
            }
        }

        private void updateFollowButton(String authorId) {
            String myUid = preferenceManager.getUserId();
            if (myUid == null || authorId == null) return;
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users").document(myUid)
                .collection("following").document(authorId)
                .get()
                .addOnSuccessListener(doc -> {
                    boolean isFollowing = doc != null && doc.exists();
                    btnFollow.setText(isFollowing ? "Following" : "Follow");
                });
        }

        private void toggleFollow(String authorId) {
            String myUid = preferenceManager.getUserId();
            if (myUid == null) {
                Toast.makeText(context, "Please login to follow", Toast.LENGTH_SHORT).show();
                return;
            }
            com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
            db.collection("users").document(myUid).collection("following").document(authorId)
                .get()
                .addOnSuccessListener(doc -> {
                    boolean isFollowing = doc != null && doc.exists();
                    com.google.firebase.firestore.WriteBatch batch = db.batch();
                    if (isFollowing) {
                        batch.delete(db.collection("users").document(myUid).collection("following").document(authorId));
                        batch.delete(db.collection("users").document(authorId).collection("followers").document(myUid));
                    } else {
                        java.util.Map<String, Object> followDoc = new java.util.HashMap<>();
                        followDoc.put("followedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
                        // minimal denormalized fields for listing
                        followDoc.put("displayName", preferenceManager.getUserName());
                        followDoc.put("username", preferenceManager.getUserName());
                        batch.set(db.collection("users").document(myUid).collection("following").document(authorId), followDoc, com.google.firebase.firestore.SetOptions.merge());
                        batch.set(db.collection("users").document(authorId).collection("followers").document(myUid), followDoc, com.google.firebase.firestore.SetOptions.merge());
                    }
                    batch.commit().addOnSuccessListener(x -> updateFollowButton(authorId));
                });
        }

        private String getTimeAgo(Date date) {
            long now = System.currentTimeMillis();
            long time = date.getTime();
            long diff = now - time;

            if (diff < 60000) { // Less than 1 minute
                return "Just now";
            } else if (diff < 3600000) { // Less than 1 hour
                return (diff / 60000) + "m ago";
            } else if (diff < 86400000) { // Less than 1 day
                return (diff / 3600000) + "h ago";
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
                return sdf.format(date);
            }
        }
    }
}

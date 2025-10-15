package com.way2.news.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.SnapHelper;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.core.content.ContextCompat;

import com.way2.news.R;
import com.way2.news.activities.NewsDetailsActivity;
import com.way2.news.adapters.NewsAdapter;
import com.way2.news.models.News;
import com.way2.news.utils.PreferenceManager;
import com.way2.news.utils.AdvancedFoldingTransformer;
import com.way2.news.utils.FoldingItemAnimator;
import com.way2.news.utils.NewsItemAnimator;
import com.way2.news.utils.RefreshAnimationHelper;
import com.way2.news.viewmodels.NewsViewModel;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements NewsAdapter.OnNewsClickListener {
    private RecyclerView recyclerViewNews;
    private LinearLayoutManager layoutManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private TextView tvPageIndicator;
    private View actionsBar;
    private ImageView btnLike;
    private ImageView btnDislike;
    private ImageView btnComment;
    private ImageView btnDownload;
    private ImageView btnShare;
    private TextView tvLikeCountOverlay;
    private TextView tvDislikeCountOverlay;
    private TextView tvCommentCountOverlay;
    
    private NewsAdapter newsAdapter;
    private NewsViewModel newsViewModel;
    private PreferenceManager preferenceManager;
    private List<News> newsList;
    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        initializeViews(view);
        setupRecyclerView();
        setupViewModel();
        setupClickListeners();
        
        return view;
    }

    private void initializeViews(View view) {
        recyclerViewNews = view.findViewById(R.id.recycler_view_news);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        progressBar = view.findViewById(R.id.progress_bar);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);
        tvPageIndicator = view.findViewById(R.id.tv_page_indicator);
        actionsBar = view.findViewById(R.id.actions_bar);
        btnLike = view.findViewById(R.id.btn_like);
        btnDislike = view.findViewById(R.id.btn_dislike);
        btnComment = view.findViewById(R.id.btn_comment);
        btnDownload = view.findViewById(R.id.btn_download);
        btnShare = view.findViewById(R.id.btn_share);
        tvLikeCountOverlay = view.findViewById(R.id.tv_like_count_overlay);
        tvDislikeCountOverlay = view.findViewById(R.id.tv_dislike_count_overlay);
        tvCommentCountOverlay = view.findViewById(R.id.tv_comment_count_overlay);
        
        preferenceManager = new PreferenceManager(requireContext());
        newsList = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();
    }

    private void setupRecyclerView() {
        newsAdapter = new NewsAdapter(requireContext(), newsList);
        newsAdapter.setOnNewsClickListener(this);
        
        layoutManager = new LinearLayoutManager(requireContext());
        recyclerViewNews.setLayoutManager(layoutManager);
        recyclerViewNews.setAdapter(newsAdapter);
        recyclerViewNews.setHasFixedSize(true);

        // Add enhanced folding animation effect
        recyclerViewNews.setItemAnimator(new NewsItemAnimator());
        
        // Enable one-article-per-screen vertical paging
        SnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(recyclerViewNews);

        // Update page indicator when user scrolls
        recyclerViewNews.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                applyScrollAnimation();
            }
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int position = layoutManager.findFirstCompletelyVisibleItemPosition();
                    if (position == RecyclerView.NO_POSITION) {
                        position = layoutManager.findFirstVisibleItemPosition();
                    }
                    updatePageIndicator(position, newsAdapter.getItemCount());
                    updateOverlayForPosition(position);
                    
                    // Add folding transition effect when scrolling stops
                    applyFoldingTransition(position);
                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    // Reset folding effect when user starts scrolling
                    resetFoldingEffect();
                }
            }
        });

        // Apply initial animation state
        recyclerViewNews.post(this::applyScrollAnimation);
    }

    private void setupViewModel() {
        newsViewModel = new ViewModelProvider(this).get(NewsViewModel.class);
        
        // Observe news list
        newsViewModel.getNewsList().observe(getViewLifecycleOwner(), news -> {
            if (news != null && !news.isEmpty()) {
                newsList.clear();
                newsList.addAll(news);
                newsAdapter.updateNewsList(newsList);
                tvEmptyState.setVisibility(View.GONE);
                recyclerViewNews.setVisibility(View.VISIBLE);
                tvPageIndicator.setVisibility(View.VISIBLE);
                if (actionsBar != null) actionsBar.setVisibility(View.VISIBLE);
                updatePageIndicator(0, newsList.size());
                updateOverlayForPosition(0);
                
                // Animate news items appearance
                animateNewsItemsAppearance();
            } else {
                tvEmptyState.setVisibility(View.VISIBLE);
                recyclerViewNews.setVisibility(View.GONE);
                tvPageIndicator.setVisibility(View.GONE);
                if (actionsBar != null) actionsBar.setVisibility(View.GONE);
            }
        });
        
        // Observe loading state
        newsViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
                // Stop refresh animation with success effect
                RefreshAnimationHelper.animateRefreshEnd(swipeRefreshLayout, null, null);
                RefreshAnimationHelper.animateRefreshSuccess(recyclerViewNews, requireContext());
            }
        });
        
        // Observe error messages
        newsViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                // Animate error effect
                RefreshAnimationHelper.animateRefreshError(recyclerViewNews, requireContext());
            }
        });
        
        // Load news
        newsViewModel.loadNews();
    }

    private void updatePageIndicator(int position, int total) {
        if (position < 0 || total <= 0) return;
        
        // Get the current news item to show its time
        String timeText = "";
        if (position < newsList.size()) {
            News currentNews = newsList.get(position);
            if (currentNews.getTimestamp() != null) {
                timeText = " / " + getTimeAgo(currentNews.getTimestamp());
            }
        }
        
        String text = "Pages " + (position + 1) + " of " + total + timeText;
        tvPageIndicator.setText(text);
    }

    private void updateOverlayForPosition(int position) {
        if (position < 0 || position >= newsList.size()) return;
        News current = newsList.get(position);
        if (tvLikeCountOverlay != null) tvLikeCountOverlay.setText(String.valueOf(current.getLikeCount()));
        if (tvDislikeCountOverlay != null) tvDislikeCountOverlay.setText(String.valueOf(current.getDislikeCount()));
        if (tvCommentCountOverlay != null) tvCommentCountOverlay.setText(String.valueOf(current.getCommentCount()));

        if (btnLike != null) {
            if (current.isLiked()) {
                btnLike.setColorFilter(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
            } else {
                btnLike.clearColorFilter();
            }
            btnLike.setOnClickListener(v -> onLikeClick(current));
        }
        if (btnDislike != null) {
            if (current.isDisliked()) {
                btnDislike.setColorFilter(ContextCompat.getColor(requireContext(), R.color.colorAccent));
            } else {
                btnDislike.clearColorFilter();
            }
            btnDislike.setOnClickListener(v -> onDislikeClick(current));
        }
        if (btnComment != null) {
            btnComment.setOnClickListener(v -> onCommentClick(current));
        }
        if (btnDownload != null) {
            btnDownload.setOnClickListener(v -> onDownloadClick(current));
        }
        if (btnShare != null) {
            btnShare.setOnClickListener(v -> onShareClick(current));
        }
    }

    private void applyScrollAnimation() {
        if (recyclerViewNews == null || recyclerViewNews.getChildCount() == 0) return;
        int recyclerCenter = recyclerViewNews.getHeight() / 2;
        float maxDistance = recyclerViewNews.getHeight() * 0.6f; // distance where effect ends

        for (int i = 0; i < recyclerViewNews.getChildCount(); i++) {
            View child = recyclerViewNews.getChildAt(i);
            int childCenter = (child.getTop() + child.getBottom()) / 2;
            float distance = Math.abs(recyclerCenter - childCenter);
            float ratio = Math.min(1f, distance / maxDistance);

            // Enhanced folding animation effect
            float scale = 0.85f + (1f - ratio) * 0.15f; // 0.85 to 1.0
            float alpha = 0.7f + (1f - ratio) * 0.3f; // 0.7 to 1.0
            
            // Add folding rotation effect
            float rotationY = ratio * 20f; // rotate up to 20 degrees
            if (childCenter < recyclerCenter) {
                rotationY = -rotationY; // rotate in opposite direction for items above center
            }
            
            // Add perspective and depth
            float translationZ = -ratio * 100f; // move away from screen
            
            child.setScaleX(scale);
            child.setScaleY(scale);
            child.setAlpha(alpha);
            child.setRotationY(rotationY);
            child.setTranslationZ(translationZ);
            
            // Set camera distance for 3D effect
            child.setCameraDistance(child.getWidth() * 4);
        }
    }
    
    private void applyFoldingTransition(int currentPosition) {
        if (recyclerViewNews == null || recyclerViewNews.getChildCount() == 0) return;
        
        for (int i = 0; i < recyclerViewNews.getChildCount(); i++) {
            View child = recyclerViewNews.getChildAt(i);
            int position = recyclerViewNews.getChildAdapterPosition(child);
            
            if (position == currentPosition) {
                // Current page - bring to front with slight scale
                child.animate()
                    .scaleX(1.02f)
                    .scaleY(1.02f)
                    .rotationY(0f)
                    .translationZ(50f)
                    .setDuration(200)
                    .start();
            } else {
                // Other pages - fold away
                float distance = Math.abs(position - currentPosition);
                float foldAmount = Math.min(1f, distance * 0.5f);
                
                child.animate()
                    .scaleX(1f - foldAmount * 0.1f)
                    .scaleY(1f - foldAmount * 0.1f)
                    .rotationY(foldAmount * 15f)
                    .translationZ(-foldAmount * 100f)
                    .alpha(1f - foldAmount * 0.3f)
                    .setDuration(200)
                    .start();
            }
        }
    }
    
    private void resetFoldingEffect() {
        if (recyclerViewNews == null || recyclerViewNews.getChildCount() == 0) return;
        
        for (int i = 0; i < recyclerViewNews.getChildCount(); i++) {
            View child = recyclerViewNews.getChildAt(i);
            child.animate()
                .scaleX(1f)
                .scaleY(1f)
                .rotationY(0f)
                .translationZ(0f)
                .alpha(1f)
                .setDuration(100)
                .start();
        }
    }
    
    private void animateNewsItemsAppearance() {
        if (recyclerViewNews == null || recyclerViewNews.getChildCount() == 0) return;
        
        // Animate each visible news item with staggered delay
        for (int i = 0; i < Math.min(recyclerViewNews.getChildCount(), 3); i++) {
            View child = recyclerViewNews.getChildAt(i);
            if (child != null) {
                RefreshAnimationHelper.animateNewsItemRefresh(child, i * 100);
            }
        }
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

    private void setupClickListeners() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Start refresh animation
            RefreshAnimationHelper.animateRefreshStart(swipeRefreshLayout, null, null);
            newsViewModel.refreshNews();
        });
        
        // Set custom colors for refresh indicator
        swipeRefreshLayout.setColorSchemeColors(
            ContextCompat.getColor(requireContext(), R.color.primary),
            ContextCompat.getColor(requireContext(), R.color.secondary),
            ContextCompat.getColor(requireContext(), R.color.like_color)
        );
        
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(requireContext(), R.color.background_white)
        );
    }

    @Override
    public void onNewsClick(News news) {
        Intent intent = new Intent(requireContext(), NewsDetailsActivity.class);
        intent.putExtra("news_id", news.getId());
        intent.putExtra("news_title", news.getTitle());
        intent.putExtra("news_content", news.getContent());
        intent.putExtra("news_image", news.getImageUrl());
        intent.putExtra("news_category", news.getCategory());
        intent.putExtra("news_source", news.getSourceUrl());
        intent.putExtra("news_author", news.getAuthor());
        startActivity(intent);
    }

    @Override
    public void onBookmarkClick(News news) {
        String uid = preferenceManager.getUserId();
        if (uid == null || uid.isEmpty()) {
            Toast.makeText(requireContext(), "Please login to save news", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean nextState = !news.isBookmarked();
        news.setBookmarked(nextState);

        int position = newsList.indexOf(news);
        if (position != -1) {
            newsAdapter.notifyItemChanged(position);
        }

        if (nextState) {
            java.util.Map<String, Object> saved = new java.util.HashMap<>();
            saved.put("newsId", news.getId());
            saved.put("title", news.getTitle());
            saved.put("summary", news.getSummary());
            saved.put("imageUrl", news.getImageUrl());
            saved.put("category", news.getCategory());
            saved.put("language", news.getLanguage());
            saved.put("sourceUrl", news.getSourceUrl());
            saved.put("author", news.getAuthor());
            saved.put("timestamp", news.getTimestamp());
            saved.put("savedAt", FieldValue.serverTimestamp());

            firestore.collection("users").document(uid)
                .collection("savedNews").document(news.getId())
                .set(saved)
                .addOnSuccessListener(aVoid -> Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Save failed", Toast.LENGTH_SHORT).show());
        } else {
            firestore.collection("users").document(uid)
                .collection("savedNews").document(news.getId())
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(requireContext(), "Removed", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Remove failed", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onShareClick(News news) {
        String uid = preferenceManager.getUserId();
        if (uid == null || uid.isEmpty()) {
            Toast.makeText(requireContext(), "Please login to share", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, news.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT, news.getTitle() + "\n\n" + 
            news.getSummary() + "\n\nRead more: " + news.getSourceUrl());
        
        startActivity(Intent.createChooser(shareIntent, "Share news via"));

        // De-duplicate share per user
        String docId = news.getId() + "_" + uid;
        firestore.collection("newsInteractions").document(docId)
            .get()
            .addOnSuccessListener(existing -> {
                boolean alreadyShared = existing != null && Boolean.TRUE.equals(existing.getBoolean("hasShared"));
                if (!alreadyShared) {
                    java.util.Map<String, Object> updates = new java.util.HashMap<>();
                    updates.put("hasShared", true);
                    updates.put("newsId", news.getId());
                    updates.put("userId", uid);
                    updates.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

                    com.google.firebase.firestore.WriteBatch batch = firestore.batch();
                    batch.set(firestore.collection("newsInteractions").document(docId), updates, com.google.firebase.firestore.SetOptions.merge());
                    batch.update(firestore.collection("news").document(news.getId()), "shareCount", com.google.firebase.firestore.FieldValue.increment(1));
                    batch.commit();
                }
            });
    }

    @Override
    public void onLikeClick(News news) {
        String uid = preferenceManager.getUserId();
        if (uid == null || uid.isEmpty()) {
            Toast.makeText(requireContext(), "Please login to like news", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean wasLiked = news.isLiked();
        boolean wasDisliked = news.isDisliked();
        
        // Toggle like state
        news.setLiked(!wasLiked);
        if (!wasLiked) {
            news.setLikeCount(news.getLikeCount() + 1);
            // If was disliked, remove dislike
            if (wasDisliked) {
                news.setDisliked(false);
                news.setDislikeCount(news.getDislikeCount() - 1);
            }
        } else {
            news.setLikeCount(news.getLikeCount() - 1);
        }

        updateNewsItem(news);
        updateLikeDislikeInFirestore(news, uid);
    }

    @Override
    public void onDislikeClick(News news) {
        String uid = preferenceManager.getUserId();
        if (uid == null || uid.isEmpty()) {
            Toast.makeText(requireContext(), "Please login to dislike news", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean wasLiked = news.isLiked();
        boolean wasDisliked = news.isDisliked();
        
        // Toggle dislike state
        news.setDisliked(!wasDisliked);
        if (!wasDisliked) {
            news.setDislikeCount(news.getDislikeCount() + 1);
            // If was liked, remove like
            if (wasLiked) {
                news.setLiked(false);
                news.setLikeCount(news.getLikeCount() - 1);
            }
        } else {
            news.setDislikeCount(news.getDislikeCount() - 1);
        }

        updateNewsItem(news);
        updateLikeDislikeInFirestore(news, uid);
    }

    @Override
    public void onCommentClick(News news) {
        Intent intent = new Intent(requireContext(), com.way2.news.activities.CommentsActivity.class);
        intent.putExtra("news_id", news.getId());
        startActivity(intent);
    }

    @Override
    public void onDownloadClick(News news) {
        if (news.getImageUrl() == null || news.getImageUrl().isEmpty()) {
            Toast.makeText(requireContext(), "No image to download", Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = preferenceManager.getUserId();
        if (uid == null || uid.isEmpty()) {
            Toast.makeText(requireContext(), "Please login to download", Toast.LENGTH_SHORT).show();
            return;
        }

        android.app.DownloadManager dm = (android.app.DownloadManager) requireContext().getSystemService(android.content.Context.DOWNLOAD_SERVICE);
        android.net.Uri uri = android.net.Uri.parse(news.getImageUrl());
        android.app.DownloadManager.Request req = new android.app.DownloadManager.Request(uri);
        req.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        req.setTitle(news.getTitle());
        req.setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_PICTURES, "way2news_" + news.getId() + ".jpg");
        dm.enqueue(req);
        Toast.makeText(requireContext(), "Downloading image...", Toast.LENGTH_SHORT).show();

        // De-duplicate download per user
        String docId = news.getId() + "_" + uid;
        firestore.collection("newsInteractions").document(docId)
            .get()
            .addOnSuccessListener(existing -> {
                boolean alreadyDownloaded = existing != null && Boolean.TRUE.equals(existing.getBoolean("hasDownloaded"));
                if (!alreadyDownloaded) {
                    java.util.Map<String, Object> updates = new java.util.HashMap<>();
                    updates.put("hasDownloaded", true);
                    updates.put("newsId", news.getId());
                    updates.put("userId", uid);
                    updates.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

                    com.google.firebase.firestore.WriteBatch batch = firestore.batch();
                    batch.set(firestore.collection("newsInteractions").document(docId), updates, com.google.firebase.firestore.SetOptions.merge());
                    batch.update(firestore.collection("news").document(news.getId()), "downloadCount", com.google.firebase.firestore.FieldValue.increment(1));
                    batch.commit();
                }
            });
    }

    @Override
    public void onMoreOptionsClick(News news) {
        // Show more options menu
        Toast.makeText(requireContext(), "More options coming soon!", Toast.LENGTH_SHORT).show();
    }

    public void loadNewsByCategory(String category) {
        if (newsViewModel != null) {
            newsViewModel.loadNewsByCategory(category);
        }
    }

    public void searchNews(String query) {
        if (newsViewModel != null) {
            newsViewModel.searchNews(query);
        }
    }

    public void loadBreakingNews() {
        if (newsViewModel != null) {
            newsViewModel.loadBreakingNews();
        }
    }

    private void updateNewsItem(News news) {
        int position = newsList.indexOf(news);
        if (position == -1 && layoutManager != null) {
            position = layoutManager.findFirstVisibleItemPosition();
        }
        if (position != -1) {
            newsAdapter.notifyItemChanged(position);
            updateOverlayForPosition(position);
        }
    }

    private void updateLikeDislikeInFirestore(News news, String uid) {
        String docId = news.getId() + "_" + uid;
        firestore.collection("newsInteractions").document(docId)
            .get()
            .addOnSuccessListener(existing -> {
                boolean prevLiked = existing != null && Boolean.TRUE.equals(existing.getBoolean("isLiked"));
                boolean prevDisliked = existing != null && Boolean.TRUE.equals(existing.getBoolean("isDisliked"));
                boolean nextLiked = news.isLiked();
                boolean nextDisliked = news.isDisliked();

                long likeDelta = 0;
                long dislikeDelta = 0;
                if (prevLiked != nextLiked) likeDelta = nextLiked ? 1 : -1;
                if (prevDisliked != nextDisliked) dislikeDelta = nextDisliked ? 1 : -1;

                java.util.Map<String, Object> interaction = new java.util.HashMap<>();
                interaction.put("newsId", news.getId());
                interaction.put("userId", uid);
                interaction.put("isLiked", nextLiked);
                interaction.put("isDisliked", nextDisliked);
                interaction.put("timestamp", FieldValue.serverTimestamp());

                com.google.firebase.firestore.WriteBatch batch = firestore.batch();
                batch.set(firestore.collection("newsInteractions").document(docId), interaction, com.google.firebase.firestore.SetOptions.merge());
                if (likeDelta != 0) {
                    batch.update(firestore.collection("news").document(news.getId()), "likeCount", FieldValue.increment(likeDelta));
                }
                if (dislikeDelta != 0) {
                    batch.update(firestore.collection("news").document(news.getId()), "dislikeCount", FieldValue.increment(dislikeDelta));
                }
                batch.commit().addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to update counts", Toast.LENGTH_SHORT).show());
            })
            .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to save interaction", Toast.LENGTH_SHORT).show());
    }
}

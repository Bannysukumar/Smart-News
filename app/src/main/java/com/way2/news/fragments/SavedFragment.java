package com.way2.news.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.way2.news.R;
import com.way2.news.activities.NewsDetailsActivity;
import com.way2.news.adapters.NewsAdapter;
import com.way2.news.models.News;
import com.way2.news.utils.PreferenceManager;
import com.way2.news.viewmodels.NewsViewModel;

import java.util.ArrayList;
import java.util.List;

public class SavedFragment extends Fragment implements NewsAdapter.OnNewsClickListener {
    private RecyclerView recyclerViewSavedNews;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private LinearLayout layoutEmptyState;
    
    private NewsAdapter newsAdapter;
    private NewsViewModel newsViewModel;
    private PreferenceManager preferenceManager;
    private List<News> savedNewsList;
    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved, container, false);
        
        initializeViews(view);
        setupRecyclerView();
        setupViewModel();
        setupClickListeners();
        
        return view;
    }

    private void initializeViews(View view) {
        recyclerViewSavedNews = view.findViewById(R.id.recycler_view_saved_news);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        progressBar = view.findViewById(R.id.progress_bar);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        
        preferenceManager = new PreferenceManager(requireContext());
        savedNewsList = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();
    }

    private void setupRecyclerView() {
        newsAdapter = new NewsAdapter(requireContext(), savedNewsList);
        newsAdapter.setOnNewsClickListener(this);
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerViewSavedNews.setLayoutManager(layoutManager);
        recyclerViewSavedNews.setAdapter(newsAdapter);
    }

    private void setupViewModel() {
        // Directly load per-user saved news from Firestore
        loadSavedNews();
    }

    private void setupClickListeners() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadSavedNews();
        });
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
            Toast.makeText(requireContext(), "Please login", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("savedNews").document(news.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    int position = savedNewsList.indexOf(news);
                    if (position != -1) {
                        savedNewsList.remove(position);
                        newsAdapter.notifyItemRemoved(position);
                        if (savedNewsList.isEmpty()) {
                            layoutEmptyState.setVisibility(View.VISIBLE);
                            recyclerViewSavedNews.setVisibility(View.GONE);
                        }
                    }
                    Toast.makeText(requireContext(), "Removed from saved", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Remove failed", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onShareClick(News news) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, news.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT, news.getTitle() + "\n\n" + 
            news.getSummary() + "\n\nRead more: " + news.getSourceUrl());
        
        startActivity(Intent.createChooser(shareIntent, "Share news via"));
    }

    @Override
    public void onLikeClick(News news) {
        Toast.makeText(requireContext(), "Like feature not available in saved news", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDislikeClick(News news) {
        Toast.makeText(requireContext(), "Dislike feature not available in saved news", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCommentClick(News news) {
        Toast.makeText(requireContext(), "Comments feature coming soon!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDownloadClick(News news) {
        Toast.makeText(requireContext(), "Downloading news for offline reading...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMoreOptionsClick(News news) {
        Toast.makeText(requireContext(), "More options coming soon!", Toast.LENGTH_SHORT).show();
    }

    private void loadSavedNews() {
        String uid = preferenceManager.getUserId();
        if (uid == null || uid.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            recyclerViewSavedNews.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        firestore.collection("users").document(uid)
                .collection("savedNews")
                .orderBy("savedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<News> items = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        News n = new News();
                        n.setId(doc.getString("newsId"));
                        n.setTitle(doc.getString("title"));
                        n.setSummary(doc.getString("summary"));
                        n.setImageUrl(doc.getString("imageUrl"));
                        n.setCategory(doc.getString("category"));
                        n.setLanguage(doc.getString("language"));
                        n.setSourceUrl(doc.getString("sourceUrl"));
                        n.setAuthor(doc.getString("author"));
                        n.setBookmarked(true);
                        items.add(n);
                    }

                    savedNewsList.clear();
                    savedNewsList.addAll(items);
                    newsAdapter.updateNewsList(savedNewsList);

                    boolean empty = savedNewsList.isEmpty();
                    layoutEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
                    recyclerViewSavedNews.setVisibility(empty ? View.GONE : View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(requireContext(), "Failed to load saved", Toast.LENGTH_SHORT).show();
                });
    }
}

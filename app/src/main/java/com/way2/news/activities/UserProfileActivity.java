package com.way2.news.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.way2.news.R;
import com.way2.news.adapters.NewsAdapter;
import com.way2.news.models.News;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ImageView ivAvatar;
    private ImageView ivVerified;
    private TextView tvUsername;
    private TextView tvDisplayName;
    private TextView tvPostsCount;
    private TextView tvFollowersCount;
    private TextView tvFollowingCount;
    private View layoutFollowers;
    private View layoutFollowing;
    private android.widget.Button btnFollow;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    private final List<News> posts = new ArrayList<>();
    private NewsAdapter adapter;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        toolbar = findViewById(R.id.toolbar_user_profile);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        ivAvatar = findViewById(R.id.iv_user_avatar);
        ivVerified = findViewById(R.id.iv_verified_badge);
        tvUsername = findViewById(R.id.tv_user_username);
        tvDisplayName = findViewById(R.id.tv_user_display_name);
        tvPostsCount = findViewById(R.id.tv_count_posts);
        tvFollowersCount = findViewById(R.id.tv_count_followers);
        tvFollowingCount = findViewById(R.id.tv_count_following);
        layoutFollowers = findViewById(R.id.layout_followers);
        layoutFollowing = findViewById(R.id.layout_following);
        btnFollow = findViewById(R.id.btn_follow);
        recyclerView = findViewById(R.id.recycler_user_posts);
        progressBar = findViewById(R.id.progress_bar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NewsAdapter(this, posts);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        userId = getIntent().getStringExtra("userId");

        loadUserInfo();
        loadFollowCounts();
        loadUserPosts();
    }

    private void loadUserInfo() {
        if (userId == null) return;
        db.collection("users").document(userId).get().addOnSuccessListener(doc -> {
            String username = doc.getString("username");
            String displayName = doc.getString("displayName");
            String photoUrl = doc.getString("photoUrl");
            Boolean emailVerified = doc.contains("emailVerified") ? doc.getBoolean("emailVerified") : null;
            if (tvUsername != null) tvUsername.setText(username != null ? "@" + username : "@user");
            if (tvDisplayName != null) tvDisplayName.setText(displayName != null ? displayName : "User");
            if (photoUrl != null && !photoUrl.isEmpty()) {
                try {
                    Glide.with(this).load(photoUrl).placeholder(R.drawable.ic_profile_placeholder).error(R.drawable.ic_profile_placeholder).into(ivAvatar);
                } catch (Throwable ignored) {}
            } else {
                ivAvatar.setImageResource(R.drawable.ic_profile_placeholder);
            }
            if (ivVerified != null) {
                boolean verified = emailVerified != null ? emailVerified : false;
                ivVerified.setImageResource(verified ? R.drawable.ic_verified : R.drawable.ic_unverified);
                ivVerified.setVisibility(View.VISIBLE);
            }
            setupFollowButton();
        });
    }

    private void loadFollowCounts() {
        if (userId == null) return;
        db.collection("users").document(userId).collection("followers").get().addOnSuccessListener(s -> {
            if (tvFollowersCount != null) tvFollowersCount.setText(String.valueOf(s.size()));
        }).addOnFailureListener(e -> {
            if (tvFollowersCount != null) tvFollowersCount.setText("0");
        });
        db.collection("users").document(userId).collection("following").get().addOnSuccessListener(s -> {
            if (tvFollowingCount != null) tvFollowingCount.setText(String.valueOf(s.size()));
        }).addOnFailureListener(e -> {
            if (tvFollowingCount != null) tvFollowingCount.setText("0");
        });
    }

    private void setupFollowButton() {
        if (btnFollow == null) return;
        com.way2.news.utils.PreferenceManager pm = new com.way2.news.utils.PreferenceManager(this);
        String myUid = pm.getUserId();
        if (myUid == null || userId == null) {
            btnFollow.setVisibility(View.GONE);
            return;
        }
        if (myUid.equals(userId)) {
            btnFollow.setVisibility(View.GONE);
            return;
        }
        btnFollow.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance().collection("users").document(myUid)
            .collection("following").document(userId)
            .get().addOnSuccessListener(doc -> {
                boolean isFollowing = doc != null && doc.exists();
                btnFollow.setText(isFollowing ? "Following" : "Follow");
            });
        btnFollow.setOnClickListener(v -> toggleFollow(myUid, userId));
    }

    private void toggleFollow(String myUid, String targetUid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(myUid).collection("following").document(targetUid)
            .get().addOnSuccessListener(doc -> {
                boolean isFollowing = doc != null && doc.exists();
                com.google.firebase.firestore.WriteBatch batch = db.batch();
                if (isFollowing) {
                    batch.delete(db.collection("users").document(myUid).collection("following").document(targetUid));
                    batch.delete(db.collection("users").document(targetUid).collection("followers").document(myUid));
                } else {
                    java.util.Map<String, Object> followDoc = new java.util.HashMap<>();
                    followDoc.put("followedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
                    batch.set(db.collection("users").document(myUid).collection("following").document(targetUid), followDoc, com.google.firebase.firestore.SetOptions.merge());
                    batch.set(db.collection("users").document(targetUid).collection("followers").document(myUid), followDoc, com.google.firebase.firestore.SetOptions.merge());
                }
                batch.commit().addOnSuccessListener(x -> {
                    setupFollowButton();
                    loadFollowCounts();
                });
            });
    }

    private void loadUserPosts() {
        if (userId == null) return;
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        db.collection("news")
            .whereEqualTo("authorId", userId)
            .get()
            .addOnSuccessListener(snap -> {
                posts.clear();
                for (DocumentSnapshot d : snap.getDocuments()) {
                    News n = d.toObject(News.class);
                    if (n != null) { n.setId(d.getId()); }
                    if (n != null) posts.add(n);
                }
                // Sort client-side by timestamp desc to avoid composite index requirement
                try {
                    Collections.sort(posts, new Comparator<News>() {
                        @Override
                        public int compare(News a, News b) {
                            java.util.Date da = a != null ? a.getTimestamp() : null;
                            java.util.Date dbb = b != null ? b.getTimestamp() : null;
                            long ta = da != null ? da.getTime() : 0L;
                            long tb = dbb != null ? dbb.getTime() : 0L;
                            return Long.compare(tb, ta);
                        }
                    });
                } catch (Throwable ignored) {}
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
                if (tvPostsCount != null) tvPostsCount.setText(String.valueOf(posts.size()));
                TextView tvEmpty = findViewById(R.id.tv_empty_posts);
                if (tvEmpty != null) tvEmpty.setVisibility(posts.isEmpty() ? View.VISIBLE : View.GONE);
            })
            .addOnFailureListener(e -> {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
            });
    }
}



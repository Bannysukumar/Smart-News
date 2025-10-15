package com.way2.news.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.way2.news.R;
import com.way2.news.activities.SettingsActivity;
import com.way2.news.utils.AuthUtils;
import com.way2.news.utils.PreferenceManager;

public class ProfileFragment extends Fragment implements View.OnClickListener {
    private ImageView ivProfileImage;
    private TextView tvUserName;
    private TextView tvUserEmail;
    private ImageView ivVerified;
    private View counterPosts;
    private View counterFollowers;
    private View counterFollowing;
    private TextView tvCountPosts;
    private TextView tvCountFollowers;
    private TextView tvCountFollowing;
    private View tvAbout;
    private View ivMenu;
    private View btnEditProfile;
    private View btnShareProfile;
    private androidx.recyclerview.widget.RecyclerView recyclerUserPosts;
    private TextView tvEmptyPosts;
    
    
    private PreferenceManager preferenceManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        initializeViews(view);
        setupClickListeners();
        loadUserData();
        setupPostsList();
        
        return view;
    }

    private void initializeViews(View view) {
        ivProfileImage = view.findViewById(R.id.iv_profile_image);
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserEmail = view.findViewById(R.id.tv_user_email);
        ivVerified = view.findViewById(R.id.iv_verified_badge);
        // Removed settings row from layout; only about remains if present
        tvAbout = view.findViewById(R.id.tv_about);
        ivMenu = view.findViewById(R.id.iv_menu);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnShareProfile = view.findViewById(R.id.btn_share_profile);
        recyclerUserPosts = view.findViewById(R.id.recycler_user_posts);
        tvEmptyPosts = view.findViewById(R.id.tv_empty_posts);
        counterPosts = view.findViewById(R.id.counter_posts);
        counterFollowers = view.findViewById(R.id.counter_followers);
        counterFollowing = view.findViewById(R.id.counter_following);
        tvCountPosts = view.findViewById(R.id.tv_count_posts);
        tvCountFollowers = view.findViewById(R.id.tv_count_followers);
        tvCountFollowing = view.findViewById(R.id.tv_count_following);
        
        
        preferenceManager = new PreferenceManager(requireContext());
    }

    private void setupClickListeners() {
        if (tvAbout != null) tvAbout.setOnClickListener(this);
        if (ivMenu != null) {
            ivMenu.setOnClickListener(v -> showMenu(v));
        }
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> editProfile());
        }
        if (btnShareProfile != null) {
            btnShareProfile.setOnClickListener(v -> shareProfile());
        }
        if (counterFollowers != null) counterFollowers.setOnClickListener(v -> openFollowersFollowing(true));
        if (counterFollowing != null) counterFollowing.setOnClickListener(v -> openFollowersFollowing(false));
    }

    private void loadUserData() {
        if (AuthUtils.isUserLoggedIn()) {
            String userName = AuthUtils.getUserDisplayName();
            String userEmail = AuthUtils.getUserEmail();
            
            tvUserName.setText(userName);
            tvUserEmail.setText(userEmail);

            com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            boolean verified = user != null && user.isEmailVerified();
            if (ivVerified != null) {
                ivVerified.setImageResource(verified ? R.drawable.ic_verified : R.drawable.ic_unverified);
                ivVerified.setVisibility(View.VISIBLE);
            }
        } else {
            tvUserName.setText("Guest User");
            tvUserEmail.setText("Not logged in");
            if (ivVerified != null) ivVerified.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        
        if (id == R.id.tv_about) {
            showAboutDialog();
        
        }
    }

    private void showAboutDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("About Smart News")
               .setMessage("Smart News v1.0\n\nA modern news app with multi-language support.\n\nDeveloped with ❤️")
               .setPositiveButton("OK", null)
               .show();
    }

    private void openWebPage(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url));
        startActivity(intent);
    }

    private void setupPostsList() {
        if (recyclerUserPosts == null) return;
        recyclerUserPosts.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
        loadUserPosts();
    }

    private void loadUserPosts() {
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            if (tvEmptyPosts != null) tvEmptyPosts.setVisibility(View.VISIBLE);
            return;
        }
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("news")
            .whereEqualTo("authorId", uid)
            .get()
            .addOnSuccessListener(snap -> {
                java.util.List<com.google.firebase.firestore.DocumentSnapshot> docs = snap.getDocuments();
                if (tvCountPosts != null) tvCountPosts.setText(String.valueOf(docs.size()));
                if (docs.isEmpty()) {
                    if (tvEmptyPosts != null) tvEmptyPosts.setVisibility(View.VISIBLE);
                } else {
                    if (tvEmptyPosts != null) tvEmptyPosts.setVisibility(View.GONE);
                }
                recyclerUserPosts.setAdapter(new ProfilePostsAdapter(docs));
            });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFollowCounts();
    }

    private void loadFollowCounts() {
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        db.collection("users").document(uid).collection("followers").get()
            .addOnSuccessListener(s -> { if (tvCountFollowers != null) tvCountFollowers.setText(String.valueOf(s.size())); });
        db.collection("users").document(uid).collection("following").get()
            .addOnSuccessListener(s -> { if (tvCountFollowing != null) tvCountFollowing.setText(String.valueOf(s.size())); });
    }

    private void openFollowersFollowing(boolean showFollowers) {
        Intent i = new Intent(requireContext(), com.way2.news.activities.FollowersFollowingActivity.class);
        i.putExtra("mode", showFollowers ? "followers" : "following");
        startActivity(i);
    }

    private static class ProfilePostsAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<ProfilePostVH> {
        private final java.util.List<com.google.firebase.firestore.DocumentSnapshot> posts;
        ProfilePostsAdapter(java.util.List<com.google.firebase.firestore.DocumentSnapshot> posts) { this.posts = posts; }
        @Override public ProfilePostVH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View v = android.view.LayoutInflater.from(parent.getContext())
                .inflate(com.way2.news.R.layout.item_profile_post, parent, false);
            return new ProfilePostVH(v);
        }
        @Override public void onBindViewHolder(ProfilePostVH h, int position) {
            com.google.firebase.firestore.DocumentSnapshot d = posts.get(position);
            String title = d.getString("title");
            String summary = d.getString("summary");
            String imageUrl = d.getString("imageUrl");
            Long likes = getLong(d, "likeCount");
            Long comments = getLong(d, "commentCount");
            Long shares = getLong(d, "shareCount");
            Long downloads = getLong(d, "downloadCount");

            h.title.setText(title != null ? title : "(no title)");
            h.summary.setText(summary != null ? summary : "");
            h.likes.setText((likes != null ? likes : 0) + " Likes");
            h.comments.setText("  •  " + (comments != null ? comments : 0) + " Comments");
            h.shares.setText("  •  " + (shares != null ? shares : 0) + " Shares");
            h.downloads.setText("  •  " + (downloads != null ? downloads : 0) + " Downloads");

            // Load image with Glide if available
            if (imageUrl != null && !imageUrl.isEmpty()) {
                try {
                    com.bumptech.glide.Glide.with(h.itemView.getContext())
                        .load(imageUrl)
                        .placeholder(com.way2.news.R.drawable.ic_profile_placeholder)
                        .error(com.way2.news.R.drawable.ic_profile_placeholder)
                        .into(h.cover);
                } catch (Throwable ignored) {}
            } else {
                h.cover.setImageResource(com.way2.news.R.drawable.ic_profile_placeholder);
            }

            h.delete.setOnClickListener(v -> {
                android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(v.getContext());
                b.setTitle("Delete post")
                    .setMessage("Are you sure you want to delete this news?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        d.getReference().delete().addOnSuccessListener(x -> {
                            int idx = h.getAdapterPosition();
                            if (idx >= 0 && idx < posts.size()) {
                                posts.remove(idx);
                                notifyItemRemoved(idx);
                            }
                            android.widget.Toast.makeText(v.getContext(), "Deleted", android.widget.Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> {
                            android.widget.Toast.makeText(v.getContext(), "Delete failed", android.widget.Toast.LENGTH_SHORT).show();
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            });

            h.comments.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(v.getContext(), com.way2.news.activities.CommentsActivity.class);
                intent.putExtra("news_id", d.getId());
                v.getContext().startActivity(intent);
            });
        }
        @Override public int getItemCount() { return posts.size(); }

        private static Long getLong(com.google.firebase.firestore.DocumentSnapshot d, String key) {
            Object v = d.get(key);
            if (v instanceof Long) return (Long) v;
            if (v instanceof Integer) return ((Integer) v).longValue();
            return null;
        }
    }

    private static class ProfilePostVH extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        android.widget.ImageView cover, delete;
        android.widget.TextView title, summary, likes, comments, shares, downloads;
        ProfilePostVH(android.view.View itemView) {
            super(itemView);
            cover = itemView.findViewById(com.way2.news.R.id.iv_cover);
            delete = itemView.findViewById(com.way2.news.R.id.btn_delete);
            title = itemView.findViewById(com.way2.news.R.id.tv_title);
            summary = itemView.findViewById(com.way2.news.R.id.tv_summary);
            likes = itemView.findViewById(com.way2.news.R.id.tv_likes);
            comments = itemView.findViewById(com.way2.news.R.id.tv_comments);
            shares = itemView.findViewById(com.way2.news.R.id.tv_shares);
            downloads = itemView.findViewById(com.way2.news.R.id.tv_downloads);
        }
    }

    private void logout() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Logout")
               .setMessage("Are you sure you want to logout?")
               .setPositiveButton("Yes", (dialog, which) -> {
                   // Sign out from Firebase
                   AuthUtils.signOut();
                   
                   // Clear user data from preferences
                   AuthUtils.clearUserDataFromPreferences(requireContext());
                   
                   Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                   
                   // Navigate to login activity
                   Intent intent = new Intent(requireContext(), com.way2.news.activities.LoginActivity.class);
                   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                   startActivity(intent);
               })
               .setNegativeButton("No", null)
               .show();
    }

    private void showMenu(View anchor) {
        android.widget.PopupMenu popup = new android.widget.PopupMenu(requireContext(), anchor);
        popup.getMenu().add("Settings").setOnMenuItemClickListener(item -> {
            Intent intent = new Intent(requireContext(), SettingsActivity.class);
            startActivity(intent);
            return true;
        });
        popup.getMenu().add("About").setOnMenuItemClickListener(item -> {
            showAboutDialog();
            return true;
        });
        popup.show();
    }

    private void editProfile() {
        Intent intent = new Intent(requireContext(), com.way2.news.activities.UserEditProfileActivity.class);
        startActivity(intent);
    }

    private void shareProfile() {
        String userName = tvUserName != null ? tvUserName.getText().toString() : "My profile";
        String shareText = "Check out " + userName + " on Smart News";
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(intent, "Share Profile"));
    }
}

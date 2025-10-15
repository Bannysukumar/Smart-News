package com.way2.news.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.way2.news.R;

import java.util.ArrayList;
import java.util.List;

public class FollowersFollowingActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private final List<DocumentSnapshot> users = new ArrayList<>();
    private FirebaseFirestore db;
    private String mode; // "followers" or "following"

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers_following);
        toolbar = findViewById(R.id.toolbar_follow_list);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerView = findViewById(R.id.recycler_follow_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        db = FirebaseFirestore.getInstance();
        mode = getIntent().getStringExtra("mode");
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(mode != null && mode.equals("following") ? "Following" : "Followers");

        loadData();
    }

    private void loadData() {
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        String sub = ("following".equals(mode)) ? "following" : "followers";
        db.collection("users").document(uid).collection(sub)
            .get()
            .addOnSuccessListener(snap -> {
                users.clear();
                users.addAll(snap.getDocuments());
                recyclerView.setAdapter(new Adapter());
            })
            .addOnFailureListener(e -> Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show());
    }

    private class Adapter extends RecyclerView.Adapter<VH> {
        @Override public VH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View v = android.view.LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_follow_user, parent, false);
            return new VH(v);
        }
        @Override public void onBindViewHolder(VH h, int position) {
            DocumentSnapshot d = users.get(position);
            h.name.setText(d.getString("displayName"));
            h.username.setText(d.getString("username"));
            h.btnAction.setText("following".equals(mode) ? "Unfollow" : "Remove");
            h.btnAction.setOnClickListener(v -> {
                String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
                if (uid == null) return;
                String targetUid = d.getId();
                if ("following".equals(mode)) {
                    // Unfollow: remove from my following and from their followers
                    db.collection("users").document(uid).collection("following").document(targetUid).delete();
                    db.collection("users").document(targetUid).collection("followers").document(uid).delete();
                } else {
                    // Remove follower: remove them from my followers and me from their following
                    db.collection("users").document(uid).collection("followers").document(targetUid).delete();
                    db.collection("users").document(targetUid).collection("following").document(uid).delete();
                }
                int idx = h.getAdapterPosition();
                if (idx >= 0 && idx < users.size()) {
                    users.remove(idx);
                    notifyItemRemoved(idx);
                }
            });
        }
        @Override public int getItemCount() { return users.size(); }
    }

    private static class VH extends RecyclerView.ViewHolder {
        android.widget.ImageView avatar;
        android.widget.TextView name, username;
        android.widget.Button btnAction;
        VH(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.iv_avatar);
            name = itemView.findViewById(R.id.tv_name);
            username = itemView.findViewById(R.id.tv_username);
            btnAction = itemView.findViewById(R.id.btn_action);
        }
    }
}



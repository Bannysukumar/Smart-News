package com.way2.news.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.way2.news.R;
import com.way2.news.utils.PreferenceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private EditText editComment;
    private ImageView btnSend;
    private TextView tvEmpty;

    private FirebaseFirestore firestore;
    private PreferenceManager preferenceManager;
    private String newsId;
    private CommentsAdapter adapter;
    private final List<CommentItem> items = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        newsId = getIntent().getStringExtra("news_id");
        firestore = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(this);

        recyclerView = findViewById(R.id.recycler_comments);
        progressBar = findViewById(R.id.progress_bar);
        editComment = findViewById(R.id.edit_comment);
        btnSend = findViewById(R.id.btn_send);
        tvEmpty = findViewById(R.id.tv_empty_state);

        adapter = new CommentsAdapter(items);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnSend.setOnClickListener(v -> postComment());

        listenForComments();
    }

    private void listenForComments() {
        progressBar.setVisibility(View.VISIBLE);
        firestore.collection("news").document(newsId)
                .collection("comments")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, e) -> {
                    progressBar.setVisibility(View.GONE);
                    if (e != null || snap == null) return;
                    for (DocumentChange dc : snap.getDocumentChanges()) {
                        CommentItem item = dc.getDocument().toObject(CommentItem.class);
                        item.id = dc.getDocument().getId();
                        switch (dc.getType()) {
                            case ADDED:
                                items.add(0, item);
                                adapter.notifyItemInserted(0);
                                break;
                            case MODIFIED: {
                                int idx = indexOfId(item.id);
                                if (idx != -1) { items.set(idx, item); adapter.notifyItemChanged(idx); }
                                break; }
                            case REMOVED: {
                                int r = indexOfId(item.id);
                                if (r != -1) { items.remove(r); adapter.notifyItemRemoved(r); }
                                break; }
                        }
                    }
                    tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    private void postComment() {
        String uid = preferenceManager.getUserId();
        String text = editComment.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;
        if (uid == null || uid.isEmpty()) return;

        Map<String, Object> data = new HashMap<>();
        data.put("userId", uid);
        String displayName = preferenceManager.getUserDisplayName();
        if (displayName == null || displayName.isEmpty()) {
            displayName = preferenceManager.getUserName();
        }
        data.put("userName", displayName != null ? displayName : "User");
        data.put("userPhotoUrl", "");
        data.put("text", text);
        data.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        firestore.collection("news").document(newsId)
                .collection("comments")
                .add(data)
                .addOnSuccessListener(ref -> {
                    editComment.setText("");
                    // increment commentCount atomically
                    firestore.collection("news").document(newsId)
                        .update("commentCount", com.google.firebase.firestore.FieldValue.increment(1));
                });
    }

    private void showOwnerActionsDialog(CommentItem c) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setItems(new CharSequence[]{"Edit", "Delete"}, (dialog, which) -> {
            if (which == 0) {
                showEditDialog(c);
            } else if (which == 1) {
                deleteComment(c);
            }
        }).show();
    }

    private void showEditDialog(CommentItem c) {
        final EditText input = new EditText(this);
        input.setText(c.text);
        input.setSelection(input.getText().length());
        new android.app.AlertDialog.Builder(this)
            .setTitle("Edit comment")
            .setView(input)
            .setPositiveButton("Save", (d, w) -> {
                String newText = input.getText().toString().trim();
                if (!newText.isEmpty()) {
                    firestore.collection("news").document(newsId)
                        .collection("comments").document(c.id)
                        .update("text", newText);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteComment(CommentItem c) {
        firestore.collection("news").document(newsId)
            .collection("comments").document(c.id)
            .delete()
            .addOnSuccessListener(v -> firestore.collection("news").document(newsId)
                .update("commentCount", com.google.firebase.firestore.FieldValue.increment(-1)));
    }

    // Simple model
    public static class CommentItem {
        public String id;
        public String userId;
        public String userName;
        public String userPhotoUrl;
        public String text;
        public com.google.firebase.Timestamp createdAt;
        public CommentItem() {}
    }

    private int indexOfId(String id) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).id != null && items.get(i).id.equals(id)) return i;
        }
        return -1;
    }

    // Simple adapter
    public class CommentsAdapter extends RecyclerView.Adapter<CommentsViewHolder> {
        private final List<CommentItem> comments;
        public CommentsAdapter(List<CommentItem> comments) { this.comments = comments; }

        @NonNull @Override public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            android.view.View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_comment, parent, false);
            return new CommentsViewHolder(view);
        }

        @Override public void onBindViewHolder(@NonNull CommentsViewHolder holder, int position) {
            CommentItem c = comments.get(position);
            holder.tvText.setText(c.text);
            holder.tvUser.setText(c.userName != null ? c.userName : "User");
            if (c.createdAt != null) {
                holder.tvTime.setText(getTimeAgo(c.createdAt.toDate()));
            } else {
                holder.tvTime.setText("");
            }
            if (c.userPhotoUrl != null && !c.userPhotoUrl.isEmpty()) {
                com.bumptech.glide.Glide.with(holder.itemView.getContext())
                    .load(c.userPhotoUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(holder.ivAvatar);
            } else {
                holder.ivAvatar.setImageResource(R.drawable.ic_profile_placeholder);
            }

            holder.itemView.setOnLongClickListener(v -> {
                String uid = preferenceManager.getUserId();
                if (uid != null && uid.equals(c.userId)) {
                    showOwnerActionsDialog(c);
                }
                return true;
            });
        }

        @Override public int getItemCount() { return comments.size(); }
    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder {
        public final TextView tvText;
        public final TextView tvUser;
        public final TextView tvTime;
        public final android.widget.ImageView ivAvatar;
        public CommentsViewHolder(@NonNull android.view.View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tv_comment_text);
            tvUser = itemView.findViewById(R.id.tv_username);
            tvTime = itemView.findViewById(R.id.tv_time);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
        }
    }

    private static String getTimeAgo(java.util.Date date) {
        long now = System.currentTimeMillis();
        long diff = now - date.getTime();
        if (diff < 60000) return "Just now";
        if (diff < 3600000) return (diff/60000) + "m";
        if (diff < 86400000) return (diff/3600000) + "h";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault());
        return sdf.format(date);
    }
}



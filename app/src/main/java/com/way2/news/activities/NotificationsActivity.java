package com.way2.news.activities;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.way2.news.R;
import com.way2.news.utils.PreferenceManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private Toolbar toolbar;
    private final List<DocumentSnapshot> notifications = new ArrayList<>();
    private FirebaseFirestore firestore;
    private PreferenceManager pm;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        toolbar = findViewById(R.id.toolbar_notifications);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Notifications");
        if (toolbar != null) toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerView = findViewById(R.id.recycler_notifications);
        progressBar = findViewById(R.id.progress_bar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        firestore = FirebaseFirestore.getInstance();
        pm = new PreferenceManager(this);
        loadNotifications();
    }

    private void loadNotifications() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        String uid = pm.getUserId();
        firestore.collection("notifications")
            .whereEqualTo("userId", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get().addOnSuccessListener(snap -> {
                notifications.clear();
                notifications.addAll(snap.getDocuments());
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                recyclerView.setAdapter(new Adapter());
            }).addOnFailureListener(e -> {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                recyclerView.setAdapter(new Adapter());
            });
    }

    private class Adapter extends RecyclerView.Adapter<VH> {
        @Override public VH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View v = android.view.LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
            return new VH(v);
        }
        @Override public void onBindViewHolder(VH h, int position) {
            DocumentSnapshot doc = notifications.get(position);
            h.title.setText(doc.getString("title"));
            h.message.setText(doc.getString("message"));

            Object ts = doc.get("createdAt");
            long timeMs = System.currentTimeMillis();
            if (ts instanceof Timestamp) {
                Date d = ((Timestamp) ts).toDate();
                timeMs = d.getTime();
            } else if (ts instanceof Date) {
                timeMs = ((Date) ts).getTime();
            }
            CharSequence rel = DateUtils.getRelativeTimeSpanString(timeMs, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
            h.time.setText(rel);

            boolean unread = Boolean.FALSE.equals(doc.getBoolean("read")) || doc.getBoolean("read") == null;
            h.dot.setVisibility(unread ? View.VISIBLE : View.GONE);

                    // Mark as read on tap (optimistic UI update)
                    h.itemView.setOnClickListener(v -> {
                        if (unread) {
                            h.dot.setVisibility(View.GONE);
                            try {
                                doc.getReference().update("read", true);
                            } catch (Exception ignored) {}
                        }
                    });
        }
        @Override public int getItemCount() { return notifications.size(); }
    }

    private static class VH extends RecyclerView.ViewHolder {
        android.widget.ImageView icon;
        android.widget.TextView title, message, time;
        View dot;
        VH(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.iv_icon);
            title = itemView.findViewById(R.id.tv_title);
            message = itemView.findViewById(R.id.tv_message);
            time = itemView.findViewById(R.id.tv_time);
            dot = itemView.findViewById(R.id.dot_unread);
        }
    }
}



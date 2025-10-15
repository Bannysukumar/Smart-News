package com.way2.news.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.way2.news.R;
import com.way2.news.models.News;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PendingSubmissionsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private FirebaseFirestore firestore;
    private final List<DocumentSnapshot> submissions = new ArrayList<>();

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_submissions);
        recyclerView = findViewById(R.id.recycler_pending);
        progressBar = findViewById(R.id.progress_bar);
        firestore = FirebaseFirestore.getInstance();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadPending();
    }

    private void loadPending() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        firestore.collection("newsSubmissions")
            .whereEqualTo("status", "pending")
            .get().addOnSuccessListener(snap -> {
                submissions.clear();
                submissions.addAll(snap.getDocuments());
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                recyclerView.setAdapter(new SimpleAdapter());
            }).addOnFailureListener(e -> {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to load submissions", Toast.LENGTH_SHORT).show();
            });
    }

    private class SimpleAdapter extends RecyclerView.Adapter<ViewHolder> {
        @Override public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View v = android.view.LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pending_submission, parent, false);
            return new ViewHolder(v);
        }
        @Override public void onBindViewHolder(ViewHolder h, int position) {
            DocumentSnapshot doc = submissions.get(position);
            News n = doc.get("news", News.class);
            h.title.setText(n != null ? n.getTitle() : "(no title)");
            h.summary.setText(n != null ? n.getSummary() : "");
            h.itemView.setOnClickListener(v -> showActions(doc));
            h.approve.setOnClickListener(v -> approve(doc));
            h.reject.setOnClickListener(v -> reject(doc));
        }
        @Override public int getItemCount() { return submissions.size(); }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        android.widget.TextView title, summary;
        View approve, reject;
        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_title);
            summary = itemView.findViewById(R.id.tv_summary);
            approve = itemView.findViewById(R.id.btn_approve);
            reject = itemView.findViewById(R.id.btn_reject);
        }
    }

    private void showActions(DocumentSnapshot doc) {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Submission")
            .setMessage("Approve this news?")
            .setPositiveButton("Approve", (d, w) -> approve(doc))
            .setNegativeButton("Reject", (d, w) -> reject(doc))
            .setNeutralButton("Cancel", null)
            .show();
    }

    private void approve(DocumentSnapshot doc) {
        News news = doc.get("news", News.class);
        if (news == null) return;
        Map<String, Object> update = new HashMap<>();
        update.put("status", "approved");
        update.put("approvedAt", new java.util.Date());
        firestore.collection("news").add(news).addOnSuccessListener(r -> {
            // Ensure the published news has authorId set for profile queries
            String authorId = doc.getString("authorId");
            if (authorId != null) {
                r.update("authorId", authorId);
            }

            doc.getReference().update(update);
            // Notify author
            if (authorId != null) {
                Map<String, Object> notif = new HashMap<>();
                notif.put("userId", authorId);
                notif.put("title", "News approved");
                notif.put("message", news.getTitle());
                notif.put("type", "submission");
                notif.put("createdAt", new java.util.Date());
                notif.put("read", false);
                firestore.collection("notifications").add(notif);
            }
            Toast.makeText(this, "Published", Toast.LENGTH_SHORT).show();
            loadPending();
        });
    }

    private void reject(DocumentSnapshot doc) {
        Map<String, Object> update = new HashMap<>();
        update.put("status", "rejected");
        update.put("rejectedAt", new java.util.Date());
        doc.getReference().update(update).addOnSuccessListener(v -> {
            // Notify author
            String authorId = doc.getString("authorId");
            News news = doc.get("news", News.class);
            if (authorId != null) {
                Map<String, Object> notif = new HashMap<>();
                notif.put("userId", authorId);
                notif.put("title", "News rejected");
                notif.put("message", news != null ? news.getTitle() : "Your submission was rejected");
                notif.put("type", "submission");
                notif.put("createdAt", new java.util.Date());
                notif.put("read", false);
                firestore.collection("notifications").add(notif);
            }
            Toast.makeText(this, "Rejected", Toast.LENGTH_SHORT).show();
            loadPending();
        });
    }
}



package com.way2.news.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.way2.news.R;
import com.way2.news.activities.NewsDetailsActivity;
import com.way2.news.utils.NotificationHelper;

public class NewsFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "NewsFCMService";
    private static final String CHANNEL_ID = "way2news_channel";
    private static final String CHANNEL_NAME = "Smart News Notifications";
    private static final String CHANNEL_DESCRIPTION = "Breaking news and daily updates";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            handleDataMessage(remoteMessage);
        }

        // Check if message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            handleNotificationMessage(remoteMessage);
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
        
        // Send token to your server
        sendTokenToServer(token);
    }

    private void handleDataMessage(RemoteMessage remoteMessage) {
        // Handle data-only messages
        String newsId = remoteMessage.getData().get("news_id");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String imageUrl = remoteMessage.getData().get("image_url");
        String category = remoteMessage.getData().get("category");
        String sourceUrl = remoteMessage.getData().get("source_url");
        
        if (title != null && body != null) {
            showNotification(title, body, newsId, imageUrl, category, sourceUrl);
        }
    }

    private void handleNotificationMessage(RemoteMessage remoteMessage) {
        // Handle notification messages
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        if (notification != null) {
            showNotification(
                notification.getTitle(),
                notification.getBody(),
                null,
                null,
                null,
                null
            );
        }
    }

    private void showNotification(String title, String body, String newsId, String imageUrl, String category, String sourceUrl) {
        createNotificationChannel();
        
        Intent intent;
        if (newsId != null) {
            // Open specific news article
            intent = new Intent(this, NewsDetailsActivity.class);
            intent.putExtra("news_id", newsId);
            intent.putExtra("news_title", title);
            intent.putExtra("news_image", imageUrl);
            intent.putExtra("news_category", category);
            intent.putExtra("news_source", sourceUrl);
        } else {
            // Open main activity
            intent = new Intent(this, com.way2.news.MainActivity.class);
        }
        
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent);

        // Add large icon if image URL is provided
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // You can use Glide or Picasso to load the image into a Bitmap
            // For now, we'll skip the large icon
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setShowBadge(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void sendTokenToServer(String token) {
        // Implement your logic to send the token to your server
        // This is where you would typically make an API call to your backend
        Log.d(TAG, "Token sent to server: " + token);
    }
}

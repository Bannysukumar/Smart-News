package com.way2.news.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.way2.news.MainActivity;
import com.way2.news.R;
import com.way2.news.activities.NewsDetailsActivity;
import com.way2.news.models.News;

public class NotificationHelper {
    private static final String CHANNEL_ID = "way2news_channel";
    private static final String CHANNEL_NAME = "Smart News Notifications";
    private static final String CHANNEL_DESCRIPTION = "Breaking news and daily updates";
    private static final int NOTIFICATION_ID = 1001;

    private Context context;
    private NotificationManagerCompat notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
        createNotificationChannel();
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

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public void showBreakingNewsNotification(News news) {
        try {
            Intent intent = new Intent(context, NewsDetailsActivity.class);
            intent.putExtra("news_id", news.getId());
            intent.putExtra("news_title", news.getTitle());
            intent.putExtra("news_content", news.getContent());
            intent.putExtra("news_image", news.getImageUrl());
            intent.putExtra("news_category", news.getCategory());
            intent.putExtra("news_source", news.getSourceUrl());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 
                0, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("🔥 Breaking News")
                .setContentText(news.getTitle())
                .setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(news.getSummary()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

            notificationManager.notify(NOTIFICATION_ID, builder.build());
        } catch (Exception e) {
            Log.e("NotificationHelper", "Error showing breaking news notification", e);
        }
    }

    public void showDailyDigestNotification(String title, String content) {
        try {
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 
                1, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(content))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

            notificationManager.notify(NOTIFICATION_ID + 1, builder.build());
        } catch (Exception e) {
            Log.e("NotificationHelper", "Error showing daily digest notification", e);
        }
    }

    public void showCategoryNotification(String category, String title, String content) {
        try {
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("category", category);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 
                2, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(category + " News")
                .setContentText(title)
                .setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(content))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

            notificationManager.notify(NOTIFICATION_ID + 2, builder.build());
        } catch (Exception e) {
            Log.e("NotificationHelper", "Error showing category notification", e);
        }
    }

    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }

    public void cancelNotification(int notificationId) {
        notificationManager.cancel(notificationId);
    }
}

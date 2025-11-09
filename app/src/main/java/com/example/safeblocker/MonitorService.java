package com.example.safeblocker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.Arrays;
import java.util.List;

public class MonitorService extends Service {

    private static final String TAG = "SafeBlocker";
    private static final String CHANNEL_ID = "SafeBlockerChannel";
    private Handler handler;
    private Runnable task;

    private final List<String> browserPackages = Arrays.asList(
            "com.android.chrome",
            "org.mozilla.firefox",
            "com.opera.browser",
            "com.brave.browser"
    );

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "MonitorService created successfully");
        createNotificationChannel();

        // Build persistent notification
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SafeBlocker is active")
                .setContentText("Monitoring browser activity...")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setOngoing(true)
                .build();

        startForeground(1, notification);

        handler = new Handler();
        task = this::monitorApps;
        handler.post(task);
    }

    private void monitorApps() {
        try {
            UsageStatsManager usm = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
            long end = System.currentTimeMillis();
            long begin = end - 2000;
            UsageEvents events = usm.queryEvents(begin, end);
            UsageEvents.Event event = new UsageEvents.Event();

            String lastApp = null;
            while (events.hasNextEvent()) {
                events.getNextEvent(event);
                if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    lastApp = event.getPackageName();
                }
            }

            if (lastApp != null && browserPackages.contains(lastApp)) {
                Log.d(TAG, "Detected browser: " + lastApp);
                BrowserBlocker.handleDetection(this, lastApp);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }

        handler.postDelayed(task, 2000);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "SafeBlocker Background Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Keeps SafeBlocker active to monitor browsing activity");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(task);
    }
}

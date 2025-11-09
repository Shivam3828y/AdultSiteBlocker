package com.example.safeblocker;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "SafeBlockerMain";
    private Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = findViewById(R.id.startButton);

        startButton.setOnClickListener(v -> {
            if (!hasUsageAccessPermission(this)) {
                Toast.makeText(this, "Grant Usage Access first", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                Log.w(TAG, "Usage access permission not granted. Redirecting user to settings...");
                return;
            }

            if (isServiceRunning(MonitorService.class)) {
                Toast.makeText(this, "SafeBlocker is already running", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "MonitorService already active.");
            } else {
                startMonitoringService();
            }
        });
    }

    /**
     * Starts the foreground monitoring service safely.
     */
    private void startMonitoringService() {
        Intent serviceIntent = new Intent(this, MonitorService.class);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
                Log.d(TAG, "Starting ForegroundService for Android 8+");
            } else {
                startService(serviceIntent);
                Log.d(TAG, "Starting regular Service for legacy Android");
            }

            Toast.makeText(this, "SafeBlocker is now monitoring", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "MonitorService launch requested.");

        } catch (Exception e) {
            Log.e(TAG, "Error while starting service: " + e.getMessage(), e);
            Toast.makeText(this, "Failed to start monitoring: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Verifies if the monitoring service is already running.
     */
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks whether Usage Access Permission is granted.
     */
    private boolean hasUsageAccessPermission(Context context) {
        try {
            AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    context.getPackageName());
            boolean granted = (mode == AppOpsManager.MODE_ALLOWED);
            Log.d(TAG, "Usage access permission granted: " + granted);
            return granted;
        } catch (Exception e) {
            Log.e(TAG, "Failed to check usage access permission", e);
            return false;
        }
    }
}

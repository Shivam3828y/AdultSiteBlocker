package com.example.safeblocker;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BrowserBlocker {

    private static final String[] bannedKeywords = {"porn", "xxx", "adult", "sex"};

    public static void handleDetection(Context context, String packageName) {
        for (String keyword : bannedKeywords) {
            // Placeholder: you can extend this with actual keyword check logic later
            if (packageName.toLowerCase().contains(keyword)) {
                Toast.makeText(context, "Blocked: " + keyword, Toast.LENGTH_SHORT).show();
                Intent i = new Intent(Intent.ACTION_MAIN);
                i.addCategory(Intent.CATEGORY_HOME);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
                break;
            }
        }
    }
}

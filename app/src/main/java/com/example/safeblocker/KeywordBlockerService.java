package com.example.safeblocker;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class KeywordBlockerService extends AccessibilityService {

    private static final String TAG = "SafeBlockerKeyword";
    private static long lastBlockTime = 0;

    // 🚫 Banned words list — moderate for better precision (add more if needed)
    private final List<String> bannedWords = Arrays.asList(
            "porn", "xxx", "sex", "nude", "adult", "boobs", "naked", "erotic",
            "hot video", "xvideo", "playboy","chut","bikini", "masturbation",
            "sexy", "randi", "hentai", "pornhub", "xnxx", "blowjob","chudai",
            "bur","bhosadi","cht","lauda","dick","xnxx","xhamster","viralkand","desidekho",
            "desibhabhi"
    );

    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) return;

        String pkg = event.getPackageName().toString().toLowerCase();

        // ✅ Only monitor major browsers (Chrome, Firefox, Brave, Opera)
        if (!(pkg.contains("chrome") || pkg.contains("firefox")
                || pkg.contains("opera") || pkg.contains("brave"))) {
            return;
        }

        int eventType = event.getEventType();
        if (eventType != AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED &&
                eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            return;
        }

        // ⏱️ Cooldown — prevent over-triggering
        if (System.currentTimeMillis() - lastBlockTime < 2000) return;

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;

        // 🧠 Collect visible text efficiently
        String visibleText = collectVisibleText(root).toLowerCase().trim();
        if (visibleText.isEmpty()) return;

        Log.d(TAG, "🔍 Scanning text: " + visibleText);

        // 🚫 Keyword check using pattern matching
        for (String banned : bannedWords) {
            String pattern = "(?i).*\\b" + Pattern.quote(banned) + "\\b.*";
            if (Pattern.matches(pattern, visibleText)) {
                Log.w(TAG, "🚫 Blocked keyword detected: " + banned);
                showToast("Blocked: " + banned);

                goHome();
                lastBlockTime = System.currentTimeMillis();
                return;
            }
        }

        // 🤖 (Optional future AI filter hook)
        /*
        AITextFilter.init(getApplicationContext());
        if (AITextFilter.isAdultContent(visibleText)) {
            showToast("AI flagged unsafe content");
            goHome();
            lastBlockTime = System.currentTimeMillis();
        }
        */
    }

    /**
     * Recursively extract all visible/editable text nodes.
     */
    private String collectVisibleText(AccessibilityNodeInfo node) {
        if (node == null) return "";
        StringBuilder textBuilder = new StringBuilder();

        CharSequence nodeText = node.getText();
        if (node.isEditable() || (node.getClassName() != null &&
                node.getClassName().toString().toLowerCase().contains("edittext"))) {
            if (nodeText != null) {
                textBuilder.append(nodeText).append(" ");
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            textBuilder.append(collectVisibleText(node.getChild(i))).append(" ");
        }

        return textBuilder.toString();
    }

    /**
     * Softly redirect user to the home screen (browser close simulation).
     */
    private void goHome() {
        try {
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
        } catch (Exception e) {
            Log.e(TAG, "⚠️ Failed to redirect to home: " + e.getMessage());
        }
    }

    /**
     * Safe Toast display from background threads.
     */
    private void showToast(String message) {
        uiHandler.post(() -> {
            try {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Toast error: " + e.getMessage());
            }
        });
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.i(TAG, "✅ KeywordBlockerService connected and active");
        showToast("SafeBlocker is active");
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "⚠️ KeywordBlockerService interrupted");
    }
}

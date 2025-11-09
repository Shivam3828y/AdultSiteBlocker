package com.example.safeblocker;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * ⚙️ OpenAccessibilityActivity
 * ----------------------------
 * A standalone activity that directly opens the Accessibility Settings screen.
 * Safe to use — it does not modify or depend on any other app components.
 */
public class OpenAccessibilityActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            Toast.makeText(this,
                    "Scroll down and enable 'SafeBlocker' in Accessibility settings.",
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this,
                    "Unable to open Accessibility Settings.",
                    Toast.LENGTH_SHORT).show();
        }

        // Close this activity once the intent is launched
        finish();
    }
}

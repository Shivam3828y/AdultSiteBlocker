package com.example.safeblocker;

import android.os.Handler;
import android.widget.TextView;

import java.util.Random;

public class FunnyDialogueManager {
    private final TextView textView;
    private final Handler handler = new Handler();
    private final Random random = new Random();

    private final String[] dialogues = {
            "👮 Inspector Chingum: 'In my town, no badmashi allowed!'",
            "😎 'Control your browser, not your destiny!'",
            "🧘 'Peace begins where temptation ends.'",
            "💥 'Even Motu Patlu agree — sanskaar is the key!'",
            "😂 'Beta, tu sharam kar le thoda!'",
            "🚨 'Bad content alert! Chingum is on duty!'",
            "🎬 'Clean browsing = clean living, my friend!'"
    };

    public FunnyDialogueManager(TextView textView) {
        this.textView = textView;
    }

    public void start() {
        handler.post(dialogueUpdater);
    }

    private final Runnable dialogueUpdater = new Runnable() {
        @Override
        public void run() {
            String msg = dialogues[random.nextInt(dialogues.length)];
            textView.setText(msg);
            handler.postDelayed(this, 5000); // change every 5 seconds
        }
    };
}

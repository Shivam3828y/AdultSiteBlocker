package com.example.safeblocker;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Locale;

public class AITextFilter {

    private static Interpreter interpreter;
    private static final String TAG = "AITextFilter";

    public static void init(Context context) {
        if (interpreter == null) {
            try {
                interpreter = new Interpreter(loadModelFile(context, "nsfw_text_model.tflite"));
                Log.d(TAG, "✅ AITextFilter initialized successfully");
            } catch (Exception e) {
                Log.e(TAG, "❌ Failed to load model: " + e.getMessage());
            }
        }
    }

    private static MappedByteBuffer loadModelFile(Context context, String modelName) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelName);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public static boolean isAdultContent(String text) {
        if (interpreter == null || text == null || text.isEmpty()) return false;

        float[] input = new float[128];
        float[][] output = new float[1][1];

        // Convert characters to normalized floats
        for (int i = 0; i < Math.min(text.length(), 128); i++) {
            input[i] = (float) text.toLowerCase(Locale.ROOT).charAt(i) / 255.0f;
        }

        interpreter.run(input, output);

        float score = output[0][0];
        Log.d(TAG, "AI score: " + score);

        return score > 0.65f; // Threshold for adult content
    }
}

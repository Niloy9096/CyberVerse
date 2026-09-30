package com.cyberverse.app.repository;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CyberKittyKnowledgeProvider {

    private static final String TAG = "CyberKittyRepo";
    private static final String ASSET_FILENAME = "cyberkitty_knowledge.json";
    private static String cachedKnowledge = null;

    public static synchronized String getKnowledge(Context context) {
        if (cachedKnowledge != null) {
            Log.d(TAG, "CyberKitty: Knowledge loaded = true (cached)");
            Log.d(TAG, "CyberKitty: Knowledge characters = " + cachedKnowledge.length());
            Log.d(TAG, "CyberKitty: App context attached = true");
            return cachedKnowledge;
        }

        if (context == null) {
            Log.e(TAG, "CyberKitty ERROR: Context is null when loading knowledge.");
            return "";
        }

        StringBuilder sb = new StringBuilder();
        try (InputStream is = context.getApplicationContext().getAssets().open(ASSET_FILENAME);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            cachedKnowledge = sb.toString().trim();
            Log.d(TAG, "CyberKitty: Knowledge loaded = true");
            Log.d(TAG, "CyberKitty: Knowledge characters = " + cachedKnowledge.length());
            Log.d(TAG, "CyberKitty: App context attached = true");
        } catch (Exception e) {
            Log.e(TAG, "CyberKitty ERROR: Failed to load cyberkitty_knowledge.json from assets: " + e.getMessage());
            cachedKnowledge = "";
        }

        return cachedKnowledge;
    }
}

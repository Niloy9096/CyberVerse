package com.cyberverse.app.repository;

import android.content.Context;
import android.util.Log;

import com.cyberverse.app.network.GroqApiService;
import com.cyberverse.app.network.GroqClient;
import com.cyberverse.app.network.GroqRequest;
import com.cyberverse.app.network.GroqResponse;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CyberKittyRepository {

    private static final String TAG = "CyberKittyRepo";
    private static final String MODEL_ID = "openai/gpt-oss-20b";

    public interface ResponseCallback {
        void onSuccess(String replyText);
        void onError(int statusCode, String friendlyErrorMessage);
    }

    public static void sendChatMessage(String apiKey, String systemPrompt, List<GroqRequest.Message> historyMessages, ResponseCallback callback) {
        sendChatMessage(null, apiKey, systemPrompt, historyMessages, callback);
    }

    public static void sendChatMessage(Context context, String apiKey, String systemPrompt, List<GroqRequest.Message> historyMessages, ResponseCallback callback) {
        boolean isKeyConfigured = (apiKey != null && !apiKey.trim().isEmpty() && !apiKey.trim().equals("gsk_your_groq_api_key_here"));
        Log.d(TAG, "CyberKitty: Starting Groq request");
        Log.d(TAG, "CyberKitty: URL = https://api.groq.com/openai/v1/chat/completions");
        Log.d(TAG, "CyberKitty: Model = " + MODEL_ID);
        Log.d(TAG, "CyberKitty: API key configured = " + isKeyConfigured);

        if (!isKeyConfigured) {
            Log.e(TAG, "CyberKitty ERROR: API key not configured properly.");
            callback.onError(401, "CyberKitty authentication failed. Please check the API configuration.");
            return;
        }

        String knowledge = "";
        if (context != null) {
            knowledge = CyberKittyKnowledgeProvider.getKnowledge(context);
        } else {
            Log.d(TAG, "CyberKitty: App context attached = false");
        }

        String finalSystemPrompt = systemPrompt;
        if (knowledge != null && !knowledge.trim().isEmpty()) {
            finalSystemPrompt = systemPrompt + "\n\nYou have access to verified CyberVerse application knowledge:\n" + knowledge +
                    "\n\nRULES:\n" +
                    "- Use the CyberVerse knowledge when answering CyberVerse-specific questions.\n" +
                    "- Do not invent CyberVerse features, screens, buttons, workflows, or settings.\n" +
                    "- If something is not documented in the knowledge base, clearly say that it is not available in the verified CyberVerse knowledge.\n" +
                    "- General cybersecurity questions can still be answered using general knowledge.";
            Log.d(TAG, "CyberKitty: Final system prompt prepared = true");
        } else {
            Log.d(TAG, "CyberKitty: Final system prompt prepared = false");
        }

        List<GroqRequest.Message> fullMessages = new ArrayList<>();
        if (finalSystemPrompt != null && !finalSystemPrompt.trim().isEmpty()) {
            fullMessages.add(new GroqRequest.Message("system", finalSystemPrompt));
        }

        int maxHistory = 12;
        if (historyMessages != null) {
            int startIndex = Math.max(0, historyMessages.size() - maxHistory);
            for (int i = startIndex; i < historyMessages.size(); i++) {
                fullMessages.add(historyMessages.get(i));
            }
        }

        GroqRequest request = new GroqRequest(MODEL_ID, fullMessages);
        String authHeader = "Bearer " + apiKey.trim();

        GroqApiService api = GroqClient.getApiService();
        api.getChatCompletion(authHeader, request).enqueue(new Callback<GroqResponse>() {
            @Override
            public void onResponse(Call<GroqResponse> call, Response<GroqResponse> response) {
                int code = response.code();
                Log.d(TAG, "CyberKitty: HTTP status = " + code);

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "CyberKitty: Response received");
                    String text = response.body().getFirstChoiceText();
                    if (text != null && !text.trim().isEmpty()) {
                        Log.d(TAG, "CyberKitty: Response received successfully");
                        callback.onSuccess(text.trim());
                        return;
                    }
                }

                ResponseBody errBody = response.errorBody();
                String errStr = "";
                if (errBody != null) {
                    try (errBody) {
                        errStr = errBody.string();
                        Log.e(TAG, "CyberKitty ERROR: HTTP status = " + code);
                        Log.e(TAG, "CyberKitty ERROR: Groq error message = " + errStr);
                    } catch (Exception ignored) {}
                }

                callback.onError(code, getFriendlyError(code));
            }

            @Override
            public void onFailure(Call<GroqResponse> call, Throwable t) {
                String exClass = t.getClass().getName();
                String exMsg = t.getMessage();
                String causeStr = (t.getCause() != null) ? t.getCause().toString() : "null";

                Log.e(TAG, "CyberKitty ERROR: Exception class = " + exClass);
                Log.e(TAG, "CyberKitty ERROR: Exception message = " + exMsg);
                Log.e(TAG, "CyberKitty ERROR: Cause = " + causeStr, t);

                String diagnosticMsg = formatExceptionMessage(t);
                callback.onError(-1, diagnosticMsg);
            }
        });
    }

    private static String formatExceptionMessage(Throwable t) {
        if (t instanceof UnknownHostException) {
            return "CyberKitty ERROR:\nException class = java.net.UnknownHostException\nMessage = Unable to resolve host \"api.groq.com\"";
        } else if (t instanceof ConnectException) {
            return "CyberKitty ERROR:\nException class = java.net.ConnectException\nMessage = " + t.getMessage();
        } else if (t instanceof SocketTimeoutException) {
            return "CyberKitty ERROR:\nException class = java.net.SocketTimeoutException\nMessage = timeout";
        } else if (t instanceof SSLException) {
            return "CyberKitty ERROR:\nException class = javax.net.ssl.SSLException\nMessage = " + t.getMessage();
        } else {
            return "CyberKitty ERROR:\nException class = " + t.getClass().getName() + "\nMessage = " + t.getMessage();
        }
    }

    private static String getFriendlyError(int code) {
        if (code == 404) {
            return "CyberKitty's AI model configuration needs to be updated.";
        } else if (code == 400) {
            return "CyberKitty received an invalid request. Please check the model/request configuration.";
        } else if (code == 401) {
            return "CyberKitty authentication failed. Please check the API configuration.";
        } else if (code == 403) {
            return "CyberKitty access was denied by the API.";
        } else if (code == 429) {
            return "CyberKitty has temporarily reached its usage limit. Please try again later.";
        } else if (code >= 500) {
            return "CyberKitty's AI service is temporarily unavailable. Please try again.";
        } else {
            return "CyberKitty could not reach the server. (" + code + ")";
        }
    }
}

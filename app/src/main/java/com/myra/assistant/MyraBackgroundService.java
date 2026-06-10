package com.myra.assistant;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.telecom.TelecomManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Locale;

public class MyraBackgroundService extends Service {
    private SpeechRecognizer speechRecognizer;
    private TextToSpeech tts;
    private Intent speechIntent;
    private RequestQueue requestQueue;
    
    private final String GEMINI_API_KEY = "AIzaSyDGFeaG6_MqLDQF2lQ1Sh6vjgmFSy3Y8AE"; 

    @Override
    public void onCreate() {
        super.onCreate();
        requestQueue = Volley.newRequestQueue(this);

        NotificationChannel channel = new NotificationChannel("myra_gemini", "MYRA Gemini Live", NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Notification notification = new Notification.Builder(this, "myra_gemini").setContentTitle("MYRA Gemini Engine Running").build();
        startForeground(4, notification);

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("ur", "PK"));
            }
        });

        startContinuousListening();
    }

    private void startContinuousListening() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ur-PK");

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String userWords = matches.get(0).toLowerCase();
                    handleSmartCommandsAndGemini(userWords);
                }
                speechRecognizer.startListening(speechIntent);
            }

            @Override
            public void onError(int error) {
                speechRecognizer.startListening(speechIntent);
            }

            @Override
            public void onReadyForSpeech(Bundle params) {}
            @Override
            public void onBeginningOfSpeech() {}
            @Override
            public void onRmsChanged(float rmsdB) {}
            @Override
            public void onBufferReceived(byte[] buffer) {}
            @Override
            public void onEndOfSpeech() {}
            @Override
            public void onPartialResults(Bundle partialResults) {}
            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        speechRecognizer.startListening(speechIntent);
    }

    private void handleSmartCommandsAndGemini(String words) {
        if (words.contains("attend") || words.contains("کال اٹھاؤ") || words.contains("yes")) {
            try {
                TelecomManager telecomManager = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
                if (telecomManager != null && telecomManager.isRinging()) {
                    telecomManager.acceptRingingCall();
                    tts.speak("کال اٹھا دی ہے اشفاق بھائی۔", TextToSpeech.QUEUE_FLUSH, null, null);
                    return;
                }
            } catch (Exception e) {}
        }

        if (words.contains("whatsapp") || words.contains("واٹس ایپ")) {
            try {
                Intent whatsappIntent = new Intent(Intent.ACTION_VIEW);
                whatsappIntent.setData(Uri.parse("https://api.whatsapp.com/send?text=" + Uri.encode("مائرہ اسسٹنٹ جیمنائی لائیو موڈ پر سیٹ ہو چکی ہے۔")));
                whatsappIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(whatsappIntent);
                tts.speak("واٹس ایپ اوپن کر دیا ہے۔", TextToSpeech.QUEUE_FLUSH, null, null);
                return;
            } catch (Exception e) {}
        }

        if (words.contains("myra") || words.contains("مائرہ") || words.length() > 3) {
            queryGeminiVolley(words);
        }
    }

    private void queryGeminiVolley(String userPrompt) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + GEMINI_API_KEY;

        try {
            JSONObject jsonBody = new JSONObject();
            JSONArray contentsArray = new JSONArray();
            JSONObject partsObj = new JSONObject();
            JSONArray partsArray = new JSONArray();
            JSONObject textObj = new JSONObject();

            textObj.put("text", "You are MYRA, a smart assistant by Ishfaq Bhai. Reply politely in short Urdu. User query: " + userPrompt);
            partsArray.put(textObj);
            partsObj.put("parts", partsArray);
            contentsArray.put(partsObj);
            jsonBody.put("contents", contentsArray);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    try {
                        String aiReply = response.getJSONArray("candidates")
                                                .getJSONObject(0)
                                                .getJSONObject("content")
                                                .getJSONArray("parts")
                                                .getJSONObject(0)
                                                .getString("text");

                        if (tts != null) {
                            tts.speak(aiReply, TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    } catch (Exception e) {
                        if (tts != null) tts.speak("دوبارہ بولیں اشفاق بھائی۔", TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                },
                error -> {
                    if (tts != null) tts.speak("انٹرنیٹ کنکشن چیک کریں۔", TextToSpeech.QUEUE_FLUSH, null, null);
                }
            );

            requestQueue.add(jsonObjectRequest);

        } catch (Exception e) {
            if (tts != null) tts.speak("سسٹم ایرر ہے۔", TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}

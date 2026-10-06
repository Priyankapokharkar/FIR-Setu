package com.example.lawayerapp.views;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lawayerapp.adapters.ChatAdapter;
import com.example.lawayerapp.controllers.ChatController;
import com.example.lawayerapp.controllers.UploadController;
import com.example.lawayerapp.databinding.ActivityChatbotBinding;
import com.example.lawayerapp.models.ChatMessage;
import com.example.lawayerapp.utils.FileUtil;
import com.example.lawayerapp.utils.SharedPrefsUtil;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatbotActivity extends AppCompatActivity implements ChatController.ChatView, UploadController.UploadView {

    private ActivityChatbotBinding binding;
    private ChatController controller;
    private UploadController uploadController;
    private ChatAdapter adapter;
    private final List<ChatMessage> chatList = new ArrayList<>();

    // Launcher for file picking
    private final ActivityResultLauncher<String> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    File file = FileUtil.getFileFromUri(this, uri);
                    if (file != null) {
                        RequestBody requestFile = RequestBody.create(MediaType.parse("application/octet-stream"), file);
                        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
                        uploadController.uploadFile(body);
                    }
                }
            }
    );

    // ActivityResultLauncher for Speech-to-Text
    private final ActivityResultLauncher<Intent> speechResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<String> resultData = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (resultData != null && !resultData.isEmpty()) {
                        String voiceText = resultData.get(0);
                        binding.etMessage.setText(voiceText);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatbotBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String token = SharedPrefsUtil.getToken(this);
        controller = new ChatController(this, token);
        uploadController = new UploadController(this, token);

        setupRecyclerView();
        setupClickListeners();

        // Initial Greeting
        addToChat("Hello! I am your AI Legal Assistant. How can I help you today?", true);
    }

    private void setupRecyclerView() {
        adapter = new ChatAdapter(chatList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.rvChat.setLayoutManager(layoutManager);
        binding.rvChat.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.btnSend.setOnClickListener(v -> {
            String text = binding.etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                addToChat(text, false);
                binding.etMessage.setText(""); 
                controller.sendMessage(text);
            }
        });

        binding.btnMic.setOnClickListener(v -> promptSpeechInput());

        binding.btnUploadDoc.setOnClickListener(v -> {
            // Pick any type of file (images, docs, etc.)
            filePickerLauncher.launch("*/*");
        });
    }

    private void addToChat(String message, boolean isAi) {
        adapter.addMessage(new ChatMessage(message, isAi));
        binding.rvChat.scrollToPosition(chatList.size() - 1);
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something...");
        try {
            speechResultLauncher.launch(intent);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(this, "Speech recognizes not supported on this device.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSend.setEnabled(false);
    }

    @Override
    public void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
        binding.btnSend.setEnabled(true);
    }

    @Override
    public void onAiReply(String text) {
        addToChat(text, true);
    }

    @Override
    public void onUploadSuccess(String filePath) {
        addToChat("System: Evidence uploaded successfully. Document path: " + filePath, true);
        Toast.makeText(this, "File uploaded successfully!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUploadError(String message) {
        addToChat("System Error: " + message, true);
    }

    @Override
    public void onError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

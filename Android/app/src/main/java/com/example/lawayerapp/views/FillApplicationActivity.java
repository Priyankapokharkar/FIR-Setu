package com.example.lawayerapp.views;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lawayerapp.adapters.ActsAdapter;
import com.example.lawayerapp.controllers.FillApplicationController;
import com.example.lawayerapp.databinding.ActivityFillApplicationBinding;
import com.example.lawayerapp.models.Act;
import com.example.lawayerapp.utils.SharedPrefsUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FillApplicationActivity extends AppCompatActivity implements FillApplicationController.FillApplicationView {

    private ActivityFillApplicationBinding binding;
    private FillApplicationController controller;
    private ActsAdapter adapter;
    private List<Act> currentSuggestedActs = new ArrayList<>();

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFillApplicationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String token = SharedPrefsUtil.getToken(this);
        controller = new FillApplicationController(this, token);

        setupRecyclerView();
        setupClickListeners();
        setupSpeechRecognizer();
    }

    private void setupRecyclerView() {
        adapter = new ActsAdapter(currentSuggestedActs);
        binding.rvSuggestedActs.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSuggestedActs.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.btnSuggestActs.setOnClickListener(v -> {
            String description = binding.etDescription.getText().toString();
            // First verify if any info is missing
            controller.verifyApplication(description);
        });

        binding.btnSubmit.setOnClickListener(v -> {
            String description = binding.etDescription.getText().toString();
            controller.submitApplication(description, currentSuggestedActs);
        });

        binding.tilDescription.setEndIconOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
            } else {
                startVoiceRecognition();
            }
        });
    }

    @Override
    public void onValidationResult(boolean isComplete, List<String> missingPoints) {
        if (isComplete) {
            // Proceed to suggest acts
            controller.suggestActs(binding.etDescription.getText().toString());
        } else {
            showMissingInfoDialog(missingPoints);
        }
    }

    private void showMissingInfoDialog(List<String> missingPoints) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("AI Needs More Details");
        
        StringBuilder message = new StringBuilder("To suggest accurate laws, please answer these questions:\n\n");
        for (String q : missingPoints) {
            message.append("• ").append(q).append("\n");
        }
        builder.setMessage(message.toString());

        final EditText input = new EditText(this);
        input.setHint("Type your response here...");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        builder.setView(input);

        builder.setPositiveButton("Add Info", (dialog, which) -> {
            String extraInfo = input.getText().toString();
            if (!extraInfo.isEmpty()) {
                String currentDesc = binding.etDescription.getText().toString();
                String updatedDesc = currentDesc + "\n\nAdditional Details provided: " + extraInfo;
                binding.etDescription.setText(updatedDesc);
                binding.etDescription.setSelection(updatedDesc.length());
                // After adding info, try suggesting acts again
                controller.suggestActs(updatedDesc);
            }
        });

        builder.setNegativeButton("Ignore (Not recommended)", (dialog, which) -> {
            controller.suggestActs(binding.etDescription.getText().toString());
        });

        builder.show();
    }

    private void setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) { Toast.makeText(FillApplicationActivity.this, "Listening...", Toast.LENGTH_SHORT).show(); }
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onError(int error) { Toast.makeText(FillApplicationActivity.this, "Speech error: " + error, Toast.LENGTH_SHORT).show(); }
            @Override public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String existingText = binding.etDescription.getText().toString();
                    String newText = existingText + (existingText.isEmpty() ? "" : " ") + matches.get(0);
                    binding.etDescription.setText(newText);
                    binding.etDescription.setSelection(newText.length());
                }
            }
            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void startVoiceRecognition() {
        speechRecognizer.startListening(speechRecognizerIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startVoiceRecognition();
        } else {
            Toast.makeText(this, "Permission denied. Voice to text is unavailable.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
    }

    @Override
    public void setSuggestedActs(List<Act> acts) {
        this.currentSuggestedActs = acts;
        adapter.updateData(acts);
    }

    @Override
    public void setSubmissionSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void setError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}

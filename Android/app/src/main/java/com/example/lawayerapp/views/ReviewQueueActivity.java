package com.example.lawayerapp.views;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lawayerapp.adapters.ReviewQueueAdapter;
import com.example.lawayerapp.controllers.ReviewQueueController;
import com.example.lawayerapp.databinding.ActivityReviewQueueBinding;
import com.example.lawayerapp.models.ApplicationData;
import com.example.lawayerapp.utils.SharedPrefsUtil;

import java.util.ArrayList;
import java.util.List;

public class ReviewQueueActivity extends AppCompatActivity implements ReviewQueueController.ReviewQueueView, ReviewQueueAdapter.OnReviewAction {

    private ActivityReviewQueueBinding binding;
    private ReviewQueueController controller;
    private ReviewQueueAdapter adapter;
    private List<ApplicationData> applicationsList = new ArrayList<>();
    private EditText activeFirDraftInput; // To update from AI response

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReviewQueueBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String token = SharedPrefsUtil.getToken(this);
        controller = new ReviewQueueController(this, token);

        setupRecyclerView();
        controller.fetchPendingApplications();
    }

    private void setupRecyclerView() {
        adapter = new ReviewQueueAdapter(applicationsList, this);
        binding.rvReviewQueue.setLayoutManager(new LinearLayoutManager(this));
        binding.rvReviewQueue.setAdapter(adapter);
    }

    @Override
    public void onApproveClick(ApplicationData app) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Approve Application");

        // Use a ScrollView to prevent content from being cut off (fixes user reported issue)
        NestedScrollView scrollView = new NestedScrollView(this);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(40, 20, 40, 20);

        android.widget.TextView msgView = new android.widget.TextView(this);
        msgView.setText("Review/Enter official FIR text:");
        msgView.setPadding(0, 0, 0, 20);
        container.addView(msgView);

        activeFirDraftInput = new EditText(this);
        activeFirDraftInput.setHint("Official FIR Narrative...");
        activeFirDraftInput.setMinLines(8);
        activeFirDraftInput.setBackgroundResource(android.R.drawable.editbox_background_normal);
        container.addView(activeFirDraftInput);

        scrollView.addView(container);
        builder.setView(scrollView);

        builder.setPositiveButton("Final Approve", (dialog, which) -> {
            String firText = activeFirDraftInput.getText().toString();
            controller.updateStatus(app.getId(), "Approved", null, firText, app.getSuggestedActs());
        });

        builder.setNeutralButton("Auto-Draft with AI", null); // Override later to prevent closing
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Override neutral button to call AI WITHOUT closing the dialog
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
            controller.draftFir(app.getId());
            Toast.makeText(this, "AI is drafting the FIR...", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDraftReceived(String draftText) {
        if (activeFirDraftInput != null) {
            activeFirDraftInput.setText(draftText);
            Toast.makeText(this, "AI Draft Received!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRejectClick(ApplicationData app) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reject Application");
        builder.setMessage("Enter reason for rejection:");

        final EditText input = new EditText(this);
        input.setHint("Reason...");
        builder.setView(input);

        builder.setPositiveButton("Reject", (dialog, which) -> {
            String reason = input.getText().toString();
            controller.updateStatus(app.getId(), "Rejected", reason, null, null);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
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
    public void setPendingApplications(List<ApplicationData> applications) {
        this.applicationsList = applications;
        adapter.updateData(applications);
        if (applications.isEmpty()) {
            Toast.makeText(this, "Queue is empty.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStatusUpdated(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

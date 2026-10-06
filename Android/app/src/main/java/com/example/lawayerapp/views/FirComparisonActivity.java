package com.example.lawayerapp.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lawayerapp.controllers.FirComparisonController;
import com.example.lawayerapp.databinding.ActivityFirComparisonBinding;
import com.example.lawayerapp.models.ComparisonDetails;
import com.example.lawayerapp.utils.SharedPrefsUtil;
import com.google.android.material.chip.Chip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

public class FirComparisonActivity extends AppCompatActivity implements FirComparisonController.FirComparisonView {

    private ActivityFirComparisonBinding binding;
    private FirComparisonController controller;
    private String applicationId;

    private final ActivityResultLauncher<String> pdfPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    uploadPdf(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFirComparisonBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String token = SharedPrefsUtil.getToken(this);
        controller = new FirComparisonController(this, token);

        Intent intent = getIntent();
        applicationId = intent.getStringExtra("APPLICATION_ID");

        if (applicationId != null && !applicationId.isEmpty()) {
            controller.analyzeFir(applicationId);
        } else {
            Toast.makeText(this, "Application ID is missing.", Toast.LENGTH_SHORT).show();
            finish();
        }

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.btnGeneratePdf.setOnClickListener(v -> {
            controller.generateFirPdf(applicationId);
        });

        binding.btnUploadManualPdf.setOnClickListener(v -> {
            pdfPickerLauncher.launch("application/pdf");
        });
    }

    private void uploadPdf(Uri uri) {
        try {
            // Copy URI content to a temporary file to upload
            File tempFile = new File(getCacheDir(), "manual_fir_upload.pdf");
            try (InputStream is = getContentResolver().openInputStream(uri);
                 FileOutputStream fos = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
            }
            controller.uploadOfflineFir(applicationId, tempFile);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to prepare file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvComparisonReport.setText("Processing Analysis...");
    }

    @Override
    public void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
    }

    @Override
    public void setComparisonReport(ComparisonDetails report) {
        populateReportUI(report);
        // Once report is shown, allow PDF actions
        binding.layoutPdfActions.setVisibility(View.VISIBLE);
    }

    private void populateReportUI(ComparisonDetails report) {
        if (report == null) return;

        // 1. Status
        binding.tvStatus.setText(report.getStatus());
        if ("Verified".equalsIgnoreCase(report.getStatus())) {
            binding.tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            binding.tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        // 2. Table Data (3-Column Layout)
        binding.tvOriginalCategory.setText(report.getOriginalCategory());
        binding.tvFirCategory.setText(report.getFirCategory());
        
        if (report.isCategoryMatch()) {
            binding.tvFirCategory.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            binding.tvFirCategory.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        binding.tvRecommendedSections.setText(report.getRecommendedIpc() + " / " + report.getRecommendedBns());
        binding.tvFirSections.setText(report.getFirIpcBns());

        if (report.isSectionMatch()) {
            binding.tvFirSections.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            binding.tvFirSections.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        // 3. Keywords Chips
        binding.chipGroupKeywords.removeAllViews();
        addChips(report.getKeyConcepts(), true);
        addChips(report.getMissingConcepts(), false);

        // 4. Source Descriptions
        binding.tvOriginalDescription.setText(report.getOriginalDesc());
        binding.tvFirDescription.setText(report.getFirText());

        // 5. Accuracy Score
        int score = report.getAccuracyScore();
        binding.tvAccuracyPercentage.setText(score + "%");
        binding.progressAccuracy.setProgress(score);

        // Color coding for progress based on percentage
        int indicatorColor;
        String desc;
        if (score >= 80) {
            indicatorColor = getResources().getColor(android.R.color.holo_green_dark);
            desc = "Excellent match across legal sections and factual consistency.";
        } else if (score >= 50) {
            indicatorColor = getResources().getColor(android.R.color.holo_orange_dark);
            desc = "Partial match. Some discrepancies detected in sections or keywords.";
        } else {
            indicatorColor = getResources().getColor(android.R.color.holo_red_dark);
            desc = "Critical mismatch. The FIR draft significantly differs from the story.";
        }
        binding.progressAccuracy.setIndicatorColor(indicatorColor);
        binding.tvAccuracyDescription.setText(desc);

        // 6. Summary Text
        binding.tvComparisonReport.setText(report.getSummaryText());
    }

    private void addChips(List<String> list, boolean isFound) {
        if (list == null) return;
        for (String item : list) {
            Chip chip = new Chip(this);
            chip.setText(item);
            if (isFound) {
                chip.setChipIconResource(android.R.drawable.presence_online);
                chip.setChipBackgroundColorResource(android.R.color.white);
            } else {
                chip.setChipIconResource(android.R.drawable.presence_busy);
                chip.setChipBackgroundColorResource(android.R.color.darker_gray);
                chip.setAlpha(0.6f);
            }
            binding.chipGroupKeywords.addView(chip);
        }
    }

    @Override
    public void onPdfActionSuccess(String message, String url, ComparisonDetails report) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        
        // Update the report with the AI analysis of the uploaded PDF
        if (report != null) {
            populateReportUI(report);
        }

        // Use the full URL provided by the backend directly
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    @Override
    public void setError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}

package com.example.lawayerapp.views;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lawayerapp.databinding.ActivityConstableDashboardBinding;
import com.example.lawayerapp.utils.SharedPrefsUtil;

public class ConstableDashboardActivity extends AppCompatActivity {

    private ActivityConstableDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConstableDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.cardPendingApplications.setOnClickListener(v ->
            startActivity(new Intent(this, ReviewQueueActivity.class)));

        binding.cardReviewHistory.setOnClickListener(v ->
            startActivity(new Intent(this, ReviewHistoryActivity.class)));

        binding.cardDirectFir.setOnClickListener(v ->
            startActivity(new Intent(this, DirectFirActivity.class)));

        binding.cardProfile.setOnClickListener(v ->
            startActivity(new Intent(this, ProfileActivity.class)));

        binding.cardAiAssistant.setOnClickListener(v ->
            startActivity(new Intent(this, ChatbotActivity.class)));

        binding.btnLogout.setOnClickListener(v -> {
            SharedPrefsUtil.clearSession(this);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}

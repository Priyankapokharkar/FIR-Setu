package com.example.lawayerapp.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lawayerapp.databinding.ActivityUserDashboardBinding;
import com.example.lawayerapp.utils.SharedPrefsUtil;

public class UserDashboardActivity extends AppCompatActivity {

    private ActivityUserDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.cardFileApplication.setOnClickListener(v -> {
            startActivity(new Intent(this, FillApplicationActivity.class));
        });

        binding.cardStatus.setOnClickListener(v -> {
            startActivity(new Intent(this, StatusActivity.class));
        });

        binding.cardChatbot.setOnClickListener(v -> {
            startActivity(new Intent(this, ChatbotActivity.class));
        });

        binding.cardProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        binding.btnLogout.setOnClickListener(v -> {
            SharedPrefsUtil.clearSession(this);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}

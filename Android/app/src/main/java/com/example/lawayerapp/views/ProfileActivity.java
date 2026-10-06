package com.example.lawayerapp.views;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lawayerapp.controllers.ProfileController;
import com.example.lawayerapp.databinding.ActivityProfileBinding;
import com.example.lawayerapp.models.ProfileResponse;
import com.example.lawayerapp.utils.SharedPrefsUtil;

public class ProfileActivity extends AppCompatActivity implements ProfileController.ProfileView {

    private ActivityProfileBinding binding;
    private ProfileController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String token = SharedPrefsUtil.getToken(this);
        controller = new ProfileController(this, token);

        // Fetch user data
        controller.fetchProfile();

        binding.btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String name = binding.etName.getText().toString().trim();
        String location = binding.etLocation.getText().toString().trim();
        String badgeNumber = binding.etBadgeNumber.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Name is required.", Toast.LENGTH_SHORT).show();
            return;
        }

        controller.updateProfile(name, location, badgeNumber, password);
    }

    @Override
    public void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSaveProfile.setEnabled(false);
    }

    @Override
    public void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
        binding.btnSaveProfile.setEnabled(true);
    }

    @Override
    public void onProfileLoaded(ProfileResponse.UserData user) {
        binding.etName.setText(user.getName());
        binding.etEmail.setText(user.getEmail());
        binding.etLocation.setText(user.getLocation());

        if ("constable".equals(user.getRole())) {
            binding.tilBadgeNumber.setVisibility(View.VISIBLE);
            binding.etBadgeNumber.setText(user.getBadgeNumber());
        }
    }

    @Override
    public void onUpdateSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish(); // Go back to dashboard after update
    }

    @Override
    public void onError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

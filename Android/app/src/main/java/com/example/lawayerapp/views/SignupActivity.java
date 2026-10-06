package com.example.lawayerapp.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lawayerapp.R;
import com.example.lawayerapp.controllers.SignupController;
import com.example.lawayerapp.databinding.ActivitySignupBinding;
import com.example.lawayerapp.utils.SharedPrefsUtil;

public class SignupActivity extends AppCompatActivity implements SignupController.SignupView {

    private ActivitySignupBinding binding;
    private SignupController controller;
    private String selectedRole = "user"; // default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        controller = new SignupController(this);

        // Toggle role and show/hide Badge field
        binding.toggleRole.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnRoleConstable) {
                    selectedRole = "constable";
                    binding.layoutBadge.setVisibility(View.VISIBLE);
                } else {
                    selectedRole = "user";
                    binding.layoutBadge.setVisibility(View.GONE);
                }
            }
        });
        // Select Citizen by default
        binding.toggleRole.check(R.id.btnRoleCitizen);

        binding.btnSignup.setOnClickListener(v -> {
            String name     = binding.etName.getText().toString().trim();
            String email    = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            String badge    = binding.etBadgeNumber.getText().toString().trim();
            controller.attemptSignup(name, email, password, selectedRole, badge);
        });

        binding.tvLogin.setOnClickListener(v -> finish()); // Go back to LoginActivity
    }

    @Override public void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSignup.setEnabled(false);
    }

    @Override public void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
        binding.btnSignup.setEnabled(true);
    }

    @Override public void onSignupSuccess(String token, String role) {
        SharedPrefsUtil.saveSession(this, token, role);
        Intent intent = "constable".equalsIgnoreCase(role)
            ? new Intent(this, ConstableDashboardActivity.class)
            : new Intent(this, UserDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override public void onSignupError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

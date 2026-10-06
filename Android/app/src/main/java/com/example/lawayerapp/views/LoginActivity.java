package com.example.lawayerapp.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lawayerapp.controllers.LoginController;
import com.example.lawayerapp.databinding.ActivityLoginBinding;
import com.example.lawayerapp.utils.SharedPrefsUtil;

public class LoginActivity extends AppCompatActivity implements LoginController.LoginView {

    private ActivityLoginBinding binding;
    private LoginController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup MVC Controller
        controller = new LoginController(this);

        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            controller.attemptLogin(email, password);
        });

        binding.tvSignup.setOnClickListener(v ->
            startActivity(new Intent(this, SignupActivity.class)));
    }

    @Override
    public void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnLogin.setEnabled(false);
    }

    @Override
    public void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
        binding.btnLogin.setEnabled(true);
    }

    @Override
    public void setLoginSuccess(String token, String role) {
        // Save token & role using utility class
        SharedPrefsUtil.saveSession(this, token, role);
        Toast.makeText(this, "Login Successful: " + role, Toast.LENGTH_SHORT).show();
        
        // Navigate based on Role
        if ("constable".equalsIgnoreCase(role)) {
            startActivity(new Intent(this, ConstableDashboardActivity.class));
        } else {
            startActivity(new Intent(this, UserDashboardActivity.class));
        }
        finish();
    }

    @Override
    public void setLoginError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

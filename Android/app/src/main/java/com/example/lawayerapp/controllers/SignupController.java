package com.example.lawayerapp.controllers;

import com.example.lawayerapp.models.LoginResponse;
import com.example.lawayerapp.models.SignupRequest;
import com.example.lawayerapp.network.ApiClient;
import com.example.lawayerapp.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupController {

    private final SignupView view;
    private final ApiService apiService;

    public interface SignupView {
        void showLoading();
        void hideLoading();
        void onSignupSuccess(String token, String role);
        void onSignupError(String message);
    }

    public SignupController(SignupView view) {
        this.view = view;
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }

    public void attemptSignup(String name, String email, String password, String role, String badgeNumber) {
        if (name.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty()) {
            view.onSignupError("All fields are required.");
            return;
        }
        if (password.length() < 6) {
            view.onSignupError("Password must be at least 6 characters.");
            return;
        }
        if ("constable".equals(role) && (badgeNumber == null || badgeNumber.trim().isEmpty())) {
            view.onSignupError("Badge number is required for Constables.");
            return;
        }

        view.showLoading();
        SignupRequest request = new SignupRequest(name, email, password, role, badgeNumber);
        apiService.signupUser(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                view.hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse resp = response.body();
                    if (resp.isSuccess()) {
                        view.onSignupSuccess(resp.getToken(), resp.getRole());
                    } else {
                        view.onSignupError(resp.getMessage() != null ? resp.getMessage() : "Signup failed.");
                    }
                } else {
                    // Parse the error body to get the real server message
                    String errorMsg = "Signup failed. Please try again.";
                    try {
                        if (response.errorBody() != null) {
                            String raw = response.errorBody().string();
                            // Extract "message" field from JSON manually (no extra lib needed)
                            if (raw.contains("\"message\"")) {
                                int start = raw.indexOf("\"message\"") + 11;
                                int end = raw.indexOf("\"", start + 1);
                                errorMsg = raw.substring(start, end);
                            }
                        }
                    } catch (Exception ignored) {}
                    view.onSignupError(errorMsg);
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                view.hideLoading();
                view.onSignupError("Network error: " + t.getMessage());
            }
        });
    }
}

package com.example.lawayerapp.controllers;

import com.example.lawayerapp.models.LoginRequest;
import com.example.lawayerapp.models.LoginResponse;
import com.example.lawayerapp.network.ApiClient;
import com.example.lawayerapp.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginController {

    private final LoginView view;
    private final ApiService apiService;

    // Interface to communicate back to the View (Activity)
    public interface LoginView {
        void showLoading();
        void hideLoading();
        void setLoginSuccess(String token, String role);
        void setLoginError(String message);
    }

    public LoginController(LoginView view) {
        this.view = view;
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }

    public void attemptLogin(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            view.setLoginError("Please enter email and password");
            return;
        }

        view.showLoading();

        LoginRequest request = new LoginRequest(email, password);
        Call<LoginResponse> call = apiService.loginUser(request);
        
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                view.hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    if (loginResponse.isSuccess()) {
                        view.setLoginSuccess(loginResponse.getToken(), loginResponse.getRole());
                    } else {
                        view.setLoginError(loginResponse.getMessage());
                    }
                } else {
                    view.setLoginError("Login failed. Check credentials.");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                view.hideLoading();
                view.setLoginError("Network Error: " + t.getMessage());
            }
        });
    }
}

package com.example.lawayerapp.controllers;

import com.example.lawayerapp.models.ProfileResponse;
import com.example.lawayerapp.models.UpdateProfileRequest;
import com.example.lawayerapp.network.ApiClient;
import com.example.lawayerapp.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileController {

    private final ProfileView view;
    private final ApiService apiService;
    private final String token;

    public interface ProfileView {
        void showLoading();
        void hideLoading();
        void onProfileLoaded(ProfileResponse.UserData user);
        void onUpdateSuccess(String message);
        void onError(String message);
    }

    public ProfileController(ProfileView view, String token) {
        this.view = view;
        this.token = "Bearer " + token;
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }

    public void fetchProfile() {
        view.showLoading();
        apiService.getProfile(token).enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                view.hideLoading();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    view.onProfileLoaded(response.body().getUser());
                } else {
                    view.onError("Failed to load profile.");
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                view.hideLoading();
                view.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void updateProfile(String name, String location, String badgeNumber, String password) {
        view.showLoading();
        UpdateProfileRequest request = new UpdateProfileRequest(name, location, badgeNumber, password);
        apiService.updateProfile(token, request).enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                view.hideLoading();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    view.onUpdateSuccess("Profile updated successfully!");
                } else {
                    view.onError("Failed to update profile.");
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                view.hideLoading();
                view.onError("Network error: " + t.getMessage());
            }
        });
    }
}

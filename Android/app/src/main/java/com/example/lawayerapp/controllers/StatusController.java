package com.example.lawayerapp.controllers;

import com.example.lawayerapp.models.ApplicationData;
import com.example.lawayerapp.models.ApplicationsResponse;
import com.example.lawayerapp.network.ApiClient;
import com.example.lawayerapp.network.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatusController {

    private final StatusView view;
    private final ApiService apiService;
    private final String token;

    public interface StatusView {
        void showLoading();
        void hideLoading();
        void setApplications(List<ApplicationData> applications);
        void setError(String message);
    }

    public StatusController(StatusView view, String token) {
        this.view = view;
        this.token = token;
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }

    public void fetchApplications() {
        view.showLoading();

        Call<ApplicationsResponse> call = apiService.getMyApplications("Bearer " + token);
        call.enqueue(new Callback<ApplicationsResponse>() {
            @Override
            public void onResponse(Call<ApplicationsResponse> call, Response<ApplicationsResponse> response) {
                view.hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    ApplicationsResponse resp = response.body();
                    if (resp.isSuccess() && resp.getApplications() != null) {
                        view.setApplications(resp.getApplications());
                    } else {
                        view.setError("Failed to fetch applications.");
                    }
                } else {
                    view.setError("Error connecting to server.");
                }
            }

            @Override
            public void onFailure(Call<ApplicationsResponse> call, Throwable t) {
                view.hideLoading();
                view.setError("Network Error: " + t.getMessage());
            }
        });
    }
}

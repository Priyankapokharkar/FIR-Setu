package com.example.lawayerapp.controllers;

import com.example.lawayerapp.models.UploadResponse;
import com.example.lawayerapp.network.ApiClient;
import com.example.lawayerapp.network.ApiService;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadController {

    private final UploadView view;
    private final ApiService apiService;
    private final String token;

    public interface UploadView {
        void showLoading();
        void hideLoading();
        void onUploadSuccess(String filePath);
        void onUploadError(String message);
    }

    public UploadController(UploadView view, String token) {
        this.view = view;
        this.token = token;
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }

    public void uploadFile(MultipartBody.Part filePart) {
        view.showLoading();
        apiService.uploadEvidence("Bearer " + token, filePart).enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                view.hideLoading();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    view.onUploadSuccess(response.body().getFileUrl());
                } else {
                    view.onUploadError("Upload failed or server error.");
                }
            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                view.hideLoading();
                view.onUploadError("Network Error: " + t.getMessage());
            }
        });
    }
}

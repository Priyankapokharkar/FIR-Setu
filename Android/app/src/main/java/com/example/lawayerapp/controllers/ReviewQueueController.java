package com.example.lawayerapp.controllers;

import com.example.lawayerapp.models.Act;
import com.example.lawayerapp.models.ApplicationData;
import com.example.lawayerapp.models.ApplicationsResponse;
import com.example.lawayerapp.models.DraftFirResponse;
import com.example.lawayerapp.models.UpdateStatusRequest;
import com.example.lawayerapp.models.UpdateStatusResponse;
import com.example.lawayerapp.network.ApiClient;
import com.example.lawayerapp.network.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewQueueController {

    private final ReviewQueueView view;
    private final ApiService apiService;
    private final String token;

    public interface ReviewQueueView {
        void showLoading();
        void hideLoading();
        void setPendingApplications(List<ApplicationData> applications);
        void onDraftReceived(String draftText);
        void onStatusUpdated(String message);
        void onError(String message);
    }

    public ReviewQueueController(ReviewQueueView view, String token) {
        this.view = view;
        this.token = token;
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }

    public void fetchPendingApplications() {
        view.showLoading();
        apiService.getPendingApplications("Bearer " + token).enqueue(new Callback<ApplicationsResponse>() {
            @Override
            public void onResponse(Call<ApplicationsResponse> call, Response<ApplicationsResponse> response) {
                view.hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    view.setPendingApplications(response.body().getApplications());
                } else {
                    view.onError("Failed to fetch applications");
                }
            }

            @Override
            public void onFailure(Call<ApplicationsResponse> call, Throwable t) {
                view.hideLoading();
                view.onError(t.getMessage());
            }
        });
    }

    public void fetchHistory() {
        view.showLoading();
        apiService.getConstableHistory("Bearer " + token).enqueue(new Callback<ApplicationsResponse>() {
            @Override
            public void onResponse(Call<ApplicationsResponse> call, Response<ApplicationsResponse> response) {
                view.hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    view.setPendingApplications(response.body().getApplications());
                } else {
                    view.onError("Failed to fetch history");
                }
            }

            @Override
            public void onFailure(Call<ApplicationsResponse> call, Throwable t) {
                view.hideLoading();
                view.onError(t.getMessage());
            }
        });
    }

    public void draftFir(String applicationId) {
        view.showLoading();
        apiService.draftFir("Bearer " + token, applicationId).enqueue(new Callback<DraftFirResponse>() {
            @Override
            public void onResponse(Call<DraftFirResponse> call, Response<DraftFirResponse> response) {
                view.hideLoading();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    view.onDraftReceived(response.body().getDraftedText());
                } else {
                    view.onError("Failed to generate AI draft.");
                }
            }

            @Override
            public void onFailure(Call<DraftFirResponse> call, Throwable t) {
                view.hideLoading();
                view.onError("Network error during drafting: " + t.getMessage());
            }
        });
    }

    public void updateStatus(String applicationId, String status, String reason, String firText, List<Act> acts) {
        if (status.equals("Rejected") && (reason == null || reason.trim().isEmpty())) {
            view.onError("A rejection reason is required.");
            return;
        }
        if (status.equals("Approved") && (firText == null || firText.trim().isEmpty())) {
            view.onError("FIR Text is required to approve.");
            return;
        }

        view.showLoading();

        UpdateStatusRequest request = new UpdateStatusRequest(status, reason, firText, acts);
        Call<UpdateStatusResponse> call = apiService.updateApplicationStatus("Bearer " + token, applicationId, request);

        call.enqueue(new Callback<UpdateStatusResponse>() {
            @Override
            public void onResponse(Call<UpdateStatusResponse> call, Response<UpdateStatusResponse> response) {
                view.hideLoading();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    view.onStatusUpdated("Application " + status + " successfully.");
                    fetchPendingApplications(); // Refresh list
                } else {
                    view.onError("Failed to update status.");
                }
            }

            @Override
            public void onFailure(Call<UpdateStatusResponse> call, Throwable t) {
                view.hideLoading();
                view.onError("Network Error: " + t.getMessage());
            }
        });
    }
}
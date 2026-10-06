package com.example.lawayerapp.controllers;

import com.example.lawayerapp.models.Act;
import com.example.lawayerapp.models.DirectFirRequest;
import com.example.lawayerapp.models.DirectFirResponse;
import com.example.lawayerapp.models.SuggestActsRequest;
import com.example.lawayerapp.models.SuggestActsResponse;
import com.example.lawayerapp.models.UserListResponse;
import com.example.lawayerapp.models.UserSummary;
import com.example.lawayerapp.network.ApiClient;
import com.example.lawayerapp.network.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DirectFirController {

    private final DirectFirView view;
    private final ApiService apiService;
    private final String token;

    public interface DirectFirView {
        void showLoading();
        void hideLoading();
        void setSuggestedActs(List<Act> acts);
        void onFirFiled(String message);
        void onCitizensLoaded(List<UserSummary> citizens);
        void onError(String message);
    }

    public DirectFirController(DirectFirView view, String token) {
        this.view = view;
        this.token = token;
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }

    public void fetchCitizens() {
        apiService.getCitizens("Bearer " + token).enqueue(new Callback<UserListResponse>() {
            @Override
            public void onResponse(Call<UserListResponse> call, Response<UserListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    view.onCitizensLoaded(response.body().getCitizens());
                } else {
                    view.onError("Failed to load citizen list.");
                }
            }
            @Override
            public void onFailure(Call<UserListResponse> call, Throwable t) {
                view.onError("Error loading citizens: " + t.getMessage());
            }
        });
    }

    public void suggestActs(String firText) {
        if (firText == null || firText.trim().isEmpty()) {
            view.onError("Please describe the FIR details first.");
            return;
        }
        view.showLoading();
        SuggestActsRequest request = new SuggestActsRequest(firText);
        apiService.suggestActs("Bearer " + token, request).enqueue(new Callback<SuggestActsResponse>() {
            @Override
            public void onResponse(Call<SuggestActsResponse> call, Response<SuggestActsResponse> response) {
                view.hideLoading();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    view.setSuggestedActs(response.body().getSuggestedActs());
                } else {
                    view.onError("AI could not produce suggestions.");
                }
            }
            @Override
            public void onFailure(Call<SuggestActsResponse> call, Throwable t) {
                view.hideLoading();
                view.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void fileDirectFir(String targetUserId, String firText, List<Act> acts) {
        if (targetUserId == null || targetUserId.trim().isEmpty()) {
            view.onError("Please select a target citizen.");
            return;
        }
        if (firText == null || firText.trim().isEmpty()) {
            view.onError("FIR details cannot be empty.");
            return;
        }
        view.showLoading();
        DirectFirRequest request = new DirectFirRequest(targetUserId, firText, acts);
        apiService.fileDirectFir("Bearer " + token, request).enqueue(new Callback<DirectFirResponse>() {
            @Override
            public void onResponse(Call<DirectFirResponse> call, Response<DirectFirResponse> response) {
                view.hideLoading();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    view.onFirFiled("FIR filed successfully and sent to user.");
                } else {
                    view.onError("Failed to file FIR. Try again.");
                }
            }
            @Override
            public void onFailure(Call<DirectFirResponse> call, Throwable t) {
                view.hideLoading();
                view.onError("Network error: " + t.getMessage());
            }
        });
    }
}

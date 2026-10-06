package com.example.lawayerapp.controllers;

import com.example.lawayerapp.models.AIValidationResponse;
import com.example.lawayerapp.models.Act;
import com.example.lawayerapp.models.SubmitApplicationRequest;
import com.example.lawayerapp.models.SubmitApplicationResponse;
import com.example.lawayerapp.models.SuggestActsRequest;
import com.example.lawayerapp.models.SuggestActsResponse;
import com.example.lawayerapp.network.ApiClient;
import com.example.lawayerapp.network.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FillApplicationController {

    private final FillApplicationView view;
    private final ApiService apiService;
    private final String token;

    public interface FillApplicationView {
        void showLoading();
        void hideLoading();
        void onValidationResult(boolean isComplete, List<String> missingPoints);
        void setSuggestedActs(List<Act> acts);
        void setSubmissionSuccess(String message);
        void setError(String message);
    }

    public FillApplicationController(FillApplicationView view, String token) {
        this.view = view;
        this.token = token;
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }

    public void verifyApplication(String description) {
        if (description == null || description.trim().isEmpty()) {
            view.setError("Please describe the incident first.");
            return;
        }

        view.showLoading();
        
        SuggestActsRequest request = new SuggestActsRequest(description);
        Call<AIValidationResponse> call = apiService.verifyApplication("Bearer " + token, request);

        call.enqueue(new Callback<AIValidationResponse>() {
            @Override
            public void onResponse(Call<AIValidationResponse> call, Response<AIValidationResponse> response) {
                view.hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    AIValidationResponse resp = response.body();
                    view.onValidationResult(resp.isComplete(), resp.getMissingPoints());
                } else {
                    view.setError("Error validating application. Proceeding anyway.");
                    view.onValidationResult(true, null); // Allow proceeding on error
                }
            }

            @Override
            public void onFailure(Call<AIValidationResponse> call, Throwable t) {
                view.hideLoading();
                view.setError("Network Error during validation.");
                view.onValidationResult(true, null);
            }
        });
    }

    public void suggestActs(String description) {
        if (description == null || description.trim().isEmpty()) {
            view.setError("Please describe the incident first.");
            return;
        }

        view.showLoading();
        
        SuggestActsRequest request = new SuggestActsRequest(description);
        Call<SuggestActsResponse> call = apiService.suggestActs("Bearer " + token, request);

        call.enqueue(new Callback<SuggestActsResponse>() {
            @Override
            public void onResponse(Call<SuggestActsResponse> call, Response<SuggestActsResponse> response) {
                view.hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    SuggestActsResponse resp = response.body();
                    if (resp.isSuccess() && resp.getSuggestedActs() != null) {
                        view.setSuggestedActs(resp.getSuggestedActs());
                    } else {
                        view.setError("Failed to generate suggestions. Please try again.");
                    }
                } else {
                    view.setError("Error connecting to AI service.");
                }
            }

            @Override
            public void onFailure(Call<SuggestActsResponse> call, Throwable t) {
                view.hideLoading();
                view.setError("Network Error: " + t.getMessage());
            }
        });
    }

    public void submitApplication(String description, List<Act> acts) {
        if (description == null || description.trim().isEmpty()) {
            view.setError("Please describe the incident.");
            return;
        }

        view.showLoading();

        SubmitApplicationRequest request = new SubmitApplicationRequest(description, acts);
        Call<SubmitApplicationResponse> call = apiService.submitApplication("Bearer " + token, request);

        call.enqueue(new Callback<SubmitApplicationResponse>() {
            @Override
            public void onResponse(Call<SubmitApplicationResponse> call, Response<SubmitApplicationResponse> response) {
                view.hideLoading();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    view.setSubmissionSuccess("Application submitted successfully.");
                } else {
                    view.setError("Submission failed. Try again.");
                }
            }

            @Override
            public void onFailure(Call<SubmitApplicationResponse> call, Throwable t) {
                view.hideLoading();
                view.setError("Network Error: " + t.getMessage());
            }
        });
    }
}

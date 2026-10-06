package com.example.lawayerapp.controllers;

import com.example.lawayerapp.models.CompareFirResponse;
import com.example.lawayerapp.models.ComparisonDetails;
import com.example.lawayerapp.models.UploadResponse;
import com.example.lawayerapp.network.ApiClient;
import com.example.lawayerapp.network.ApiService;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FirComparisonController {

    private final FirComparisonView view;
    private final ApiService apiService;
    private final String token;

    public interface FirComparisonView {
        void showLoading();
        void hideLoading();
        void setComparisonReport(ComparisonDetails report);
        void onPdfActionSuccess(String message, String url, ComparisonDetails report);
        void setError(String message);
    }

    public FirComparisonController(FirComparisonView view, String token) {
        this.view = view;
        this.token = token;
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }

    public void analyzeFir(String applicationId) {
        if (applicationId == null || applicationId.isEmpty()) {
            view.setError("Application ID is missing.");
            return;
        }

        view.showLoading();

        Call<CompareFirResponse> call = apiService.compareFir("Bearer " + token, applicationId);
        call.enqueue(new Callback<CompareFirResponse>() {
            @Override
            public void onResponse(Call<CompareFirResponse> call, Response<CompareFirResponse> response) {
                view.hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    CompareFirResponse resp = response.body();
                    if (resp.isSuccess() && resp.getReport() != null) {
                        view.setComparisonReport(resp.getReport());
                    } else {
                        view.setError("Failed to fetch analysis report.");
                    }
                } else {
                    view.setError("FIR not generated yet or network error.");
                }
            }

            @Override
            public void onFailure(Call<CompareFirResponse> call, Throwable t) {
                view.hideLoading();
                view.setError("Network Error: " + t.getMessage());
            }
        });
    }

    public void generateFirPdf(String applicationId) {
        view.showLoading();
        apiService.generateFirPdf("Bearer " + token, applicationId).enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                view.hideLoading();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    view.onPdfActionSuccess("FIR PDF generated successfully!", response.body().getFileUrl(), response.body().getReport());
                } else {
                    view.setError("Failed to generate PDF. Try again.");
                }
            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                view.hideLoading();
                view.setError("Network error: " + t.getMessage());
            }
        });
    }

    public void uploadOfflineFir(String applicationId, File pdfFile) {
        view.showLoading();
        RequestBody requestFile = RequestBody.create(MediaType.parse("application/pdf"), pdfFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("fir", pdfFile.getName(), requestFile);

        apiService.uploadOfflineFir("Bearer " + token, applicationId, body).enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                view.hideLoading();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    view.onPdfActionSuccess("Manual FIR uploaded and Approved!", response.body().getFileUrl(), response.body().getReport());
                } else {
                    String errorMsg = "Failed to upload PDF. Check if the file is correct.";
                    try {
                        if (response.errorBody() != null) {
                            String errorStr = response.errorBody().string();
                            org.json.JSONObject jsonObj = new org.json.JSONObject(errorStr);
                            if (jsonObj.has("message")) {
                                errorMsg = jsonObj.getString("message");
                            }
                        }
                    } catch (Exception e) {
                        // Fallback to default message
                    }
                    view.setError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                view.hideLoading();
                view.setError("Network error during upload: " + t.getMessage());
            }
        });
    }
}

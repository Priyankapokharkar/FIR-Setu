package com.example.lawayerapp.network;

import com.example.lawayerapp.models.ApplicationsResponse;
import com.example.lawayerapp.models.ChatRequest;
import com.example.lawayerapp.models.ChatResponse;
import com.example.lawayerapp.models.CompareFirResponse;
import com.example.lawayerapp.models.DirectFirRequest;
import com.example.lawayerapp.models.DirectFirResponse;
import com.example.lawayerapp.models.LoginRequest;
import com.example.lawayerapp.models.LoginResponse;
import com.example.lawayerapp.models.SignupRequest;
import com.example.lawayerapp.models.SubmitApplicationRequest;
import com.example.lawayerapp.models.SubmitApplicationResponse;
import com.example.lawayerapp.models.SuggestActsRequest;
import com.example.lawayerapp.models.SuggestActsResponse;
import com.example.lawayerapp.models.ProfileResponse;
import com.example.lawayerapp.models.UpdateProfileRequest;
import com.example.lawayerapp.models.UpdateStatusRequest;
import com.example.lawayerapp.models.UpdateStatusResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface ApiService {

    @POST("api/auth/login")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);

    @GET("api/auth/profile")
    Call<ProfileResponse> getProfile(@Header("Authorization") String token);

    @PUT("api/auth/profile")
    Call<ProfileResponse> updateProfile(
        @Header("Authorization") String token,
        @Body UpdateProfileRequest request
    );
    
    @GET("api/constable/history")
    Call<ApplicationsResponse> getConstableHistory(@Header("Authorization") String token);

    @POST("api/ai/suggest-acts")
    Call<SuggestActsResponse> suggestActs(
        @retrofit2.http.Header("Authorization") String token, 
        @Body SuggestActsRequest request
    );

    @POST("api/user/application")
    Call<SubmitApplicationResponse> submitApplication(
        @retrofit2.http.Header("Authorization") String token, 
        @Body SubmitApplicationRequest request
    );

    @retrofit2.http.GET("api/user/applications")
    Call<ApplicationsResponse> getMyApplications(
        @retrofit2.http.Header("Authorization") String token
    );

    @retrofit2.http.GET("api/ai/compare-fir/{applicationId}")
    Call<CompareFirResponse> compareFir(
        @retrofit2.http.Header("Authorization") String token,
        @retrofit2.http.Path("applicationId") String applicationId
    );

    @POST("api/ai/chat")
    Call<ChatResponse> chatWithAI(
        @retrofit2.http.Header("Authorization") String token,
        @Body ChatRequest request
    );

    @retrofit2.http.GET("api/constable/applications")
    Call<ApplicationsResponse> getPendingApplications(
        @retrofit2.http.Header("Authorization") String token
    );

    @retrofit2.http.PUT("api/constable/application/{id}/status")
    Call<UpdateStatusResponse> updateApplicationStatus(
        @retrofit2.http.Header("Authorization") String token,
        @retrofit2.http.Path("id") String applicationId,
        @Body UpdateStatusRequest request
    );

    @POST("api/constable/direct-fir")
    Call<DirectFirResponse> fileDirectFir(
        @retrofit2.http.Header("Authorization") String token,
        @Body DirectFirRequest request
    );

    @retrofit2.http.POST("api/auth/signup")
    Call<LoginResponse> signupUser(@Body SignupRequest signupRequest);

    @retrofit2.http.GET("api/user/list")
    Call<com.example.lawayerapp.models.UserListResponse> getCitizens(
        @retrofit2.http.Header("Authorization") String token
    );

    @retrofit2.http.Multipart
    @retrofit2.http.POST("api/user/upload-evidence")
    Call<com.example.lawayerapp.models.UploadResponse> uploadEvidence(
        @retrofit2.http.Header("Authorization") String token,
        @retrofit2.http.Part okhttp3.MultipartBody.Part file
    );

    @retrofit2.http.POST("api/user/generate-fir-pdf/{id}")
    Call<com.example.lawayerapp.models.UploadResponse> generateFirPdf(
        @retrofit2.http.Header("Authorization") String token,
        @retrofit2.http.Path("id") String applicationId
    );

    @retrofit2.http.Multipart
    @retrofit2.http.POST("api/user/upload-offline-fir/{id}")
    Call<com.example.lawayerapp.models.UploadResponse> uploadOfflineFir(
        @retrofit2.http.Header("Authorization") String token,
        @retrofit2.http.Path("id") String applicationId,
        @retrofit2.http.Part okhttp3.MultipartBody.Part fir
    );

    @POST("api/ai/verify-application")
    Call<com.example.lawayerapp.models.AIValidationResponse> verifyApplication(
        @retrofit2.http.Header("Authorization") String token,
        @Body com.example.lawayerapp.models.SuggestActsRequest request
    );

    @retrofit2.http.POST("api/ai/draft-fir/{id}")
    Call<com.example.lawayerapp.models.DraftFirResponse> draftFir(
        @retrofit2.http.Header("Authorization") String token,
        @retrofit2.http.Path("id") String applicationId
    );
}

package com.example.lawayerapp.controllers;

import com.example.lawayerapp.models.ChatRequest;
import com.example.lawayerapp.models.ChatResponse;
import com.example.lawayerapp.network.ApiClient;
import com.example.lawayerapp.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatController {

    private final ChatView view;
    private final ApiService apiService;
    private final String token;

    public interface ChatView {
        void showLoading();
        void hideLoading();
        void onAiReply(String text);
        void onError(String message);
    }

    public ChatController(ChatView view, String token) {
        this.view = view;
        this.token = token;
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }

    public void sendMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return;
        }

        view.showLoading();

        ChatRequest request = new ChatRequest(message);
        Call<ChatResponse> call = apiService.chatWithAI("Bearer " + token, request);

        call.enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                view.hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    ChatResponse resp = response.body();
                    if (resp.isSuccess() && resp.getReply() != null) {
                        view.onAiReply(resp.getReply());
                    } else {
                        view.onError("AI failed to reply.");
                    }
                } else {
                    view.onError("Server Error: Unable to process request.");
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                view.hideLoading();
                view.onError("Network Error: " + t.getMessage());
            }
        });
    }
}

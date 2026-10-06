package com.example.lawayerapp.views;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lawayerapp.adapters.ApplicationsAdapter;
import com.example.lawayerapp.controllers.ReviewQueueController;
import com.example.lawayerapp.databinding.ActivityReviewHistoryBinding;
import com.example.lawayerapp.models.ApplicationData;
import com.example.lawayerapp.utils.SharedPrefsUtil;

import java.util.ArrayList;
import java.util.List;

public class ReviewHistoryActivity extends AppCompatActivity implements ReviewQueueController.ReviewQueueView {

    private ActivityReviewHistoryBinding binding;
    private ReviewQueueController controller;
    private ApplicationsAdapter adapter;
    private List<ApplicationData> historyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReviewHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.toolbar.setNavigationOnClickListener(v -> finish());
        }

        String token = SharedPrefsUtil.getToken(this);
        controller = new ReviewQueueController(this, token);

        setupRecyclerView();
        controller.fetchHistory();
    }

    private void setupRecyclerView() {
        adapter = new ApplicationsAdapter(historyList);
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.rvHistory.setAdapter(adapter);
    }

    @Override
    public void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
    }

    @Override
    public void setPendingApplications(List<ApplicationData> applications) {
        this.historyList = applications;
        adapter.updateData(applications);
        if (applications.isEmpty()) {
            Toast.makeText(this, "No history found.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStatusUpdated(String message) {
        // Not used here
    }

    @Override
    public void onDraftReceived(String draftText) {
        // Not used here
    }

    @Override
    public void onError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

package com.example.lawayerapp.views;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lawayerapp.adapters.ApplicationsAdapter;
import com.example.lawayerapp.controllers.StatusController;
import com.example.lawayerapp.databinding.ActivityStatusBinding;
import com.example.lawayerapp.models.ApplicationData;
import com.example.lawayerapp.utils.SharedPrefsUtil;

import java.util.ArrayList;
import java.util.List;

public class StatusActivity extends AppCompatActivity implements StatusController.StatusView {

    private ActivityStatusBinding binding;
    private StatusController controller;
    private ApplicationsAdapter adapter;
    private List<ApplicationData> applicationsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatusBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String token = SharedPrefsUtil.getToken(this);
        controller = new StatusController(this, token);

        setupRecyclerView();

        // Fetch Data
        controller.fetchApplications();
    }

    private void setupRecyclerView() {
        adapter = new ApplicationsAdapter(applicationsList);
        binding.rvApplications.setLayoutManager(new LinearLayoutManager(this));
        binding.rvApplications.setAdapter(adapter);
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
    public void setApplications(List<ApplicationData> applications) {
        this.applicationsList = applications;
        adapter.updateData(applications);
        if (applications.isEmpty()) {
            Toast.makeText(this, "No applications found.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void setError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

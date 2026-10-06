package com.example.lawayerapp.views;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lawayerapp.adapters.ActsAdapter;
import com.example.lawayerapp.controllers.DirectFirController;
import com.example.lawayerapp.databinding.ActivityDirectFirBinding;
import com.example.lawayerapp.models.Act;
import com.example.lawayerapp.models.UserSummary;
import com.example.lawayerapp.utils.SharedPrefsUtil;

import java.util.ArrayList;
import java.util.List;

public class DirectFirActivity extends AppCompatActivity implements DirectFirController.DirectFirView {

    private ActivityDirectFirBinding binding;
    private DirectFirController controller;
    private ActsAdapter adapter;
    private List<Act> currentActs = new ArrayList<>();
    
    private String selectedUserId = "";
    private ArrayAdapter<UserSummary> userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDirectFirBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String token = SharedPrefsUtil.getToken(this);
        controller = new DirectFirController(this, token);

        // Setup acts RecyclerView
        adapter = new ActsAdapter(currentActs);
        binding.rvAppliedActs.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAppliedActs.setAdapter(adapter);

        // Fetch citizen list
        controller.fetchCitizens();

        binding.btnSuggestActs.setOnClickListener(v ->
            controller.suggestActs(binding.etFirText.getText().toString()));

        binding.btnFileFir.setOnClickListener(v ->
            controller.fileDirectFir(
                selectedUserId,
                binding.etFirText.getText().toString().trim(),
                currentActs));

        // Listener for search dropdown selection
        binding.etTargetUserId.setOnItemClickListener((parent, view, position, id) -> {
            UserSummary selected = userAdapter.getItem(position);
            if (selected != null) {
                selectedUserId = selected.get_id();
                binding.etTargetUserId.setText(selected.getName());
            }
        });
    }

    @Override public void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnFileFir.setEnabled(false);
        binding.btnSuggestActs.setEnabled(false);
    }

    @Override public void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
        binding.btnFileFir.setEnabled(true);
        binding.btnSuggestActs.setEnabled(true);
    }

    @Override public void setSuggestedActs(List<Act> acts) {
        currentActs = acts;
        adapter.updateData(acts);
    }

    @Override
    public void onCitizensLoaded(List<UserSummary> citizens) {
        userAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, citizens);
        binding.etTargetUserId.setAdapter(userAdapter);
    }

    @Override public void onFirFiled(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override public void onError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

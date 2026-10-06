package com.example.lawayerapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lawayerapp.R;
import com.example.lawayerapp.models.ApplicationData;

import java.util.List;

public class ReviewQueueAdapter extends RecyclerView.Adapter<ReviewQueueAdapter.ReviewViewHolder> {

    private List<ApplicationData> applicationList;
    private final OnReviewAction listener;

    public interface OnReviewAction {
        void onApproveClick(ApplicationData app);
        void onRejectClick(ApplicationData app);
    }

    public ReviewQueueAdapter(List<ApplicationData> applicationList, OnReviewAction listener) {
        this.applicationList = applicationList;
        this.listener = listener;
    }

    public void updateData(List<ApplicationData> newApplicationList) {
        this.applicationList = newApplicationList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review_application, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        ApplicationData app = applicationList.get(position);

        String date = app.getCreatedAt() != null ? app.getCreatedAt().substring(0, 10) : "N/A";
        holder.tvDate.setText("Date: " + date);
        holder.tvDescription.setText(app.getDescription());

        holder.btnApprove.setOnClickListener(v -> listener.onApproveClick(app));
        holder.btnReject.setOnClickListener(v -> listener.onRejectClick(app));
    }

    @Override
    public int getItemCount() {
        return applicationList == null ? 0 : applicationList.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvDescription;
        Button btnApprove, btnReject;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}

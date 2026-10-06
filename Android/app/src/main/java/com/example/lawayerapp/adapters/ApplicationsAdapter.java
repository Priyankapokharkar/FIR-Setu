package com.example.lawayerapp.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lawayerapp.R;
import com.example.lawayerapp.models.ApplicationData;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class ApplicationsAdapter extends RecyclerView.Adapter<ApplicationsAdapter.AppViewHolder> {

    private List<ApplicationData> applicationList;

    public ApplicationsAdapter(List<ApplicationData> applicationList) {
        this.applicationList = applicationList;
    }

    public void updateData(List<ApplicationData> newApplicationList) {
        this.applicationList = newApplicationList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_application, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        ApplicationData app = applicationList.get(position);

        String date = app.getCreatedAt() != null ? app.getCreatedAt().substring(0, 10) : "N/A";
        holder.tvDate.setText("Date: " + date);
        holder.tvDescription.setText(app.getDescription());
        holder.tvStatus.setText(app.getStatus());

        if (app.getAssignedConstable() != null) {
            holder.tvConstableName.setText("Assigned: Officer " + app.getAssignedConstable().getName());
        } else {
            holder.tvConstableName.setText("Assigned: Pending Assignment");
        }

        // Apply Color Mapping Based on status
        if ("Approved".equalsIgnoreCase(app.getStatus())) {
            holder.tvStatus.setTextColor(Color.parseColor("#10B981")); // Green
            
            // Show Download Button if PDF exists
            if (app.getGeneratedFirUrl() != null && !app.getGeneratedFirUrl().isEmpty()) {
                holder.btnDownloadPdf.setVisibility(View.VISIBLE);
                holder.btnDownloadPdf.setOnClickListener(v -> {
                    String url = app.getGeneratedFirUrl();
                    android.content.Intent browserIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url));
                    v.getContext().startActivity(browserIntent);
                });
            } else {
                holder.btnDownloadPdf.setVisibility(View.GONE);
            }

            // Show On-Screen FIR Text if available
            if (app.getLinkedFir() != null && app.getLinkedFir().getFirText() != null) {
                holder.btnViewFir.setVisibility(View.VISIBLE);
                holder.tvFirContent.setText(app.getLinkedFir().getFirText());
                holder.btnViewFir.setOnClickListener(v -> {
                    if (holder.layoutFirText.getVisibility() == View.VISIBLE) {
                        holder.layoutFirText.setVisibility(View.GONE);
                        holder.btnViewFir.setText("View FIR Text");
                    } else {
                        holder.layoutFirText.setVisibility(View.VISIBLE);
                        holder.btnViewFir.setText("Hide FIR Text");
                    }
                });
            } else {
                holder.btnViewFir.setVisibility(View.GONE);
            }

            // Enable Click to Compare AI
            holder.itemView.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(v.getContext(), com.example.lawayerapp.views.FirComparisonActivity.class);
                intent.putExtra("APPLICATION_ID", app.getId());
                v.getContext().startActivity(intent);
            });
        } else if ("Rejected".equalsIgnoreCase(app.getStatus())) {
            holder.tvStatus.setTextColor(Color.parseColor("#EF4444")); // Red
            holder.btnDownloadPdf.setVisibility(View.GONE);
            holder.btnViewFir.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(null);
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#F59E0B")); // Amber
            holder.btnDownloadPdf.setVisibility(View.GONE);
            holder.btnViewFir.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return applicationList == null ? 0 : applicationList.size();
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvStatus, tvDescription, tvConstableName, tvFirContent;
        MaterialButton btnDownloadPdf, btnViewFir;
        View layoutFirText;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvConstableName = itemView.findViewById(R.id.tvConstableName);
            btnDownloadPdf = itemView.findViewById(R.id.btnDownloadPdf);
            btnViewFir = itemView.findViewById(R.id.btnViewFir);
            layoutFirText = itemView.findViewById(R.id.layoutFirText);
            tvFirContent = itemView.findViewById(R.id.tvFirContent);
        }
    }
}

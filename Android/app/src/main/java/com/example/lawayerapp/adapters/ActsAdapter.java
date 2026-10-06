package com.example.lawayerapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lawayerapp.R;
import com.example.lawayerapp.models.Act;

import java.util.List;

public class ActsAdapter extends RecyclerView.Adapter<ActsAdapter.ActViewHolder> {

    private List<Act> actsList;

    public ActsAdapter(List<Act> actsList) {
        this.actsList = actsList;
    }

    public void updateData(List<Act> newActsList) {
        this.actsList = newActsList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ActViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_act, parent, false);
        return new ActViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActViewHolder holder, int position) {
        Act act = actsList.get(position);
        holder.tvActName.setText(act.getActName());
        holder.tvActDescription.setText(act.getDescription());
    }

    @Override
    public int getItemCount() {
        return actsList == null ? 0 : actsList.size();
    }

    static class ActViewHolder extends RecyclerView.ViewHolder {
        TextView tvActName, tvActDescription;

        public ActViewHolder(@NonNull View itemView) {
            super(itemView);
            tvActName = itemView.findViewById(R.id.tvActName);
            tvActDescription = itemView.findViewById(R.id.tvActDescription);
        }
    }
}

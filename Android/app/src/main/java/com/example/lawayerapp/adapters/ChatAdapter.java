package com.example.lawayerapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lawayerapp.R;
import com.example.lawayerapp.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messages.get(position);

        if (message.isAi()) {
            holder.layAiMessage.setVisibility(View.VISIBLE);
            holder.layUserMessage.setVisibility(View.GONE);
            holder.tvAiMessage.setText(message.getText());
        } else {
            holder.layAiMessage.setVisibility(View.GONE);
            holder.layUserMessage.setVisibility(View.VISIBLE);
            holder.tvUserMessage.setText(message.getText());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layAiMessage, layUserMessage;
        TextView tvAiMessage, tvUserMessage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            layAiMessage = itemView.findViewById(R.id.layAiMessage);
            layUserMessage = itemView.findViewById(R.id.layUserMessage);
            tvAiMessage = itemView.findViewById(R.id.tvAiMessage);
            tvUserMessage = itemView.findViewById(R.id.tvUserMessage);
        }
    }
}

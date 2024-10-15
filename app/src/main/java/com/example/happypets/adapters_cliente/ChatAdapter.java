package com.example.happypets.adapters_cliente;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.happypets.R;
import com.example.happypets.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<ChatMessage> messages;

    public ChatAdapter() {
        this.messages = new ArrayList<>();
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
        holder.textViewMessage.setText(message.getText());

        // Establece el estilo del mensaje según si es del usuario o de la API
        if (message.isUserMessage()) {
            holder.textViewMessage.setBackgroundResource(R.drawable.container_background);
        } else {
            holder.textViewMessage.setBackgroundResource(R.drawable.container_background);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMessage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.text_view_message);
        }
    }
}

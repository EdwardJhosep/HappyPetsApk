package com.example.happypets.adapters_cliente;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.happypets.R;
import com.example.happypets.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<ChatMessage> messages;

    public ChatAdapter(Context context) {
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

        // Establecer el ícono y la alineación
        if (message.isUserMessage()) {
            holder.iconUser.setVisibility(View.VISIBLE);
            holder.iconBot.setVisibility(View.GONE);
            holder.alignUserMessage();
            holder.textViewMessage.setBackgroundResource(R.drawable.user_message_background);
        } else {
            holder.iconUser.setVisibility(View.GONE);
            holder.iconBot.setVisibility(View.VISIBLE);
            holder.alignBotMessage();
            holder.textViewMessage.setBackgroundResource(R.drawable.bot_message_background);
        }

        if (message.hasImage()) {
            holder.imageViewProduct.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(message.getImageUrl())
                    .into(holder.imageViewProduct);
        } else {
            holder.imageViewProduct.setVisibility(View.GONE);
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
        ImageView iconUser;
        ImageView iconBot;
        ImageView imageViewProduct;
        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.text_view_message);
            iconUser = itemView.findViewById(R.id.icon_user);
            iconBot = itemView.findViewById(R.id.icon_bot);
            imageViewProduct = itemView.findViewById(R.id.image_view_product);
        }

        void alignUserMessage() {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) textViewMessage.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_END);
            textViewMessage.setLayoutParams(params);
            textViewMessage.setBackgroundResource(R.drawable.container_background_rounded);
        }

        void alignBotMessage() {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) textViewMessage.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_START);
            textViewMessage.setLayoutParams(params);
            textViewMessage.setBackgroundResource(R.drawable.container_background_rounded);
        }
    }
}
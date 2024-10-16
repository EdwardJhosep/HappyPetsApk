package com.example.happypets.adapters_cliente;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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

        // Establecer los íconos y estilos según el tipo de mensaje
        if (message.isUserMessage()) {
            // Mostrar ícono del usuario y ocultar ícono del bot
            holder.iconUser.setVisibility(View.VISIBLE);
            holder.iconBot.setVisibility(View.GONE);

            // Alineación a la derecha para mensajes del usuario
            ViewGroup.LayoutParams params = holder.textViewMessage.getLayoutParams();
            ((RelativeLayout.LayoutParams) params).addRule(RelativeLayout.ALIGN_PARENT_END);
            holder.textViewMessage.setLayoutParams(params);
            holder.textViewMessage.setBackgroundResource(R.drawable.container_background_rounded);  // Cambia al fondo correspondiente para el usuario

            // Establecer ícono del usuario
            holder.iconUser.setImageResource(R.drawable.ic_user);  // Cambia esto al nombre de tu ícono
        } else {
            // Mostrar ícono del bot y ocultar ícono del usuario
            holder.iconUser.setVisibility(View.GONE);
            holder.iconBot.setVisibility(View.VISIBLE);

            // Alineación a la izquierda para mensajes de la API
            ViewGroup.LayoutParams params = holder.textViewMessage.getLayoutParams();
            ((RelativeLayout.LayoutParams) params).removeRule(RelativeLayout.ALIGN_PARENT_END);
            ((RelativeLayout.LayoutParams) params).addRule(RelativeLayout.ALIGN_PARENT_START);
            holder.textViewMessage.setLayoutParams(params);
            holder.textViewMessage.setBackgroundResource(R.drawable.container_background_rounded);  // Cambia al fondo correspondiente para el bot

            // Establecer ícono del bot
            holder.iconBot.setImageResource(R.drawable.ic_chatbot);  // Cambia esto al nombre de tu ícono
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
        ImageView iconUser;  // Ícono del usuario
        ImageView iconBot;   // Ícono del bot

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.text_view_message);
            iconUser = itemView.findViewById(R.id.icon_user);
            iconBot = itemView.findViewById(R.id.icon_bot);
        }
    }
}

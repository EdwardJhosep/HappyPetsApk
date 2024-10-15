package com.example.happypets.adapters_admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.happypets.R;
import com.example.happypets.models.User;

import java.util.List;

public class UsuariosAdapter extends RecyclerView.Adapter<UsuariosAdapter.UsuarioViewHolder> {
    private List<User> usuarios;

    public UsuariosAdapter(List<User> usuarios) {
        this.usuarios = usuarios;
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_usuario, parent, false);
        return new UsuarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        User usuario = usuarios.get(position);
        holder.nombreTextView.setText(usuario.getNombres());
        holder.telefonoTextView.setText(usuario.getTelefono());
        holder.ubicacionTextView.setText(usuario.getUbicacion());
    }

    @Override
    public int getItemCount() {
        return usuarios.size();
    }

    public static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        TextView nombreTextView;
        TextView telefonoTextView;
        TextView ubicacionTextView;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreTextView = itemView.findViewById(R.id.nombreTextView);
            telefonoTextView = itemView.findViewById(R.id.telefonoTextView);
            ubicacionTextView = itemView.findViewById(R.id.ubicacionTextView);
        }
    }
}

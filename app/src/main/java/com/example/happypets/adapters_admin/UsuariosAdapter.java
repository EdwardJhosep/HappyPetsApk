package com.example.happypets.adapters_admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.happypets.R;
import com.example.happypets.models.User;

import java.util.ArrayList;
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
        holder.ubicacionTextView.setText(usuario.getUbicacion() != null && !usuario.getUbicacion().isEmpty() ? usuario.getUbicacion() : "Sin ubicación");
        holder.permisosTextView.setText(usuario.getPermisos() != null ? String.join(", ", usuario.getPermisos()) : "Sin permisos");
    }

    @Override
    public int getItemCount() {
        return usuarios.size();
    }

    public void updateUsuarios(List<User> nuevosUsuarios) {
        List<User> usuariosFiltrados = new ArrayList<>();
        for (User usuario : nuevosUsuarios) {
            if (usuario.getPermisos() != null && usuario.getPermisos().size() == 1 && usuario.getPermisos().get(0).equals("Usuario")) {
                usuariosFiltrados.add(usuario);
            }
        }
        this.usuarios = usuariosFiltrados;
        notifyDataSetChanged();
    }

    public void filtrarPorDNI(String dni, List<User> listaOriginal) {
        List<User> usuariosFiltrados = new ArrayList<>();
        for (User usuario : listaOriginal) {
            if (usuario.getPermisos() != null &&
                    usuario.getPermisos().size() == 1 &&
                    usuario.getPermisos().get(0).equals("Usuario") &&
                    usuario.getDni() != null &&
                    usuario.getDni().toLowerCase().contains(dni.toLowerCase())) {
                usuariosFiltrados.add(usuario);
            }
        }

        this.usuarios = usuariosFiltrados;
        notifyDataSetChanged();
    }


    public static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        TextView nombreTextView;
        TextView telefonoTextView;
        TextView ubicacionTextView;
        TextView permisosTextView;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreTextView = itemView.findViewById(R.id.nombreTextView);
            telefonoTextView = itemView.findViewById(R.id.telefonoTextView);
            ubicacionTextView = itemView.findViewById(R.id.ubicacionTextView);
            permisosTextView = itemView.findViewById(R.id.permisosTextView);
        }
    }
}

package com.example.happypets.adapters_admin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // Cambiado a ImageButton
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.happypets.R;
import com.example.happypets.dialogs_admin.DarPermisoDialogFragment;
import com.example.happypets.models.User;

import java.util.ArrayList;
import java.util.List;

public class UsuariosAdapter2 extends RecyclerView.Adapter<UsuariosAdapter2.UsuarioViewHolder> {
    private List<User> usuarios;
    private String token; // Campo para almacenar el token

    public UsuariosAdapter2(List<User> usuarios, String token) {
        this.usuarios = usuarios;
        this.token = token; // Inicializa el token
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

        // Maneja el clic en el botón darPermisoButton
        holder.darPermisoButton.setOnClickListener(view -> {
            mostrarFormularioDarPermiso(usuario, token, holder.itemView.getContext());
        });
    }

    private void mostrarFormularioDarPermiso(User usuario, String token, Context context) {
        // Obtén el ID del usuario
        String userId = usuario.getId(); // Asegúrate de que este método exista en tu clase User

        // Pasa el token y el ID al dialog fragment
        DarPermisoDialogFragment dialog = new DarPermisoDialogFragment(token, userId, usuario);
        dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "darPermiso");
    }


    @Override
    public int getItemCount() {
        return usuarios.size();
    }

    public void updateUsuarios(List<User> nuevosUsuarios) {
        List<User> usuariosFiltrados = new ArrayList<>();
        for (User usuario : nuevosUsuarios) {
            // Verifica si el usuario tiene más de un permiso
            if (usuario.getPermisos() != null && usuario.getPermisos().size() > 1) {
                usuariosFiltrados.add(usuario);
            } else if (usuario.getPermisos() != null && usuario.getPermisos().size() == 1 &&
                    !usuario.getPermisos().get(0).equals("Cliente")) {
                // Incluye usuarios que tienen un único permiso, siempre que no sea "Usuario"
                usuariosFiltrados.add(usuario);
            }
        }
        this.usuarios = usuariosFiltrados;
        notifyDataSetChanged();
    }



    public void filtrarPorDNI(String dni, List<User> listaOriginal) {
        List<User> usuariosFiltrados = new ArrayList<>();
        for (User usuario : listaOriginal) {
            // Verifica si el usuario tiene más de un permiso
            if (usuario.getPermisos() != null && usuario.getPermisos().size() > 1) {
                // Añade el usuario si tiene más de un permiso
                usuariosFiltrados.add(usuario);
            } else if (usuario.getPermisos() != null && usuario.getPermisos().size() == 1 &&
                    !usuario.getPermisos().get(0).equals("Cliente") &&
                    usuario.getDni() != null &&
                    usuario.getDni().toLowerCase().contains(dni.toLowerCase())) {
                // Incluye usuarios que tienen un único permiso, siempre que no sea "Usuario"
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
        ImageButton darPermisoButton; // Cambiado a ImageButton

        @SuppressLint("WrongViewCast")
        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreTextView = itemView.findViewById(R.id.nombreTextView);
            telefonoTextView = itemView.findViewById(R.id.telefonoTextView);
            ubicacionTextView = itemView.findViewById(R.id.ubicacionTextView);
            permisosTextView = itemView.findViewById(R.id.permisosTextView);
            darPermisoButton = itemView.findViewById(R.id.darPermisoButton); // Inicializa el ImageButton
        }
    }
}

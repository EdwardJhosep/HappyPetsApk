package com.example.happypets.adapters_admin;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.example.happypets.R;
import com.example.happypets.models.Servicio;
import com.bumptech.glide.Glide;

import java.util.List;

public class ServicioAdapter extends RecyclerView.Adapter<ServicioAdapter.ServicioViewHolder> {

    private List<Servicio> servicios;
    private String token;  // Variable para almacenar el token

    public ServicioAdapter(List<Servicio> servicios, String token) {
        this.servicios = servicios;
        this.token = token;  // Guardar el token
    }

    @NonNull
    @Override
    public ServicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_servicio, parent, false);
        return new ServicioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServicioViewHolder holder, int position) {
        Servicio servicio = servicios.get(position);

        holder.tipoTextView.setText(servicio.getTipo());
        holder.descripcionTextView.setText(servicio.getDescripcion());

        Glide.with(holder.itemView.getContext())
                .load("https://api.happypetshco.com/ServidorServicios/" + servicio.getImagen())
                .into(holder.imagenImageView);

        // Configurar el botón de edición para mostrar el formulario flotante
        holder.editarServicioButton.setOnClickListener(v -> {
            // Crear y mostrar el DialogFragment
            Bundle args = new Bundle();

            // Enviar los datos al DialogFragment
            args.putString("tipoServicio", servicio.getTipo());
            args.putString("descripcionServicio", servicio.getDescripcion());
            args.putString("imagenServicio", servicio.getImagen());
            args.putString("token", token);  // Enviar el token al DialogFragment

            // Mostrar el fragmento flotante
        });
    }

    @Override
    public int getItemCount() {
        return servicios.size();
    }

    public static class ServicioViewHolder extends RecyclerView.ViewHolder {

        TextView tipoTextView;
        TextView descripcionTextView;
        ImageView imagenImageView;
        ImageButton editarServicioButton; // Referencia al botón de edición

        @SuppressLint("WrongViewCast")
        public ServicioViewHolder(View itemView) {
            super(itemView);
            tipoTextView = itemView.findViewById(R.id.tipoServicioTextView);
            descripcionTextView = itemView.findViewById(R.id.descripcionServicioTextView);
            imagenImageView = itemView.findViewById(R.id.imagenServicioImageView);
            editarServicioButton = itemView.findViewById(R.id.editarServicioButton); // Asignamos el botón
        }
    }
}

package com.example.happypets.adapters_cliente;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.happypets.models.Mascota;
import com.example.happypets.R;

import java.util.List;

public class MascotaAdapter extends RecyclerView.Adapter<MascotaAdapter.MascotaViewHolder> {

    private final List<Mascota> mascotas;
    private final Context context;

    public MascotaAdapter(Context context, List<Mascota> mascotas) {
        this.context = context;
        this.mascotas = mascotas;
    }

    @NonNull
    @Override
    public MascotaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_mascota, parent, false);
        return new MascotaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MascotaViewHolder holder, int position) {
        Mascota mascota = mascotas.get(position);
        holder.nombreTextView.setText(mascota.getNombre());
        holder.edadTextView.setText("Edad: " + mascota.getEdad());
        holder.especieTextView.setText("Especie: " + mascota.getEspecie());
        holder.razaTextView.setText("Raza: " + mascota.getRaza());
        holder.sexoTextView.setText("Sexo: " + mascota.getSexo());
        holder.estadoTextView.setText("Estado: " + mascota.getEstado());

        // Cambiar el color de fondo del estadoTextView si está activo
        if (mascota.getEstado().equalsIgnoreCase("activo")) {
            holder.estadoTextView.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_light));
            holder.estadoTextView.setTextColor(context.getResources().getColor(android.R.color.white));
        } else {
            holder.estadoTextView.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
            holder.estadoTextView.setTextColor(context.getResources().getColor(android.R.color.black));
        }

        // Establecer la imagen según la especie
        String especie = mascota.getEspecie().toLowerCase();
        if (especie.equals("perro")) {
            holder.mascotaImageView.setImageResource(R.drawable.perro);
        } else if (especie.equals("gato")) {
            holder.mascotaImageView.setImageResource(R.drawable.gato);
        } else {
            holder.mascotaImageView.setImageResource(R.drawable.default_mascota);
        }
    }

    @Override
    public int getItemCount() {
        return mascotas.size();
    }

    public static class MascotaViewHolder extends RecyclerView.ViewHolder {
        TextView nombreTextView;
        TextView edadTextView;
        TextView especieTextView;
        TextView razaTextView;
        TextView sexoTextView;
        TextView estadoTextView;
        ImageView mascotaImageView;

        public MascotaViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreTextView = itemView.findViewById(R.id.nombreTextView);
            edadTextView = itemView.findViewById(R.id.edadTextView);
            especieTextView = itemView.findViewById(R.id.especieTextView);
            razaTextView = itemView.findViewById(R.id.razaTextView);
            sexoTextView = itemView.findViewById(R.id.sexoTextView);
            estadoTextView = itemView.findViewById(R.id.estadoTextView);
            mascotaImageView = itemView.findViewById(R.id.mascotaImageView);
        }
    }
}

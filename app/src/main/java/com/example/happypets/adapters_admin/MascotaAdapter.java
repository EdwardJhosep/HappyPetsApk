package com.example.happypets.adapters_admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.happypets.R;
import com.example.happypets.models.Mascota;

import java.util.List;

public class MascotaAdapter extends RecyclerView.Adapter<MascotaAdapter.MascotaViewHolder> {

    private List<Mascota> listaMascotas;

    public MascotaAdapter(List<Mascota> listaMascotas) {
        this.listaMascotas = listaMascotas;
    }

    @NonNull
    @Override
    public MascotaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mascota_admin, parent, false);
        return new MascotaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MascotaViewHolder holder, int position) {
        Mascota mascota = listaMascotas.get(position);
        holder.nombreTextView.setText(mascota.getNombre());
        holder.edadTextView.setText("Edad: " + mascota.getEdad());
        holder.especieTextView.setText("Especie: " + mascota.getEspecie());
        holder.razaTextView.setText("Raza: " + mascota.getRaza());
        holder.sexoTextView.setText("Sexo: " + mascota.getSexo());

        // Cambiar el drawable del estadoView según el estado de la mascota
        if (mascota.getEstado().equalsIgnoreCase("activo")) {
            holder.estadoView.setBackgroundResource(R.drawable.circle_green); // Drawable verde
        } else {
            holder.estadoView.setBackgroundResource(R.drawable.circle_red); // Drawable rojo
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
        return listaMascotas.size();
    }

    public static class MascotaViewHolder extends RecyclerView.ViewHolder {
        TextView nombreTextView, edadTextView, especieTextView, razaTextView, sexoTextView;
        View estadoView; // Mantener como View
        ImageView mascotaImageView;

        public MascotaViewHolder(@NonNull View itemView) {
            super(itemView);
            mascotaImageView = itemView.findViewById(R.id.mascotaImageView);
            nombreTextView = itemView.findViewById(R.id.nombreTextView);
            edadTextView = itemView.findViewById(R.id.edadTextView);
            especieTextView = itemView.findViewById(R.id.especieTextView);
            razaTextView = itemView.findViewById(R.id.razaTextView);
            sexoTextView = itemView.findViewById(R.id.sexoTextView);
            estadoView = itemView.findViewById(R.id.estadoView); // Se mantiene como View
        }
    }
}

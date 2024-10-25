package com.example.happypets.adapters_cliente;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Importar Glide
import com.example.happypets.models.Mascota;
import com.example.happypets.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MascotaAdapter extends RecyclerView.Adapter<MascotaAdapter.MascotaViewHolder> {

    private final List<Mascota> mascotas;
    private final Context context;

    // Mapa para almacenar el color de fondo según el estado
    private static final Map<String, Integer> estadoColorMap = new HashMap<>();

    static {
        estadoColorMap.put("activo", R.drawable.circle_green);
        estadoColorMap.put("inactivo", R.drawable.circle_red);
        // Agrega más estados según sea necesario
    }

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
        holder.bind(mascota);
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
        View estadoView;  // Mantenemos como View
        ImageView mascotaImageView;

        @SuppressLint("WrongViewCast")
        public MascotaViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreTextView = itemView.findViewById(R.id.nombreTextView);
            edadTextView = itemView.findViewById(R.id.edadTextView);
            especieTextView = itemView.findViewById(R.id.especieTextView);
            razaTextView = itemView.findViewById(R.id.razaTextView);
            sexoTextView = itemView.findViewById(R.id.sexoTextView);
            estadoView = itemView.findViewById(R.id.estadoView);  // Asegúrate de que el ID sea correcto
            mascotaImageView = itemView.findViewById(R.id.mascotaImageView);
        }

        public void bind(Mascota mascota) {
            nombreTextView.setText(mascota.getNombre());
            edadTextView.setText("Edad: " + mascota.getEdad());
            especieTextView.setText("Especie: " + mascota.getEspecie());
            razaTextView.setText("Raza: " + mascota.getRaza());
            sexoTextView.setText("Sexo: " + mascota.getSexo());

            // Cambiar el color de fondo de estadoView según el estado
            estadoView.setBackgroundResource(estadoColorMap.getOrDefault(mascota.getEstado().toLowerCase(), R.drawable.circle_background));

            // Cargar la imagen usando Glide
            String imagenUrl = "https://api-happypetshco-com.preview-domain.com/ServidorMascotas/" + mascota.getImagen();
            Glide.with(mascotaImageView.getContext())
                    .load(imagenUrl)
                    .placeholder(R.drawable.placeholder_image) // Imagen de carga
                    .error(R.drawable.logo_eliminar) // Imagen de error
                    .into(mascotaImageView);
        }
    }
}

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
        estadoColorMap.put("activo", R.drawable.circle_green); // Color para estado activo
        estadoColorMap.put("inactivo", R.drawable.circle_red); // Color para estado inactivo
        // Agrega más estados según sea necesario
    }

    public MascotaAdapter(Context context, List<Mascota> mascotas) {
        this.context = context;
        this.mascotas = mascotas;
    }

    @NonNull
    @Override
    public MascotaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflar el layout para cada item de la lista
        View view = LayoutInflater.from(context).inflate(R.layout.item_mascota, parent, false);
        return new MascotaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MascotaViewHolder holder, int position) {
        // Obtener la mascota correspondiente a la posición
        Mascota mascota = mascotas.get(position);
        holder.bind(mascota); // Llamar al método bind para configurar la vista
    }

    @Override
    public int getItemCount() {
        return mascotas.size(); // Número total de elementos en la lista
    }

    public static class MascotaViewHolder extends RecyclerView.ViewHolder {
        // Definir las vistas de cada item
        TextView nombreTextView;
        TextView edadTextView;
        TextView especieTextView;
        View estadoView;  // Vista para el estado (sobre la imagen)
        ImageView mascotaImageView; // Imagen circular de la mascota

        @SuppressLint("WrongViewCast")
        public MascotaViewHolder(@NonNull View itemView) {
            super(itemView);
            // Inicializar las vistas
            nombreTextView = itemView.findViewById(R.id.nombreTextView);
            edadTextView = itemView.findViewById(R.id.edadTextView);
            especieTextView = itemView.findViewById(R.id.especieTextView);
            estadoView = itemView.findViewById(R.id.estadoView);  // Vista para el estado
            mascotaImageView = itemView.findViewById(R.id.mascotaImageView); // Imagen de la mascota
        }

        public void bind(Mascota mascota) {
            // Establecer los valores de las vistas a partir de la mascota
            nombreTextView.setText(mascota.getNombre());
            edadTextView.setText("" + mascota.getEdad());
            especieTextView.setText("" + mascota.getEspecie());

            // Cambiar el color de fondo de estadoView según el estado de la mascota
            estadoView.setBackgroundResource(estadoColorMap.getOrDefault(mascota.getEstado().toLowerCase(), R.drawable.circle_background));

            // Cargar la imagen de la mascota usando Glide
            String imagenUrl = "https://api.happypetshco.com/ServidorMascotas/" + mascota.getImagen();
            Glide.with(mascotaImageView.getContext())
                    .load(imagenUrl) // URL de la imagen
                    .placeholder(R.drawable.placeholder_image) // Imagen de carga
                    .error(R.drawable.logo_eliminar) // Imagen de error
                    .into(mascotaImageView); // Colocar la imagen en el ImageView
        }
    }
}

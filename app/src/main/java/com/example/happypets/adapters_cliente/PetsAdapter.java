package com.example.happypets.adapters_cliente;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.happypets.R;
import com.example.happypets.models.Mascota;
import com.example.happypets.perfilview.VerCitasActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PetsAdapter extends RecyclerView.Adapter<PetsAdapter.PetViewHolder> {

    private Context context;
    private ArrayList<Mascota> petsList;
    private Map<String, Integer> estadoColorMap;
    private String userId;
    private String token;
    public PetsAdapter(Context context, ArrayList<Mascota> petsList, String userId, String token) {
        this.context = context;
        this.petsList = petsList;
        this.userId = userId;
        this.token = token;

        // Mapa de colores de estado, ajusta según los estados que necesites
        estadoColorMap = new HashMap<>();
        estadoColorMap.put("activo", R.drawable.circle_green);
        estadoColorMap.put("inactivo", R.drawable.circle_red);
        estadoColorMap.put("en tratamiento", R.drawable.circle_background);
    }

    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_mascota2, parent, false);
        return new PetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        Mascota mascota = petsList.get(position);
        holder.bind(mascota);
        holder.verHistorialButton.setOnClickListener(v -> {
            String mascotaId = mascota.getId();
            if (context instanceof VerCitasActivity) {
                ((VerCitasActivity) context).mostrarDatosMascota(mascotaId);
                ((VerCitasActivity) context).mostrarHistorialCompleto(mascotaId);
            }
        });
    }






    @Override
    public int getItemCount() {
        return petsList.size();
    }

    // ViewHolder para cada item en el RecyclerView
    public class PetViewHolder extends RecyclerView.ViewHolder {
        ImageButton verHistorialButton;
        TextView nombreTextView;
        TextView edadTextView;
        TextView especieTextView;
        View estadoView;
        ImageView mascotaImageView;

        public PetViewHolder(View itemView) {
            super(itemView);
            nombreTextView = itemView.findViewById(R.id.nombreTextView);
            edadTextView = itemView.findViewById(R.id.edadTextView);
            especieTextView = itemView.findViewById(R.id.especieTextView);
            estadoView = itemView.findViewById(R.id.estadoView);
            mascotaImageView = itemView.findViewById(R.id.mascotaImageView);
            verHistorialButton = itemView.findViewById(R.id.verHistorialButton);  // Ensure this line is correct
        }


        public void bind(Mascota mascota) {
            nombreTextView.setText(mascota.getNombre());
            edadTextView.setText(String.valueOf(mascota.getEdad()));
            especieTextView.setText(mascota.getEspecie());

            // Cambiar el color de fondo de estadoView según el estado de la mascota
            estadoView.setBackgroundResource(estadoColorMap.getOrDefault(mascota.getEstado().toLowerCase(), R.drawable.circle_background));

            // Cargar la imagen de la mascota usando Glide
            String imagenUrl = "https://api.happypetshco.com/ServidorMascotas/" + mascota.getImagen();
            Glide.with(mascotaImageView.getContext())
                    .load(imagenUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.logo_eliminar)
                    .into(mascotaImageView);
        }
    }
}

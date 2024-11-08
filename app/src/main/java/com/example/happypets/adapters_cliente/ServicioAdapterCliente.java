package com.example.happypets.adapters_cliente;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.happypets.R;
import com.example.happypets.dialogs_cliente.CrearCitaDialogFragment;
import com.example.happypets.models.Servicio;

import java.util.List;

public class ServicioAdapterCliente extends RecyclerView.Adapter<ServicioAdapterCliente.ServicioViewHolder> {

    private List<Servicio> servicios;
    private String token;
    private String userId;

    public ServicioAdapterCliente(List<Servicio> servicios, String token, String userId) {
        this.servicios = servicios;
        this.token = token;
        this.userId = userId;
    }

    @NonNull
    @Override
    public ServicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_servicio_cliente, parent, false);
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

        holder.reservarButton.setOnClickListener(view -> {
            CrearCitaDialogFragment crearCitaDialogFragment = new CrearCitaDialogFragment();
            Bundle args = new Bundle();
            args.putString("token", token);
            args.putString("userId", userId);  // Pasar userId a los argumentos
            crearCitaDialogFragment.setArguments(args);
            crearCitaDialogFragment.show(((AppCompatActivity) holder.itemView.getContext()).getSupportFragmentManager(), "CrearCitaDialogFragment");
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
        Button reservarButton;

        public ServicioViewHolder(View itemView) {
            super(itemView);
            tipoTextView = itemView.findViewById(R.id.tipoServicioTextView);
            descripcionTextView = itemView.findViewById(R.id.descripcionServicioTextView);
            imagenImageView = itemView.findViewById(R.id.imagenServicioImageView);
            reservarButton = itemView.findViewById(R.id.reservarButton);
        }
    }
}

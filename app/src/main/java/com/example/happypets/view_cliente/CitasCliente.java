package com.example.happypets.view_cliente;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.happypets.R;

public class CitasCliente extends Fragment {

    private String userId;
    private String token; // Añadir variable para el token

    // Método para crear una nueva instancia del fragmento
    public static CitasCliente newInstance(String userId, String token) {
        CitasCliente fragment = new CitasCliente();
        Bundle args = new Bundle();
        args.putString("USER_ID", userId);
        args.putString("TOKEN", token); // Guardar el token en los argumentos
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Infla el diseño para este fragmento
        return inflater.inflate(R.layout.activity_citas_cliente, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Obtener el userId y el token de los argumentos
        if (getArguments() != null) {
            userId = getArguments().getString("USER_ID");
            token = getArguments().getString("TOKEN"); // Leer el token
        }

        // Aquí puedes usar userId y token según sea necesario
    }
}

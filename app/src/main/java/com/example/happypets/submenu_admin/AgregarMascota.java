package com.example.happypets.submenu_admin;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.happypets.R;

public class AgregarMascota extends Fragment {

    private static final String ARG_TOKEN = "token"; // Clave para el token
    private String token; // Variable para almacenar el token

    // Método estático para crear una nueva instancia del fragmento y pasar el token
    public static AgregarMascota newInstance(String token) {
        AgregarMascota fragment = new AgregarMascota();
        Bundle args = new Bundle();
        args.putString(ARG_TOKEN, token); // Almacenar el token en el Bundle
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Obtener el token del Bundle
        if (getArguments() != null) {
            token = getArguments().getString(ARG_TOKEN);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Infla el diseño para este fragmento
        View view = inflater.inflate(R.layout.activity_agregar_mascota, container, false);

        // Aquí puedes usar el token según lo que necesites, por ejemplo:
        // Log.d("AgregarMascota", "Token recibido: " + token);

        return view;
    }
}

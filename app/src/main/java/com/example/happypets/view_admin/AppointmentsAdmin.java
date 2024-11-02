package com.example.happypets.view_admin;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.happypets.R;

public class AppointmentsAdmin extends Fragment {

    private static final String ARG_TOKEN = "token"; // Definir una clave para el token
    private String token; // Variable para almacenar el token

    // Método estático para crear una nueva instancia del fragmento
    public static AppointmentsAdmin newInstance(String token) {
        AppointmentsAdmin fragment = new AppointmentsAdmin();
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
        View view = inflater.inflate(R.layout.activity_appointments_admin, container, false);

        // Aquí puedes usar el token según sea necesario
        // Ejemplo: mostrar el token en un log
        System.out.println("Token: " + token);

        return view;
    }
}

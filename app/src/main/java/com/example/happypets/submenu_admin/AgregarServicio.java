package com.example.happypets.submenu_admin;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

public class AgregarServicio extends Fragment {

    private String token;

    public static AgregarServicio newInstance(String token) {
        AgregarServicio fragment = new AgregarServicio();
        Bundle args = new Bundle();
        args.putString("token", token); // Pasar el token al fragmento
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            token = getArguments().getString("token");
        }
    }

    // Implementa la lógica para agregar nuevos servicios aquí
}

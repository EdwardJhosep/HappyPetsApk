package com.example.happypets.submenu_admin;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.happypets.R;

public class AgregarMascota extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Infla el diseño para este fragmento
        return inflater.inflate(R.layout.activity_agregar_mascota, container, false);
    }
}
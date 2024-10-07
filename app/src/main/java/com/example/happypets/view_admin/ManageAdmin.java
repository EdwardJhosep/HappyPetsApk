package com.example.happypets.view_admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.happypets.R;
import com.example.happypets.submenu_admin.Submenu_AdminMascotas;
import com.example.happypets.submenu_admin.Submenu_AdminPersonal;
import com.example.happypets.submenu_admin.Submenu_AdminProductos;
import com.example.happypets.submenu_admin.Submenu_AdminServicios;

public class ManageAdmin extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_manage_admin, container, false);

        // References to buttons
        Button buttonAdminPersonal = view.findViewById(R.id.button_admin_personal);
        Button buttonAdminProductos = view.findViewById(R.id.button_admin_productos);
        Button buttonAdminServicios = view.findViewById(R.id.button_admin_servicios);
        Button buttonAdminMascotas = view.findViewById(R.id.button_admin_mascotas);

        // Set click listeners for buttons
        buttonAdminPersonal.setOnClickListener(v -> startActivity(new Intent(getActivity(), Submenu_AdminPersonal.class)));
        buttonAdminProductos.setOnClickListener(v -> startActivity(new Intent(getActivity(), Submenu_AdminProductos.class)));
        buttonAdminServicios.setOnClickListener(v -> startActivity(new Intent(getActivity(), Submenu_AdminServicios.class)));
        buttonAdminMascotas.setOnClickListener(v -> startActivity(new Intent(getActivity(), Submenu_AdminMascotas.class)));

        return view;
    }
}

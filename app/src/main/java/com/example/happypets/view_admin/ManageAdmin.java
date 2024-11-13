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
import com.example.happypets.submenu_admin.Submenu_AdminPersonal;

public class ManageAdmin extends Fragment {

    private static final String ARG_TOKEN = "token"; // Clave para el token
    private String token; // Variable para almacenar el token

    // Método estático para crear una nueva instancia del fragmento
    public static ManageAdmin newInstance(String token) {
        ManageAdmin fragment = new ManageAdmin();
        Bundle args = new Bundle();
        args.putString(ARG_TOKEN, token); // Almacenar el token en el Bundle
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Obtener el token del Bundle
        if (getArguments() != null) {
            token = getArguments().getString(ARG_TOKEN);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_manage_admin, container, false);

        Button buttonAdminPersonal = view.findViewById(R.id.button_admin_personal);

        buttonAdminPersonal.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Submenu_AdminPersonal.class);
            intent.putExtra(ARG_TOKEN, token); // Pasar el token al submenú
            startActivity(intent);
        });
        return view;
    }
}

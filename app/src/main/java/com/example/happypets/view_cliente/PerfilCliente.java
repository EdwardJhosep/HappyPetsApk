package com.example.happypets.view_cliente;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.happypets.R;

public class PerfilCliente extends Fragment {

    private String dni;
    private String phoneNumber;
    private String nombreCompleto; // Variable para el nombre completo

    // Utilizar el patrón newInstance para pasar parámetros
    public static PerfilCliente newInstance(String dni, String phoneNumber, String nombreCompleto) {
        PerfilCliente fragment = new PerfilCliente();
        Bundle args = new Bundle();
        args.putString("dni", dni);
        args.putString("phoneNumber", phoneNumber);
        args.putString("nombreCompleto", nombreCompleto); // Pasar el nombre completo
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_perfil_cliente, container, false);

        // Obtener los argumentos del Bundle
        if (getArguments() != null) {
            dni = getArguments().getString("dni");
            phoneNumber = getArguments().getString("phoneNumber");
            nombreCompleto = getArguments().getString("nombreCompleto"); // Obtener el nombre completo
        }

        // Mostrar los datos del usuario
        TextView dniTextView = view.findViewById(R.id.dniTextView);
        TextView phoneTextView = view.findViewById(R.id.phoneTextView);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        TextView nombreTextView = view.findViewById(R.id.nombreTextView); // TextView para mostrar el nombre completo

        // Establecer el texto en los TextViews
        dniTextView.setText("DNI: " + dni);
        phoneTextView.setText("Teléfono: " + phoneNumber);
        nombreTextView.setText("Nombre completo: " + nombreCompleto); // Mostrar el nombre completo

        return view;
    }
}
package com.example.happypets.view_cliente;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.happypets.R;

public class PerfilCliente extends Fragment {

    private String dni;
    private String phoneNumber;
    private String nombreCompleto; // Variable para el nombre completo
    private String userId;         // Variable para el ID del usuario

    // Utilizar el patrón newInstance para pasar parámetros
    public static PerfilCliente newInstance(String dni, String phoneNumber, String nombreCompleto, String userId) {
        PerfilCliente fragment = new PerfilCliente();
        Bundle args = new Bundle();
        args.putString("dni", dni);
        args.putString("phoneNumber", phoneNumber);
        args.putString("nombreCompleto", nombreCompleto); // Pasar el nombre completo
        args.putString("userId", userId);                 // Pasar el ID del usuario
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
            userId = getArguments().getString("userId");                 // Obtener el ID del usuario
        }

        // Mostrar los datos del usuario
        TextView dniTextView = view.findViewById(R.id.dniTextView);
        TextView phoneTextView = view.findViewById(R.id.phoneTextView);
        TextView nombreTextView = view.findViewById(R.id.nombreTextView); // TextView para mostrar el nombre completo
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        TextView userIdTextView = view.findViewById(R.id.userIdTextView); // TextView para mostrar el ID del usuario

        // Establecer el texto en los TextViews
        dniTextView.setText("DNI: " + dni);
        phoneTextView.setText("Teléfono: " + phoneNumber);
        nombreTextView.setText("Nombre completo: " + nombreCompleto); // Mostrar el nombre completo
        userIdTextView.setText("ID de Usuario: " + userId);           // Mostrar el ID del usuario

        // Configurar el botón para agregar mascota
        ImageButton addPetButton = view.findViewById(R.id.addPetButton);
        // En el método onCreateView de PerfilCliente, cambia esto
        addPetButton.setOnClickListener(v -> {
            // Mostrar el DialogFragment de agregar mascota
            AgregarMascotaDialogFragment agregarMascotaDialogFragment = AgregarMascotaDialogFragment.newInstance(userId);
            agregarMascotaDialogFragment.show(getChildFragmentManager(), "agregarMascota");
        });
        return view;
    }
}
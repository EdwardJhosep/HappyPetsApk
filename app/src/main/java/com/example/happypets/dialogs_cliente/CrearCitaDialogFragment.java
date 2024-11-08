package com.example.happypets.dialogs_cliente;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.happypets.R;

public class CrearCitaDialogFragment extends DialogFragment {

    private String token;  // Para almacenar el token pasado desde el adapter
    private String userId; // Para almacenar el userId pasado desde el adapter

    // Campos del formulario de cita
    private EditText fechaEditText;
    private EditText horaEditText;
    private TextView userIdTextView; // El TextView donde se mostrará el userId

    public CrearCitaDialogFragment() {
        // Requiere un constructor vacío
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Asegúrate de que el fragmento tiene un estilo de diálogo
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Light_Dialog);
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflar el layout del fragmento
        View view = inflater.inflate(R.layout.dialog_crear_cita_cliente, container, false);

        // Obtener el token y userId pasados como argumento
        if (getArguments() != null) {
            token = getArguments().getString("token");
            userId = getArguments().getString("userId"); // Obtener el userId
        }

        // Inicializar los campos del formulario
        fechaEditText = view.findViewById(R.id.fechaEditText);
        horaEditText = view.findViewById(R.id.horaEditText);
        userIdTextView = view.findViewById(R.id.userIdTextView); // Inicializar el TextView

        // Mostrar el userId en el TextView

        // Botón para reservar la cita
        Button reservarButton = view.findViewById(R.id.reservarButton);
        reservarButton.setOnClickListener(v -> {
            // Obtener los datos de la cita
            String fecha = fechaEditText.getText().toString().trim();
            String hora = horaEditText.getText().toString().trim();

            // Validar los campos
            if (TextUtils.isEmpty(fecha) || TextUtils.isEmpty(hora)) {
                Toast.makeText(getContext(), "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Aquí puedes hacer la lógica para enviar los datos de la cita al servidor
            // Usar el token para la autenticación si es necesario

            // Mostrar un mensaje de éxito o realizar la acción de creación de cita
            Toast.makeText(getContext(), "Cita reservada con éxito", Toast.LENGTH_SHORT).show();
            dismiss(); // Cerrar el diálogo después de la acción
        });

        return view;
    }
}

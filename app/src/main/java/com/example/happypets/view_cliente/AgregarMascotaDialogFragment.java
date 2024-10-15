package com.example.happypets.view_cliente;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.happypets.R;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AgregarMascotaDialogFragment extends DialogFragment {

    private String userId;
    private String token; // Agrega esta línea para almacenar el token
    private ProgressBar progressBar;
    private EditText nombreEditText;
    private EditText edadEditText;
    private EditText especieEditText;
    private EditText razaEditText;
    private EditText sexoEditText;
    private Button agregarButton;

    public static AgregarMascotaDialogFragment newInstance(String userId, String token) {
        AgregarMascotaDialogFragment fragment = new AgregarMascotaDialogFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("token", token); // Pasa el token como argumento
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_agregar_mascota_dialog_fragment, container, false);

        // Obtener los argumentos
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
            token = getArguments().getString("token"); // Obtén el token de los argumentos
        }

        // Inicializar los elementos del formulario
        nombreEditText = view.findViewById(R.id.nombreEditText);
        edadEditText = view.findViewById(R.id.edadEditText);
        especieEditText = view.findViewById(R.id.especieEditText);
        razaEditText = view.findViewById(R.id.razaEditText);
        sexoEditText = view.findViewById(R.id.sexoEditText);
        agregarButton = view.findViewById(R.id.agregarButton);
        progressBar = view.findViewById(R.id.progressBar); // Inicializa el ProgressBar

        agregarButton.setOnClickListener(v -> {
            String nombre = nombreEditText.getText().toString().trim();
            String edad = edadEditText.getText().toString().trim();
            String especie = especieEditText.getText().toString().trim();
            String raza = razaEditText.getText().toString().trim();
            String sexo = sexoEditText.getText().toString().trim();

            // Validar que todos los campos estén completos
            if (nombre.isEmpty() || edad.isEmpty() || especie.isEmpty() || raza.isEmpty() || sexo.isEmpty()) {
                Toast.makeText(getActivity(), "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
                return;
            }

            agregarMascota(nombre, edad, especie, raza, sexo, userId, token); // Pasa el token a la función
        });

        return view;
    }

    private void agregarMascota(String nombre, String edad, String especie, String raza, String sexo, String idUsuario, String token) {
        agregarButton.setEnabled(false); // Deshabilitar el botón
        progressBar.setVisibility(View.VISIBLE); // Mostrar el ProgressBar

        new Thread(() -> {
            try {
                URL url = new URL("https://api-happypetshco-com.preview-domain.com/api/NuevaMascota");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + token); // Agrega el token en el encabezado
                conn.setDoOutput(true);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("nombre", nombre);
                jsonObject.put("edad", edad);
                jsonObject.put("especie", especie);
                jsonObject.put("raza", raza);
                jsonObject.put("sexo", sexo);
                jsonObject.put("id_usuario", idUsuario); // Pasar el ID del usuario

                OutputStream os = conn.getOutputStream();
                os.write(jsonObject.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_CREATED) {
                    // Mascota agregada exitosamente
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getActivity(), "Mascota registrada exitosamente", Toast.LENGTH_SHORT).show();
                        limpiarFormulario(); // Limpiar campos
                        dismiss(); // Cerrar el diálogo
                    });
                } else {
                    // Manejar errores
                    requireActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Error al registrar la mascota", Toast.LENGTH_SHORT).show());
                }
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } finally {
                // Restablecer la UI
                requireActivity().runOnUiThread(() -> {
                    agregarButton.setEnabled(true); // Habilitar el botón
                    progressBar.setVisibility(View.GONE); // Ocultar el ProgressBar
                });
            }
        }).start();
    }

    private void limpiarFormulario() {
        nombreEditText.setText("");
        edadEditText.setText("");
        especieEditText.setText("");
        razaEditText.setText("");
        sexoEditText.setText("");
    }
}

package com.example.happypets;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends DialogFragment {

    public MainActivity() {
        // Constructor público requerido
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main, container, false);

        // Referencias a los campos del formulario
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText etDni = view.findViewById(R.id.etDni);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText etTelefono = view.findViewById(R.id.etTelefono);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText etPassword = view.findViewById(R.id.etPassword);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button btnRegister = view.findViewById(R.id.btnRegister);
        Button btnClose = view.findViewById(R.id.btnClose);

        // Acción al pulsar el botón de registro
        btnRegister.setOnClickListener(v -> {
            String dni = etDni.getText().toString().trim();
            String telefono = etTelefono.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // Validaciones
            if (!areFieldsFilled(dni, telefono, password, confirmPassword)) {
                Toast.makeText(getContext(), "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            } else if (!isDniValid(dni)) {
                Toast.makeText(getContext(), "El DNI debe contener solo números y tener 8 dígitos", Toast.LENGTH_SHORT).show();
            } else if (!isPhoneValid(telefono)) {
                Toast.makeText(getContext(), "El número de teléfono debe contener solo números y tener 9 dígitos", Toast.LENGTH_SHORT).show();
            } else if (!isPasswordSecure(password)) {
                Toast.makeText(getContext(), "La contraseña debe contener solo letras y números", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            } else {
                // Si las validaciones son exitosas, se registra al usuario
                registerUser(dni, telefono, password);
            }
        });

        // Acción al pulsar el botón de cerrar
        btnClose.setOnClickListener(v -> dismiss());

        return view;
    }

    // Método para registrar al usuario
    private void registerUser(String dni, String telefono, String password) {
        String url = "https://api-happypetshco-com.preview-domain.com/api/Registro";

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("dni", dni);
            jsonObject.put("telefono", telefono);
            jsonObject.put("password", password);
            jsonObject.put("password_confirmation", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue queue = Volley.newRequestQueue(getContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                response -> {
                    try {
                        String mensaje = response.getString("mensaje");
                        String nombres = response.getString("nombres");
                        String telefonoRespuesta = response.getString("telefono");

                        Toast.makeText(getContext(), "Registro exitoso: " + mensaje, Toast.LENGTH_SHORT).show();
                        dismiss();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> handleError(error)
        );

        queue.add(jsonObjectRequest);
    }

    // Método para manejar los errores del servidor
    private void handleError(VolleyError error) {
        String errorMessage = "Error en el registro: " + error.getMessage();
        if (error.networkResponse != null && error.networkResponse.data != null) {
            String responseData = new String(error.networkResponse.data);
            errorMessage += ": " + responseData; // Muestra la respuesta del servidor
        }
        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
    }

    // Método para verificar que todos los campos están llenos
    private boolean areFieldsFilled(String dni, String telefono, String password, String confirmPassword) {
        return !dni.isEmpty() && !telefono.isEmpty() && !password.isEmpty() && !confirmPassword.isEmpty();
    }

    // Método para validar el DNI
    private boolean isDniValid(String dni) {
        return dni.matches("\\d{8}"); // El DNI debe tener exactamente 8 dígitos
    }

    // Método para validar el teléfono
    private boolean isPhoneValid(String telefono) {
        return telefono.matches("\\d{9}"); // El teléfono debe tener exactamente 9 dígitos
    }

    // Método para verificar la seguridad de la contraseña
    private boolean isPasswordSecure(String password) {
        return password.matches("^[a-zA-Z0-9]+$"); // Solo permite letras y números
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }
}
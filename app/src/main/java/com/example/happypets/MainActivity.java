package com.example.happypets;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.example.happypets.view_cliente.MenuCliente;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class MainActivity extends DialogFragment {

    private TextView tvErrorMessage;
    private ProgressBar progressBar; // Declarar ProgressBar

    public MainActivity() {
        // Constructor vacío
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main, container, false);

        EditText etDni = view.findViewById(R.id.etDni);
        EditText etTelefono = view.findViewById(R.id.etTelefono);
        EditText etPassword = view.findViewById(R.id.etPassword);
        EditText etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        Button btnRegister = view.findViewById(R.id.btnRegister);
        Button btnClose = view.findViewById(R.id.btnClose);
        tvErrorMessage = view.findViewById(R.id.tvErrorMessage);
        progressBar = view.findViewById(R.id.progressBar); // Inicializar ProgressBar

        btnRegister.setOnClickListener(v -> {
            String dni = etDni.getText().toString().trim();
            String telefono = etTelefono.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // Limpiar mensajes de error anteriores
            tvErrorMessage.setText("");

            // Validaciones
            if (TextUtils.isEmpty(dni) || TextUtils.isEmpty(telefono) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                showError(tvErrorMessage, "Todos los campos son obligatorios");
            } else if (dni.length() != 8) {
                showError(tvErrorMessage, "El DNI debe tener 8 dígitos");
            } else if (telefono.length() != 9) {
                showError(tvErrorMessage, "El número de teléfono debe tener 9 dígitos");
            } else if (!isPasswordSecure(password)) {
                showError(tvErrorMessage, "La contraseña debe contener solo letras y números");
            } else if (!password.equals(confirmPassword)) {
                showError(tvErrorMessage, "Las contraseñas no coinciden");
            } else {
                registerUser(dni, telefono, password);
            }
        });

        btnClose.setOnClickListener(v -> dismiss());

        return view;
    }

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
            showError(tvErrorMessage, "Error al crear el objeto JSON");
            return; // Salir si hubo un error
        }

        progressBar.setVisibility(View.VISIBLE); // Mostrar ProgressBar
        RequestQueue queue = Volley.newRequestQueue(getContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                response -> {
                    progressBar.setVisibility(View.GONE); // Ocultar ProgressBar
                    handleResponse(response);
                },
                error -> {
                    progressBar.setVisibility(View.GONE); // Ocultar ProgressBar
                    handleError(error);
                }
        );

        queue.add(jsonObjectRequest);
    }

    private void handleResponse(JSONObject response) {
        try {
            if (response.has("error")) {
                // Manejo de errores de validación
                JSONObject errorObject = response.getJSONObject("error");
                StringBuilder errorMessages = new StringBuilder("Errores de validación:\n");
                for (Iterator<String> it = errorObject.keys(); it.hasNext(); ) {
                    String key = it.next();
                    errorMessages.append(key).append(": ")
                            .append(errorObject.getJSONArray(key).join(", ")).append("\n");
                }
                Toast.makeText(getContext(), errorMessages.toString(), Toast.LENGTH_LONG).show();
            } else {
                String mensaje = response.getString("mensaje");
                Toast.makeText(getContext(), "Registro exitoso: " + mensaje, Toast.LENGTH_SHORT).show();

                // Redirigir a MenuCliente después del registro exitoso
                Intent intent = new Intent(getActivity(), MenuCliente.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                // Cerrar el diálogo
                dismiss();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            showError(tvErrorMessage, "Error en la respuesta del servidor");
        }
    }

    private void handleError(VolleyError error) {
        String errorMessage = "Error en el registro: " + error.getMessage();
        if (error.networkResponse != null && error.networkResponse.data != null) {
            String responseData = new String(error.networkResponse.data);
            errorMessage += ": " + responseData; // Muestra la respuesta del servidor
        }
        showError(tvErrorMessage, errorMessage);
    }

    // Método para mostrar mensajes de error
    private void showError(TextView tvErrorMessage, String message) {
        tvErrorMessage.setText(message);
        tvErrorMessage.setVisibility(View.VISIBLE); // Asegúrate de que el TextView sea visible
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

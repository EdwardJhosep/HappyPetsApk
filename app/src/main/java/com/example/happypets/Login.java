package com.example.happypets;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.happypets.view_admin.MenuAdmin;
import com.example.happypets.view_cliente.MenuCliente;

import org.json.JSONException;
import org.json.JSONObject;

public class Login extends AppCompatActivity {

    private ProgressBar progressBar; // Declara el ProgressBar
    private EditText etUsername;
    private EditText etPassword;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializa el ProgressBar y EditTexts
        progressBar = findViewById(R.id.progressBar);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        progressBar.setVisibility(View.GONE); // Asegúrate de ocultar el ProgressBar al inicio

        // Asignar el método al botón de registro
        Button btnRegister = findViewById(R.id.registro);
        btnRegister.setOnClickListener(this::onRegisterButtonClick);

        // Asignar el método al botón de inicio de sesión
        Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this::onLoginButtonClick);
    }

    // Método para manejar el clic en el botón de inicio de sesión
    public void onLoginButtonClick(View view) {
        String dni = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (dni.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
        } else {
            authenticateUser(dni, password);
        }
    }

    private void authenticateUser(String dni, String password) {
        String url = "https://api-happypetshco-com.preview-domain.com/api/Autenticar";

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("dni", dni);
            jsonObject.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al crear la solicitud.", Toast.LENGTH_SHORT).show();
            return; // Salir si hay un error al crear la solicitud
        }

        RequestQueue queue = Volley.newRequestQueue(this);

        // Mostrar el ProgressBar antes de la solicitud
        progressBar.setVisibility(View.VISIBLE);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                response -> {
                    // Ocultar el ProgressBar al recibir la respuesta
                    progressBar.setVisibility(View.GONE);
                    handleResponse(response);
                },
                error -> {
                    // Ocultar el ProgressBar en caso de error
                    progressBar.setVisibility(View.GONE);
                    handleError(error);
                }
        );

        queue.add(jsonObjectRequest);
    }

    private void handleResponse(JSONObject response) {
        try {
            if (response.has("error")) {
                String errorMessage = response.getString("error");
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            } else {
                String mensaje = response.getString("mensaje");
                String token = response.getString("token");
                getUserData(token);
                Toast.makeText(this, "Inicio de sesión exitoso.", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error en la respuesta del servidor.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleError(VolleyError error) {
        if (error.networkResponse == null) {
            Toast.makeText(this, "No hay conexión a Internet. Verifique su conexión.", Toast.LENGTH_SHORT).show();
        } else {
            switch (error.networkResponse.statusCode) {
                case 400:
                    Toast.makeText(this, "Error en la solicitud. Datos no válidos.", Toast.LENGTH_SHORT).show();
                    break;
                case 401:
                    Toast.makeText(this, "Credenciales incorrectas. Por favor, intente de nuevo.", Toast.LENGTH_SHORT).show();
                    break;
                case 404:
                    Toast.makeText(this, "Usuario no encontrado. Verifique su DNI.", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(this, "Error en la autenticación. Intente más tarde.", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    private void getUserData(String token) {
        String url = "https://api-happypetshco-com.preview-domain.com/api/DatosUsuario";

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    progressBar.setVisibility(View.GONE); // Ocultar el ProgressBar al recibir la respuesta
                    handleUserDataResponse(response, token);
                },
                error -> {
                    progressBar.setVisibility(View.GONE); // Ocultar el ProgressBar en caso de error
                    Toast.makeText(this, "Error al obtener datos del usuario.", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.HashMap<String, String> headers = new java.util.HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }

    private void handleUserDataResponse(JSONObject response, String token) {
        try {
            if (response.has("usuarios")) {
                JSONObject user = response.getJSONObject("usuarios");
                String permisos = user.has("permisos") ? user.getString("permisos") : "No tiene permisos";
                Intent intent = "Administrador".equals(permisos) ? new Intent(this, MenuAdmin.class) : new Intent(this, MenuCliente.class);
                intent.putExtra("token", token);
                startActivity(intent);
            } else {
                Toast.makeText(this, "No se encontraron datos de usuario.", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al procesar los datos del usuario.", Toast.LENGTH_SHORT).show();
        }
    }

    public void onRegisterButtonClick(View view) {
        MainActivity registerDialogFragment = new MainActivity();
        registerDialogFragment.show(getSupportFragmentManager(), "registerDialog");
    }
}
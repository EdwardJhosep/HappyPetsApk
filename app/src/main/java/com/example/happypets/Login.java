package com.example.happypets;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class Login extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private ProgressBar progressBar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Verificar si el usuario ya ha iniciado sesión
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String token = sharedPreferences.getString("token", null);

        if (isLoggedIn && token != null) {
            getUserData(token);
            return;
        }

        setContentView(R.layout.activity_login);

        // Inicializa los EditTexts y el ProgressBar
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        progressBar = findViewById(R.id.progressBar);

        // Configura el Listener para el Padding en la vista principal
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Asigna el método al botón de registro
        Button btnRegister = findViewById(R.id.registro);
        btnRegister.setOnClickListener(this::onRegisterButtonClick);

        // Asigna el método al botón de inicio de sesión
        Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this::onLoginButtonClick);
    }

    public void onRegisterButtonClick(View view) {
        MainActivity registerDialogFragment = new MainActivity();
        registerDialogFragment.setOnRegisterCompleteListener(new MainActivity.OnRegisterCompleteListener() {
            @Override
            public void onRegisterComplete(String dni, String password) {
                // Hacer login automáticamente con los datos del registro
                authenticateUser(dni, password);
            }
        });
        registerDialogFragment.show(getSupportFragmentManager(), "registerDialog");
    }

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
        String url = "https://api.happypetshco.com/api/Autenticar";

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("dni", dni);
            jsonObject.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al crear la solicitud.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                response -> {
                    handleResponse(response);
                    progressBar.setVisibility(View.GONE);
                },
                error -> {
                    handleError(error);
                    progressBar.setVisibility(View.GONE);
                }
        );

        queue.add(jsonObjectRequest);
    }

    private void handleResponse(JSONObject response) {
        try {
            Log.d("Response", response.toString()); // Log de la respuesta

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
            showAlert("No hay conexión a Internet. Verifique su conexión.");
        } else {
            switch (error.networkResponse.statusCode) {
                case 400:
                    showAlert("Error en la solicitud. Datos no válidos.");
                    break;
                case 401:
                    showAlert("Credenciales incorrectas. Por favor, intente de nuevo.");
                    break;
                case 404:
                    showAlert("Usuario no encontrado. Verifique su DNI.");
                    break;
                default:
                    showAlert("Error en la autenticación. Intente más tarde.");
                    break;
            }
        }
    }

    private void getUserData(String token) {
        String url = "https://api.happypetshco.com/api/Datos";

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    handleUserDataResponse(response, token);
                },
                error -> {
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
            Log.d("UserDataResponse", response.toString());
            if (response.has("usuarios")) {
                JSONObject user = response.getJSONObject("usuarios");
                Log.d("UserData", user.toString());

                // Iterar sobre las claves del objeto 'user'
                for (Iterator<String> it = user.keys(); it.hasNext(); ) {
                    String key = it.next();
                    Log.d("UserDataKey", key + ": " + user.get(key));
                }

                if (user.has("permisos")) {
                    JSONArray permisosArray = user.getJSONArray("permisos");
                    String permisos = permisosArray.length() > 0 ? permisosArray.getString(0) : "No tiene permisos";

                    if ("Cliente".equals(permisos)) {
                        saveLoginState(true, token);
                        Intent intent = new Intent(this, MenuCliente.class);
                        intent.putExtra("token", token);
                        startActivity(intent);
                        finish();
                    } else if ("Administrador".equals(permisos)) {
                        Intent intent = new Intent(this, MenuAdmin.class);
                        intent.putExtra("token", token);
                        startActivity(intent);
                        finish();
                    } else {
                        showAlert("Permisos no reconocidos: " + permisos);
                    }
                } else {
                    showAlert("No se encontraron permisos para el usuario.");
                    Log.d("PermissionsCheck", "El objeto 'usuarios' no contiene el campo 'permiso'.");
                }
            } else {
                showAlert("No se encontraron datos de usuario.");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            showAlert("Error al procesar los datos del usuario: " + e.getMessage());
        }
    }
    private void showAlert(String message) {
        new AlertDialog.Builder(this)
                .setTitle("¡Error!")
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert) // Icono de advertencia
                .setPositiveButton("OK", null)
                .create()
                .show();
    }
    private void saveLoginState(boolean isLoggedIn, String token) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", isLoggedIn);
        editor.putString("token", token);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}

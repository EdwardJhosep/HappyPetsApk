package com.example.happypets;

import android.content.Intent;
import android.content.SharedPreferences;
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

    private EditText etUsername;
    private EditText etPassword;
    private ProgressBar progressBar; // Agrega el ProgressBar aquí

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Verificar si el usuario ya ha iniciado sesión
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String token = sharedPreferences.getString("token", null); // Obtener el token guardado

        if (isLoggedIn && token != null) {
            // Aquí debes agregar la lógica para redirigir al usuario según sus permisos
            getUserData(token); // Obtén los datos del usuario con el token guardado
            return; // No continuar con la creación de la vista
        }

        setContentView(R.layout.activity_login);

        // Inicializa los EditTexts y el ProgressBar
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        progressBar = findViewById(R.id.progressBar); // Inicializa el ProgressBar

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

    private void onRegisterButtonClick(View view) {
        // Aquí puedes iniciar una nueva actividad para el registro
        Intent intent = new Intent(this, MainActivity.class); // Cambia MainActivity por el nombre de tu actividad de registro
        startActivity(intent);
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

        progressBar.setVisibility(View.VISIBLE); // Muestra el ProgressBar
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                response -> {
                    handleResponse(response);
                    progressBar.setVisibility(View.GONE); // Oculta el ProgressBar
                },
                error -> {
                    handleError(error);
                    progressBar.setVisibility(View.GONE); // Oculta el ProgressBar
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
                saveLoginState(true, token); // Guarda el estado de inicio de sesión
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
            if (response.has("usuarios")) {
                JSONObject user = response.getJSONObject("usuarios");
                String permisos = user.has("permisos") ? user.getString("permisos") : "No tiene permisos";

                // Guardar el estado de inicio de sesión y el token para usuarios no administradores
                if (!"Administrador".equals(permisos)) {
                    saveLoginState(true, token);
                }

                Intent intent = "Administrador".equals(permisos) ? new Intent(this, MenuAdmin.class) : new Intent(this, MenuCliente.class);
                intent.putExtra("token", token); // Envío del token aquí
                startActivity(intent);
                finish(); // Finaliza la actividad de inicio de sesión para que no se pueda volver a ella
            } else {
                Toast.makeText(this, "No se encontraron datos de usuario.", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al procesar los datos del usuario.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveLoginState(boolean isLoggedIn, String token) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", isLoggedIn);
        editor.putString("token", token); // Guardar el token
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}

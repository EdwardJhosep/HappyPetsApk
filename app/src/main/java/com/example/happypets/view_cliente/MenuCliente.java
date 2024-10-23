package com.example.happypets.view_cliente;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.happypets.Login;
import com.example.happypets.R;
import com.example.happypets.adapters_cliente.ChatFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

public class MenuCliente extends AppCompatActivity {

    // Variables para almacenar los datos del cliente
    private String dni;
    private String phoneNumber;
    private String nombres;
    private String token; // Token de inicio de sesión o registro
    private String userId; // ID del usuario

    // Listener para manejar la selección de los items del BottomNavigationView
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    // Manejo de selección de fragmentos usando if-else
                    if (item.getItemId() == R.id.navigation_product) {
                        selectedFragment = ProductoCliente.newInstance(userId, token); // Pasar userId y token
                    } else if (item.getItemId() == R.id.navigation_appointments) {
                        selectedFragment = CitasCliente.newInstance(userId, token); // Pasar userId y token
                    } else if (item.getItemId() == R.id.navigation_profile) {
                        // Pasar dni, phoneNumber, nombres, userId y token al fragmento
                        selectedFragment = PerfilCliente.newInstance(dni, phoneNumber, nombres, userId, token);
                    } else if (item.getItemId() == R.id.navigation_ubication) {
                        selectedFragment = new Ubication();
                    }else if (item.getItemId() == R.id.navigation_logout) {
                        logout();
                    }


                    if (selectedFragment != null) {
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.fragment_container, selectedFragment);
                        transaction.commit();
                    }

                    return true;
                }
            };
    private void logout() {
        // Crea un Snackbar
        Snackbar snackbar = Snackbar.make(findViewById(R.id.main), "¿Está seguro de que desea cerrar sesión?", Snackbar.LENGTH_LONG);

        // Cambia el color de fondo
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(getResources().getColor(R.color.red_color)); // Color rojo

        // Cambia el texto del Snackbar
        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(getResources().getColor(R.color.white)); // Color del texto
        textView.setTextSize(16); // Tamaño del texto
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD); // Texto en negrita

        // Agrega un botón de acción
        snackbar.setAction("Cerrar sesión", view -> {
            // Limpia los datos de la sesión
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear(); // Borra todos los datos guardados
            editor.apply();

            // Redirige al usuario a la actividad de inicio de sesión
            Intent intent = new Intent(MenuCliente.this, Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // Cierra la actividad actual
        });

        // Cambia el color del texto del botón de acción
        snackbar.setActionTextColor(getResources().getColor(R.color.white)); // Color del botón

        // Muestra el Snackbar
        snackbar.show();
    }



    private void getUserData() {
        String url = "https://api-happypetshco-com.preview-domain.com/api/Datos";

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // Asegúrate de que "usuarios" existe en la respuesta
                        if (response.has("usuarios")) {
                            JSONObject user = response.getJSONObject("usuarios");
                            // Obtener los datos del usuario
                            dni = user.getString("dni");
                            nombres = user.getString("nombres");
                            phoneNumber = user.getString("telefono");
                            userId = user.getString("id"); // Obtener el ID del usuario

                            // Obtener permisos
                            String permisos = user.has("permisos") ? user.getString("permisos") : "Sin permisos";

                            // Llamar a loadInitialFragment después de obtener los datos
                            loadInitialFragment();
                        } else {
                            Toast.makeText(this, "No se encontraron datos de usuario.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al procesar los datos del usuario", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    String errorMessage = "Error al obtener datos del usuario: " + error.getMessage();
                    if (error.networkResponse != null) {
                        errorMessage += ", Código de error: " + error.networkResponse.statusCode;
                        if (error.networkResponse.data != null) {
                            String responseData = new String(error.networkResponse.data);
                            errorMessage += ", Respuesta: " + responseData;
                        }
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    Log.e("MenuCliente", errorMessage); // Imprimir el error en el log
                }) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.HashMap<String, String> headers = new java.util.HashMap<>();
                headers.put("Authorization", "Bearer " + token); // Usar el token correctamente
                return headers;
            }
        };

        // Agregar la solicitud a la cola
        queue.add(jsonObjectRequest);
    }

    private void loadInitialFragment() {
        if (userId != null) {
            Fragment initialFragment = ProductoCliente.newInstance(userId, token);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, initialFragment)
                    .commit();
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_cliente);

        // Obtener los datos pasados por Intent
        dni = getIntent().getStringExtra("dni");
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        nombres = getIntent().getStringExtra("nombres");
        token = getIntent().getStringExtra("token"); // Obtener el token si es necesario

        // Configura los insets para evitar que la UI se superponga con las barras de sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializa el BottomNavigationView
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        FloatingActionButton fabChat = findViewById(R.id.fab_chat);
        fabChat.setOnClickListener(view -> {
            // Crear una instancia de ChatDialogFragment
            ChatFragment chatDialogFragment = new ChatFragment();
            chatDialogFragment.show(getSupportFragmentManager(), "chatDialog");
        });



        // Llamar a getUserData() para obtener los datos del usuario
        getUserData();
    }
}

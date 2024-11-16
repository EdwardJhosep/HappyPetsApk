package com.example.happypets.perfilview;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.happypets.R;

import org.json.JSONException;
import org.json.JSONObject;

public class DatosPersonalesActivity extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_datos_personales);

        // Recuperar el token y userId desde el Intent
        String token = getIntent().getStringExtra("token");
        String userId = getIntent().getStringExtra("userId");

        // Verificar que el token y userId no sean nulos
        if (token != null && userId != null) {
            // Realizar la solicitud de datos del usuario
            getUserData(token);
        } else {
            Toast.makeText(this, "Token o UserId no disponibles", Toast.LENGTH_SHORT).show();
        }

        // Manejo de insets para el diseño edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Button buttonEdit = findViewById(R.id.buttonEdit); // Asegúrate de tener este botón en tu layout
        buttonEdit.setOnClickListener(v -> {
            // Crear un nuevo objeto Bundle para enviar los datos al fragmento
            Bundle bundle = new Bundle();
            bundle.putString("userId", userId);  // Enviar el userId
            bundle.putString("token", token);    // Enviar el token

            // Crear el fragmento y establecer los argumentos
            EditUserDataDialogFragment dialogFragment = new EditUserDataDialogFragment();
            dialogFragment.setArguments(bundle);  // Establecer los argumentos en el fragmento

            // Mostrar el fragmento
            dialogFragment.show(getSupportFragmentManager(), "editUserDataDialog");
        });
    }

    private void getUserData(String token) {
        String url = "https://api.happypetshco.com/api/Datos";

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    handleUserDataResponse(response);
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

    private void handleUserDataResponse(JSONObject response) {
        try {
            // Obtener los datos dentro del objeto 'usuarios'
            JSONObject usuario = response.getJSONObject("usuarios");

            // Extraer los campos del JSON
            int id = usuario.getInt("id");
            String dni = usuario.getString("dni");
            String nombres = usuario.getString("nombres");
            String apellidos = usuario.getString("apellidos");
            String telefono = usuario.getString("telefono");
            String ubicacion = usuario.isNull("ubicacion") ? "No disponible" : usuario.getString("ubicacion");
            String especialidad = usuario.getString("especialidad");

            // Si prefieres mostrar los datos en TextViews, los puedes actualizar así:
            TextView textViewId = findViewById(R.id.textViewId);
            TextView textViewDni = findViewById(R.id.textViewDni);
            TextView textViewNombres = findViewById(R.id.textViewNombres);
            TextView textViewApellidos = findViewById(R.id.textViewApellidos);
            TextView textViewTelefono = findViewById(R.id.textViewTelefono);
            TextView textViewUbicacion = findViewById(R.id.textViewUbicacion);
            TextView textViewEspecialidad = findViewById(R.id.textViewEspecialidad);

            // Establecer los valores en los TextViews
            textViewId.setText("ID: " + id);
            textViewDni.setText("DNI: " + dni);
            textViewNombres.setText("Nombres: " + nombres);
            textViewApellidos.setText("Apellidos: " + apellidos);
            textViewTelefono.setText("Teléfono: " + telefono);
            textViewUbicacion.setText("Ubicación: " + ubicacion);
            textViewEspecialidad.setText("Especialidad: " + especialidad);

            // Llamar al fragmento de edición pasándole los datos del usuario
            Button buttonEdit = findViewById(R.id.buttonEdit); // Asegúrate de tener este botón en tu layout
            buttonEdit.setOnClickListener(v -> {
                // Crear un nuevo objeto Bundle para enviar los datos al fragmento
                Bundle bundle = new Bundle();
                bundle.putString("userId", String.valueOf(id));  // Enviar el userId
                bundle.putString("token", getIntent().getStringExtra("token"));  // Enviar el token
                bundle.putString("telefono", telefono);  // Enviar el teléfono
                bundle.putString("ubicacion", ubicacion); // Enviar la ubicación

                // Crear el fragmento y establecer los argumentos
                EditUserDataDialogFragment dialogFragment = new EditUserDataDialogFragment();
                dialogFragment.setArguments(bundle);  // Establecer los argumentos en el fragmento

                // Mostrar el fragmento
                dialogFragment.show(getSupportFragmentManager(), "editUserDataDialog");
            });

        } catch (JSONException e) {
            Toast.makeText(this, "Error al procesar los datos del usuario.", Toast.LENGTH_SHORT).show();
        }
    }
}

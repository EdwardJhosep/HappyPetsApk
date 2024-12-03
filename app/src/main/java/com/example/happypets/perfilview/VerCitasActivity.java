package com.example.happypets.perfilview;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.happypets.R;
import com.example.happypets.adapters_cliente.HistorialCitasAdapter;
import com.example.happypets.adapters_cliente.PetsAdapter;
import com.example.happypets.models.Mascota;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VerCitasActivity extends AppCompatActivity {

    private ArrayList<Mascota> petsList;
    private PetsAdapter petsAdapter;
    private RecyclerView recyclerView;
    private String token;  // Variable de clase para el token
    private String userId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_citas);

        petsList = new ArrayList<>();
        recyclerView = findViewById(R.id.petsListView);

        token = getIntent().getStringExtra("token");
        userId = getIntent().getStringExtra("userId");

        if (token == null || userId == null) {
            Toast.makeText(this, "Token o UserId no proporcionados", Toast.LENGTH_SHORT).show();
            return;
        }

        petsAdapter = new PetsAdapter(this, petsList, userId, token);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(petsAdapter);

        obtenerHistorialMascotas();
    }

    private void obtenerHistorialMascotas() {
        String url = "https://api.happypetshco.com/api/MascotasUsuario=" + userId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        JSONArray mascotasArray = response.getJSONArray("mascotas");
                        petsList.clear();
                        if (mascotasArray.length() > 0) {
                            for (int i = 0; i < mascotasArray.length(); i++) {
                                JSONObject mascota = mascotasArray.getJSONObject(i);
                                petsList.add(new Mascota(
                                        mascota.getString("id"),
                                        mascota.getString("nombre"),
                                        mascota.getString("edad"),
                                        mascota.getString("especie"),
                                        mascota.getString("raza"),
                                        mascota.getString("sexo"),
                                        mascota.getString("estado"),
                                        mascota.getString("imagen")
                                ));
                            }
                            petsAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(VerCitasActivity.this, "No se encontraron mascotas", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("API_RESPONSE_ERROR", "Error al procesar JSON: " + e.getMessage());
                        Toast.makeText(VerCitasActivity.this, "Error al obtener el historial de mascotas", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("API_ERROR", "Error de red: " + error.getMessage());
                    if (error.networkResponse != null) {
                        Log.e("API_ERROR", "Código de estado: " + error.networkResponse.statusCode);
                        Log.e("API_ERROR", "Contenido: " + new String(error.networkResponse.data));
                    }
                    Toast.makeText(VerCitasActivity.this, "Error al obtener el historial de mascotas", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    public void mostrarDatosMascota(String userId) {
        String url = "https://api.happypetshco.com/api/HistorialMascota=" + userId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        // Extract pet data
                        JSONObject mascotaData = response.getJSONArray("mascotas").getJSONObject(0); // Solo tomo la primera mascota

                        String nombreMascota = mascotaData.getString("nombre");
                        String edad = mascotaData.getString("edad");
                        String especie = mascotaData.getString("especie");
                        String raza = mascotaData.getString("raza");
                        String sexo = mascotaData.getString("sexo");  // Campo sexo
                        String estado = mascotaData.getString("estado");  // Campo estado

                        // Extract owner data
                        JSONObject usuarioData = mascotaData.getJSONObject("usuario");
                        String nombreDuenio = usuarioData.getString("nombres") + " " + usuarioData.getString("apellidos");
                        String telefonoDuenio = usuarioData.getString("telefono");

                        // Update UI for pet information
                        TextView mascotaNombre = findViewById(R.id.mascotaNombre);
                        TextView mascotaEdad = findViewById(R.id.mascotaEdad);
                        TextView mascotaEspecie = findViewById(R.id.mascotaEspecie);
                        TextView mascotaRaza = findViewById(R.id.mascotaRaza);
                        TextView mascotaSexo = findViewById(R.id.mascotaSexo);  // TextView para sexo
                        TextView mascotaEstado = findViewById(R.id.mascotaEstado);  // TextView para estado

                        mascotaNombre.setText(nombreMascota);
                        mascotaEdad.setText(edad);
                        mascotaEspecie.setText(especie);
                        mascotaRaza.setText(raza);
                        mascotaSexo.setText(sexo);  // Mostrar el sexo
                        mascotaEstado.setText(estado);  // Mostrar el estado

                        // Update UI for owner information
                        TextView duenioNombre = findViewById(R.id.duenioNombre);
                        TextView duenioTelefono = findViewById(R.id.duenioTelefono);

                        duenioNombre.setText(nombreDuenio);
                        duenioTelefono.setText(telefonoDuenio);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("API_RESPONSE_ERROR", "Error al procesar JSON: " + e.getMessage());
                        Toast.makeText(VerCitasActivity.this, "Error al obtener los detalles de la mascota", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("API_ERROR", "Error de red: " + error.getMessage());
                    if (error.networkResponse != null) {
                        Log.e("API_ERROR", "Código de estado: " + error.networkResponse.statusCode);
                        Log.e("API_ERROR", "Contenido: " + new String(error.networkResponse.data));
                    }
                    Toast.makeText(VerCitasActivity.this, "Error al obtener detalles de la mascota", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
    public void mostrarHistorialCompleto(String userId) {
        String url = "https://api.happypetshco.com/api/HistorialMascota=" + userId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        // Extraer los datos de las mascotas
                        JSONArray mascotasArray = response.getJSONArray("mascotas");
                        List<JSONObject> citasList = new ArrayList<>();

                        for (int i = 0; i < mascotasArray.length(); i++) {
                            JSONObject mascotaData = mascotasArray.getJSONObject(i);

                            // Recorrer todas las citas de la mascota
                            JSONArray citasArray = mascotaData.getJSONArray("citas");
                            for (int j = 0; j < citasArray.length(); j++) {
                                JSONObject citaData = citasArray.getJSONObject(j);
                                citasList.add(citaData);  // Agregar cada cita a la lista
                            }
                        }

                        // Formato de fecha para parsear
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

                        // Ordenar las citas por fecha (más reciente primero)
                        Collections.sort(citasList, new Comparator<JSONObject>() {
                            @Override
                            public int compare(JSONObject o1, JSONObject o2) {
                                try {
                                    // Extraer las fechas de las citas
                                    String fecha1 = o1.getString("fecha");
                                    String fecha2 = o2.getString("fecha");

                                    // Parsear las fechas a objetos Date
                                    Date date1 = dateFormat.parse(fecha1);
                                    Date date2 = dateFormat.parse(fecha2);

                                    // Comparar las fechas (más reciente primero)
                                    return date2.compareTo(date1);  // Orden descendente (más reciente primero)
                                } catch (JSONException | ParseException e) {
                                    e.printStackTrace();
                                }
                                return 0;  // Si ocurre un error, no cambia el orden
                            }
                        });

                        // Crear el adaptador y asociarlo al ListView
                        HistorialCitasAdapter adapter = new HistorialCitasAdapter(VerCitasActivity.this, citasList);
                        ListView listView = findViewById(R.id.listViewCitas);
                        listView.setAdapter(adapter);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("API_RESPONSE_ERROR", "Error al procesar JSON: " + e.getMessage());
                        Toast.makeText(VerCitasActivity.this, "Error al obtener los detalles de la mascota", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("API_ERROR", "Error de red: " + error.getMessage());
                    if (error.networkResponse != null) {
                        Log.e("API_ERROR", "Código de estado: " + error.networkResponse.statusCode);
                        Log.e("API_ERROR", "Contenido: " + new String(error.networkResponse.data));
                    }
                    Toast.makeText(VerCitasActivity.this, "Error al obtener detalles de la mascota", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
}

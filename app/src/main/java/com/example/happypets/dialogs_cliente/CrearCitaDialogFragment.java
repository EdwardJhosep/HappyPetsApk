package com.example.happypets.dialogs_cliente;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.happypets.R;
import com.example.happypets.models.Mascota;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CrearCitaDialogFragment extends DialogFragment {

    private String token;
    private String userId;
    private String servicioId;
    private EditText fechaEditText;
    private EditText horaEditText;
    private TextView userIdTextView;
    private Spinner mascotaSpinner;
    private Spinner metodoPagoSpinner; // Spinner for payment methods
    private ArrayList<Mascota> petsList = new ArrayList<>();

    public CrearCitaDialogFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Light_Dialog);
    }

    @Override
    public void onStart() {
        super.onStart();
        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setAttributes(params);

        // Apply rounded background
        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.card_background);
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_crear_cita_cliente, container, false);

        // Retrieve arguments passed to the dialog
        if (getArguments() != null) {
            token = getArguments().getString("token");
            userId = getArguments().getString("userId");
            servicioId = getArguments().getString("servicioId");
        }

        fechaEditText = view.findViewById(R.id.fechaEditText);
        horaEditText = view.findViewById(R.id.horaEditText);
        userIdTextView = view.findViewById(R.id.userIdTextView);
        mascotaSpinner = view.findViewById(R.id.MascotaSpinner);
        metodoPagoSpinner = view.findViewById(R.id.metodoPagoSpinner); // Bind the Spinner
        obtenerHistorialMascotas(userId);

        Button reservarButton = view.findViewById(R.id.reservarButton);
        reservarButton.setOnClickListener(v -> {
            String fecha = fechaEditText.getText().toString().trim();
            String hora = horaEditText.getText().toString().trim();

            // Check if fields are empty
            if (TextUtils.isEmpty(fecha) || TextUtils.isEmpty(hora) || mascotaSpinner.getSelectedItem() == null) {
                Toast.makeText(getContext(), "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get selected pet
            Mascota selectedMascota = (Mascota) mascotaSpinner.getSelectedItem();

            // Prepare data for the appointment
            String idMascota = selectedMascota.getId();
            String estado = "pendiente";
            String observaciones = "";

            // Call the method to create the appointment
            crearCita(fecha, hora, idMascota, estado, observaciones, servicioId);
        });

        return view;
    }

    private void obtenerHistorialMascotas(String userId) {
        if (userId == null || userId.isEmpty()) {
            Log.e("API_ERROR", "El userId es nulo o vacío");
            if (getContext() != null) {
                Toast.makeText(getContext(), "El ID de usuario es inválido.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        String url = "https://api.happypetshco.com/api/MascotasUsuario=" + userId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, // GET method
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("API_RESPONSE", response.toString());
                        try {
                            JSONArray mascotasArray = response.getJSONArray("mascotas");
                            petsList.clear();  // Limpiar cualquier dato existente

                            if (mascotasArray.length() > 0) {
                                for (int i = 0; i < mascotasArray.length(); i++) {
                                    JSONObject mascota = mascotasArray.getJSONObject(i);
                                    petsList.add(new Mascota(
                                            mascota.getString("id"),
                                            mascota.getString("nombre"), // Solo almacenar el nombre
                                            mascota.getString("edad"),
                                            mascota.getString("especie"),
                                            mascota.getString("raza"),
                                            mascota.getString("sexo"),
                                            mascota.getString("estado"),
                                            mascota.getString("imagen")
                                    ));
                                }

                                ArrayAdapter<Mascota> adapter = new ArrayAdapter<Mascota>(
                                        getContext(),
                                        android.R.layout.simple_spinner_item,
                                        petsList
                                )
                                {
                                    @Override
                                    public View getDropDownView(int position, View convertView, ViewGroup parent) {
                                        View view = super.getDropDownView(position, convertView, parent);
                                        TextView textView = (TextView) view.findViewById(android.R.id.text1);
                                        textView.setText(petsList.get(position).getNombre());
                                        return view;
                                    }

                                    @Override
                                    public View getView(int position, View convertView, ViewGroup parent) {
                                        // Este método maneja la vista del item seleccionado
                                        View view = super.getView(position, convertView, parent);
                                        TextView textView = (TextView) view.findViewById(android.R.id.text1);
                                        textView.setText(petsList.get(position).getNombre()); // Solo mostrar nombre
                                        return view;
                                    }
                                };
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                mascotaSpinner.setAdapter(adapter);
                            } else {
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), "No se encontraron mascotas", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("API_RESPONSE_ERROR", "Error al procesar JSON: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("API_ERROR", "Error de red: " + error.getMessage());
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error al obtener el historial de mascotas", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(getContext()).add(jsonObjectRequest);
    }

    private void crearCita(String fecha, String hora, String idMascota, String estado, String observaciones, String idServicio) {
        String url = "https://api.happypetshco.com/api/NuevaCita";

        // Crear un objeto JSON con los datos de la cita
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("fecha", fecha);
            requestBody.put("hora", hora);
            requestBody.put("id_mascota", idMascota);
            requestBody.put("estado", estado);  // Esto es opcional
            requestBody.put("observaciones", observaciones);  // Esto es opcional
            requestBody.put("id_servicio", idServicio);  // Usamos el ID del servicio
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        // Crear la solicitud POST
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, // Método POST
                url,
                requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Aquí puedes manejar la respuesta de éxito
                        Log.d("API_RESPONSE", response.toString());
                        Toast.makeText(getContext(), "Cita creada exitosamente", Toast.LENGTH_SHORT).show();
                        dismiss(); // Cerrar el diálogo
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Aquí puedes manejar el error de la solicitud
                        Log.e("API_ERROR", "Error de red: " + error.getMessage());
                        Toast.makeText(getContext(), "Error al crear la cita", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(getContext()).add(jsonObjectRequest);
    }

}

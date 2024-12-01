package com.example.happypets.view_cliente;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.happypets.R;
import com.example.happypets.adapters_cliente.MascotaAdapter;
import com.example.happypets.models.Mascota;
import com.example.happypets.perfilview.AgregarMascotaActivity;
import com.example.happypets.perfilview.DatosPersonalesActivity;
import com.example.happypets.perfilview.HistorialNotificacionesActivity;
import com.example.happypets.perfilview.MisComprasActivity;
import com.example.happypets.perfilview.VerCitasActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PerfilCliente extends Fragment {

    private String dni;
    private String phoneNumber;
    private String nombreCompleto;
    private String userId;
    private String token;
    private RecyclerView petsListView;
    private MascotaAdapter petsAdapter;
    private ArrayList<Mascota> petsList;

    public static PerfilCliente newInstance(String dni, String phoneNumber, String nombreCompleto, String userId, String token) {
        PerfilCliente fragment = new PerfilCliente();
        Bundle args = new Bundle();
        args.putString("dni", dni);
        args.putString("phoneNumber", phoneNumber);
        args.putString("nombreCompleto", nombreCompleto);
        args.putString("userId", userId);
        args.putString("token", token);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_perfil_cliente, container, false);

        if (getArguments() != null) {
            dni = getArguments().getString("dni");
            phoneNumber = getArguments().getString("phoneNumber");
            nombreCompleto = getArguments().getString("nombreCompleto");
            userId = getArguments().getString("userId");
            token = getArguments().getString("token");
        }

        TextView nombreTextView = view.findViewById(R.id.nombreTextView);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        TextView userIdTextView = view.findViewById(R.id.userIdTextView);

        nombreTextView.setText("HOLA: " + nombreCompleto);
        userIdTextView.setText("ID de Usuario: " + userId);

        petsListView = view.findViewById(R.id.petsListView);
        petsList = new ArrayList<>();
        petsAdapter = new MascotaAdapter(getContext(), petsList);
        petsListView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        petsListView.setAdapter(petsAdapter);

        obtenerHistorialMascotas(userId);

        // Set up button clicks
        setupButtonClicks(view);

        ImageButton notificationIcon = view.findViewById(R.id.notificationIcon);
        notificationIcon.setOnClickListener(v -> {
            NotificacionesDialogFragment notificacionesDialogFragment = NotificacionesDialogFragment.newInstance(userId, token);
            notificacionesDialogFragment.show(getChildFragmentManager(), "notificacionesDialog");
        });

        return view;
    }

    private void setupButtonClicks(View view) {
        String token = getArguments().getString("token");
        String userId = getArguments().getString("userId");

        // Button for "Datos Personales"
        view.findViewById(R.id.button_personal_data).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), DatosPersonalesActivity.class);
            intent.putExtra("token", token);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        // Button for "Mis Compras"
        view.findViewById(R.id.button_my_purchases).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MisComprasActivity.class);
            intent.putExtra("token", token);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        view.findViewById(R.id.button_notifications_history).setOnClickListener(v -> {
            // Navigate to the "Historial de Notificaciones" screen and send token and userId
            Intent intent = new Intent(getContext(), HistorialNotificacionesActivity.class);
            intent.putExtra("token", token);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        view.findViewById(R.id.button_view_appointments).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), VerCitasActivity.class);
            intent.putExtra("token", token);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        view.findViewById(R.id.button_add_pets).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AgregarMascotaActivity.class);
            intent.putExtra("token", token);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        // Actualiza la lista de mascotas cada vez que el fragmento se vuelve a mostrar
        obtenerHistorialMascotas(userId);
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
                Request.Method.GET, // Cambiar a método GET
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("API_RESPONSE", response.toString());
                        try {
                            JSONArray mascotasArray = response.getJSONArray("mascotas");
                            petsList.clear(); // Limpia la lista antes de agregar nuevos elementos

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
                                petsAdapter.notifyDataSetChanged(); // Notifica al adaptador que los datos han cambiado
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
                        if (error.networkResponse != null) {
                            Log.e("API_ERROR", "Código de estado: " + error.networkResponse.statusCode);
                            Log.e("API_ERROR", "Contenido: " + new String(error.networkResponse.data));
                        }
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
}

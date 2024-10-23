package com.example.happypets.submenu_admin;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager; // Asegúrate de importar GridLayoutManager
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.happypets.R;
import com.example.happypets.adapters_admin.MascotaAdapter;
import com.example.happypets.models.Mascota;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class EditarMascota extends Fragment {

    private static final String ARG_TOKEN = "token"; // Clave para el token
    private String token; // Variable para almacenar el token
    private RecyclerView recyclerView; // RecyclerView para mostrar la lista de mascotas
    private MascotaAdapter mascotaAdapter; // Adaptador para el RecyclerView
    private List<Mascota> listaMascotas; // Lista de mascotas

    // Método estático para crear una nueva instancia del fragmento y pasar el token
    public static EditarMascota newInstance(String token) {
        EditarMascota fragment = new EditarMascota();
        Bundle args = new Bundle();
        args.putString(ARG_TOKEN, token); // Almacenar el token en el Bundle
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Obtener el token del Bundle
        if (getArguments() != null) {
            token = getArguments().getString(ARG_TOKEN);
        }
        listaMascotas = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Infla el diseño para este fragmento
        View view = inflater.inflate(R.layout.activity_editar_mascota, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewMascotas); // Asegúrate de que tu RecyclerView tenga este id
        // Cambiar a GridLayoutManager para mostrar 2 columnas
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2 columnas
        mascotaAdapter = new MascotaAdapter(listaMascotas);
        recyclerView.setAdapter(mascotaAdapter);

        // Llamar a la API para listar mascotas
        listarMascotas();

        return view;
    }

    private void listarMascotas() {
        OkHttpClient client = new OkHttpClient();
        String url = "https://api-happypetshco-com.preview-domain.com/api/TodasMascotas";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token) // Agregar "Bearer" al token
                .get() // Especificar que es un método GET
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Manejo de error
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error al cargar las mascotas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    // Procesar la respuesta JSON
                    JsonObject jsonResponse = new Gson().fromJson(responseData, JsonObject.class);
                    JsonArray jsonMascotas = jsonResponse.getAsJsonArray("mascotas"); // Asumiendo que la clave es "mascotas"

                    // Convertir el JsonArray en una lista de Mascota
                    listaMascotas.clear();
                    for (JsonElement element : jsonMascotas) {
                        Mascota mascota = new Gson().fromJson(element, Mascota.class);
                        listaMascotas.add(mascota);
                    }

                    // Actualizar el RecyclerView
                    getActivity().runOnUiThread(() -> {
                        mascotaAdapter.notifyDataSetChanged(); // Notificar al adaptador que los datos han cambiado
                    });
                } else {
                    // Manejo de respuesta no exitosa
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error en la respuesta: " + response.message(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
}

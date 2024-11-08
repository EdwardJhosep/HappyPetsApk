package com.example.happypets.submenu_admin;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.happypets.R;
import com.example.happypets.adapters_admin.ServicioAdapter;
import com.example.happypets.models.Respuesta;
import com.example.happypets.models.Servicio;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class EditarServicio extends Fragment {

    private static final String API_URL = "https://api.happypetshco.com/api/ListarServicios";
    private String token;

    private RecyclerView recyclerView;
    private ServicioAdapter servicioAdapter;
    private List<Servicio> servicioList = new ArrayList<>();
    private List<Servicio> filteredServicioList = new ArrayList<>();

    public static EditarServicio newInstance(String token) {
        EditarServicio fragment = new EditarServicio();
        Bundle args = new Bundle();
        args.putString("token", token); // Pasar el token al fragmento
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            token = getArguments().getString("token");
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_editar_servicio, container, false);

        // Inicializar RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewServicios);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Inicializar el campo de búsqueda
        EditText searchEditText = view.findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Filtrar la lista según el texto ingresado
                filterServicios(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        // Llamar a la API para cargar los servicios
        listarServicios();

        return view;
    }

    // Método para obtener los servicios de la API
    private void listarServicios() {
        OkHttpClient client = new OkHttpClient();

        // Crear la solicitud HTTP con el token
        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        // Realizar la solicitud de forma asíncrona
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                // Manejar el error de la solicitud
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Error al cargar los servicios", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Obtener la respuesta en formato JSON
                    String responseBody = response.body().string();

                    // Usar Gson para convertir la respuesta JSON a un objeto Respuesta
                    Gson gson = new Gson();
                    Respuesta respuesta = gson.fromJson(responseBody, Respuesta.class);

                    // Obtener la lista de servicios
                    servicioList = respuesta.getServicios();
                    filteredServicioList.addAll(servicioList); // Inicialmente no hay filtro

                    // Actualizar el RecyclerView con la lista de servicios
                    getActivity().runOnUiThread(() -> {
                        servicioAdapter = new ServicioAdapter(filteredServicioList, token); // Pasa el token
                        recyclerView.setAdapter(servicioAdapter);
                    });
                } else {
                    getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Error al cargar los servicios", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void filterServicios(String query) {
        filteredServicioList.clear();

        if (query.isEmpty()) {
            filteredServicioList.addAll(servicioList);
        } else {
            for (Servicio servicio : servicioList) {
                if (servicio.getTipo().toLowerCase().contains(query.toLowerCase())) {
                    filteredServicioList.add(servicio);
                }
            }
        }

        // Notificar al adaptador que los datos han cambiado
        servicioAdapter.notifyDataSetChanged();
    }
}

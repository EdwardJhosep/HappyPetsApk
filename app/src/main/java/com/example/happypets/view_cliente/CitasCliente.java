package com.example.happypets.view_cliente;

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
import com.example.happypets.adapters_cliente.ServicioAdapterCliente;
import com.example.happypets.models.Respuesta;
import com.example.happypets.models.Servicio;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CitasCliente extends Fragment {

    private static final String API_URL = "https://api.happypetshco.com/api/ListarServicios";
    private String token;
    private String userId;

    private RecyclerView recyclerView;
    private ServicioAdapterCliente servicioAdapterCliente;
    private List<Servicio> servicioList = new ArrayList<>();
    private List<Servicio> filteredServicioList = new ArrayList<>();

    public static CitasCliente newInstance(String userId, String token) {
        CitasCliente fragment = new CitasCliente();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("token", token);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            token = getArguments().getString("token");
            userId = getArguments().getString("userId");
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_citas_cliente, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewServicios);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        EditText searchEditText = view.findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                filterServicios(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        listarServicios();

        return view;
    }

    private void listarServicios() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Error al cargar los servicios", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();

                    Gson gson = new Gson();
                    Respuesta respuesta = gson.fromJson(responseBody, Respuesta.class);

                    servicioList = respuesta.getServicios();
                    filteredServicioList.addAll(servicioList);

                    getActivity().runOnUiThread(() -> {
                        servicioAdapterCliente = new ServicioAdapterCliente(filteredServicioList, token, userId);
                        recyclerView.setAdapter(servicioAdapterCliente);
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

        servicioAdapterCliente.notifyDataSetChanged();
    }
}

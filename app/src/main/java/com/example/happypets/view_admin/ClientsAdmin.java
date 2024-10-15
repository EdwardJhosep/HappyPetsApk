package com.example.happypets.view_admin;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.happypets.R;
import com.example.happypets.adapters_admin.UsuariosAdapter;
import com.example.happypets.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ClientsAdmin extends Fragment {

    private static final String ARG_TOKEN = "token";
    private String token;
    private RecyclerView recyclerViewUsuarios;
    private UsuariosAdapter adapter;
    private List<User> usuarios;
    private List<User> usuariosFiltrados; // Lista para almacenar los usuarios filtrados
    private EditText buscarEditText;

    public static ClientsAdmin newInstance(String token) {
        ClientsAdmin fragment = new ClientsAdmin();
        Bundle args = new Bundle();
        args.putString(ARG_TOKEN, token);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            token = getArguments().getString(ARG_TOKEN);
        }
        usuarios = new ArrayList<>();
        usuariosFiltrados = new ArrayList<>(); // Inicializa la lista filtrada
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_clients_admin, container, false);

        buscarEditText = view.findViewById(R.id.buscarEditText); // Asegúrate de que este ID sea correcto
        recyclerViewUsuarios = view.findViewById(R.id.recyclerViewUsuarios);
        recyclerViewUsuarios.setLayoutManager(new LinearLayoutManager(getContext()));

        obtenerUsuarios();

        // Agrega el TextWatcher al EditText
        buscarEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No se necesita implementación aquí
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarUsuarios(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No se necesita implementación aquí
            }
        });

        return view;
    }

    private void obtenerUsuarios() {
        OkHttpClient client = new OkHttpClient();
        String url = "https://api-happypetshco-com.preview-domain.com/api/Usuarios";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Error al obtener usuarios", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONArray jsonUsuarios = jsonResponse.getJSONArray("usuarios");

                        for (int i = 0; i < jsonUsuarios.length(); i++) {
                            JSONObject jsonUsuario = jsonUsuarios.getJSONObject(i);
                            // Verificar si el usuario tiene permisos de CLIENTE
                            if (jsonUsuario.optString("permisos").equals("Usuario")) {
                                User usuario = new User(
                                        jsonUsuario.getString("dni"),
                                        jsonUsuario.getString("nombres"),
                                        jsonUsuario.getString("telefono"),
                                        jsonUsuario.has("ubicacion") && !jsonUsuario.isNull("ubicacion") ? jsonUsuario.getString("ubicacion") : "sin dirección"
                                );
                                usuarios.add(usuario);
                            }
                        }

                        // Copia la lista de usuarios a la lista filtrada
                        usuariosFiltrados.addAll(usuarios);

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                adapter = new UsuariosAdapter(usuariosFiltrados); // Usa la lista filtrada
                                recyclerViewUsuarios.setAdapter(adapter);
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getActivity(), "Error: " + response.message(), Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }

    private void filtrarUsuarios(String texto) {
        usuariosFiltrados.clear();
        if (texto.isEmpty()) {
            usuariosFiltrados.addAll(usuarios); // Si el texto está vacío, muestra todos los usuarios
        } else {
            String textoLower = texto.toLowerCase();
            for (User usuario : usuarios) {
                if (usuario.getDni().toLowerCase().contains(textoLower) ||
                        usuario.getNombres().toLowerCase().contains(textoLower)) {
                    usuariosFiltrados.add(usuario);
                }
            }
        }
        adapter.notifyDataSetChanged(); // Notifica al adaptador que los datos han cambiado
    }
}

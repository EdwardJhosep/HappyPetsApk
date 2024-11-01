package com.example.happypets.submenu_admin;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.happypets.R;
import com.example.happypets.adapters_admin.UsuariosAdapter2;
import com.example.happypets.models.User;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class EditarPersonal extends Fragment {

    private static final String ARG_TOKEN = "token";
    private String token;
    private RecyclerView recyclerViewUsuarios;
    private UsuariosAdapter2 adapter;
    private EditText buscarEditText;
    private List<User> listaUsuariosOriginal;

    public static EditarPersonal newInstance(String token) {
        EditarPersonal fragment = new EditarPersonal();
        Bundle args = new Bundle();
        args.putString(ARG_TOKEN, token);
        fragment.setArguments(args);
        return fragment;
    }

    // Método para establecer el token, si es necesario
    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            token = getArguments().getString(ARG_TOKEN);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_editar_personal, container, false);
        buscarEditText = view.findViewById(R.id.buscarEditText);
        recyclerViewUsuarios = view.findViewById(R.id.recyclerViewUsuarios);
        recyclerViewUsuarios.setLayoutManager(new LinearLayoutManager(getContext()));

        // Pasa el token al adaptador
        adapter = new UsuariosAdapter2(new ArrayList<>(), token);
        recyclerViewUsuarios.setAdapter(adapter);
        obtenerUsuarios();

        buscarEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (listaUsuariosOriginal != null) {
                    adapter.filtrarPorDNI(s.toString(), listaUsuariosOriginal);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void obtenerUsuarios() {
        new AsyncTask<Void, Void, List<User>>() {
            @Override
            protected List<User> doInBackground(Void... voids) {
                try {
                    URL url = new URL("https://api.happypetshco.com/api/Usuarios");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                    conn.connect();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    String jsonResponse = response.toString();
                    Gson gson = new Gson();
                    UsuarioResponse usuarioResponse = gson.fromJson(jsonResponse, UsuarioResponse.class);
                    return usuarioResponse.getUsuarios();

                } catch (Exception e) {
                    Log.e("Error", "Error al obtener usuarios: " + e.getMessage());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<User> usuarios) {
                if (usuarios != null) {
                    listaUsuariosOriginal = new ArrayList<>(usuarios);
                    adapter.updateUsuarios(usuarios);
                } else {
                    Toast.makeText(getContext(), "Error al obtener usuarios", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private class UsuarioResponse {
        private List<User> usuarios;

        public List<User> getUsuarios() {
            return usuarios;
        }
    }
}

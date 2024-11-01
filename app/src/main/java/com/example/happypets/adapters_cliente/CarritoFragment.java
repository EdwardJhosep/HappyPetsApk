package com.example.happypets.adapters_cliente;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import com.example.happypets.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class CarritoFragment extends BottomSheetDialogFragment {

    private String userId;
    private String token; // Variable para el token
    private ArrayList<JSONObject> productos = new ArrayList<>();
    private ListView listView;

    // Método para crear una nueva instancia del fragmento
    public static CarritoFragment newInstance(String userId, String token) {
        CarritoFragment fragment = new CarritoFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("token", token); // Agregar token a los argumentos
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.listarcarritocliente, container, false);
        listView = view.findViewById(R.id.listViewCarrito);

        // Obtener el userId y token de los argumentos
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
            token = getArguments().getString("token"); // Obtener el token
            new ListarCarritoTask().execute(userId);
        }

        return view;
    }

    private class ListarCarritoTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String userId = params[0];
            String apiUrl = "https://api.happypetshco.com/api/MostrarCarrito=" + userId;

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + token); // Establecer el token en el encabezado
                connection.setDoInput(true);
                connection.connect();

                // Verificar el código de respuesta
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Leer la respuesta
                    try (InputStream inputStream = connection.getInputStream();
                         InputStreamReader reader = new InputStreamReader(inputStream)) {
                        StringBuilder response = new StringBuilder();
                        int data;
                        while ((data = reader.read()) != -1) {
                            response.append((char) data);
                        }
                        return response.toString();
                    }
                } else {
                    return null; // Retornar null si la respuesta no es OK
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    // Verificar si la respuesta contiene el campo 'carrito'
                    if (jsonResponse.has("carrito")) {
                        JSONArray jsonArray = jsonResponse.getJSONArray("carrito");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            productos.add(jsonArray.getJSONObject(i));
                        }

                        // Crear el adaptador y configurarlo en el ListView
                        ListarCarritoAdapter adapter = new ListarCarritoAdapter(getActivity(), productos, userId, token);
                        listView.setAdapter(adapter);
                    } else {
                        Toast.makeText(getActivity(), "No hay productos en el carrito", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Error al parsear la respuesta", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "Error en la conexión o el servidor", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

package com.example.happypets.adapters_cliente;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import com.example.happypets.R;
import com.example.happypets.view_cliente.ProductoCliente;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class CarritoFragment extends BottomSheetDialogFragment {

    private String userId;
    private ArrayList<JSONObject> productos = new ArrayList<>();
    private ListView listView;

    // Método para crear una nueva instancia del fragmento
    public static CarritoFragment newInstance(String userId) {
        CarritoFragment fragment = new CarritoFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.listarcarritocliente, container, false);

        listView = view.findViewById(R.id.listViewCarrito);

        // Obtener el userId de los argumentos
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
            new ListarCarritoTask().execute(userId);
        }

        return view;
    }

    private class ListarCarritoTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String userId = params[0];
            String apiUrl = "https://api-happypetshco-com.preview-domain.com/api/ListarCarrito=" + userId;
            StringBuilder response = new StringBuilder();

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                int data;
                while ((data = inputStream.read()) != -1) {
                    response.append((char) data);
                }

                return response.toString();
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
                    JSONArray jsonArray = jsonResponse.getJSONArray("carrito");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        productos.add(jsonArray.getJSONObject(i));
                    }
                    ListarCarritoAdapter adapter = new ListarCarritoAdapter(getActivity(), productos);
                    listView.setAdapter(adapter); // Configurar el adaptador para el ListView
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Error al parsear la respuesta", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "Error en la conexión", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

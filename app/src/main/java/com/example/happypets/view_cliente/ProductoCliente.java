package com.example.happypets.view_cliente;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.happypets.R;
import com.example.happypets.adapters_cliente.ProductoAdapter;
import com.example.happypets.models.Producto;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ProductoCliente extends Fragment {

    private RecyclerView recyclerView;
    private ProductoAdapter productoAdapter;
    private ArrayList<Producto> productoList = new ArrayList<>();
    private EditText editTextSearch; // EditText para la búsqueda
    private CardView cardView1;
    private CardView cardView2;
    private TextView textViewMensaje; // TextView para mostrar mensajes

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_producto_cliente, container, false);
        cardView1 = view.findViewById(R.id.cardView1);
        cardView2 = view.findViewById(R.id.cardView2);
        textViewMensaje = view.findViewById(R.id.textViewMensaje); // Asegúrate de que exista en el layout

        editTextSearch = view.findViewById(R.id.editTextSearch);
        recyclerView = view.findViewById(R.id.recyclerViewProductos);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        productoAdapter = new ProductoAdapter(productoList);
        recyclerView.setAdapter(productoAdapter);

        // Llamada a la API
        new GetProductosTask().execute("https://api-happypetshco-com.preview-domain.com/api/ListarProductos");

        // Inicialmente mostrar los CardViews y ocultar el RecyclerView
        recyclerView.setVisibility(View.GONE);
        cardView1.setVisibility(View.VISIBLE);
        cardView2.setVisibility(View.VISIBLE);

        // Agregar TextWatcher para la búsqueda en tiempo real
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private class GetProductosTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("API Response", result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONArray jsonArray = jsonObject.getJSONArray("productos");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject productoJson = jsonArray.getJSONObject(i);
                    Producto producto = new Producto(
                            productoJson.getInt("id"),
                            productoJson.getString("nm_producto"),
                            productoJson.getString("descripcion"),
                            productoJson.getString("categoria"),
                            productoJson.getString("precio"),
                            productoJson.getString("descuento"),
                            productoJson.getString("stock"),
                            productoJson.getString("imagen")
                    );
                    productoList.add(producto);
                }
                productoAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                Toast.makeText(getContext(), "Error al procesar datos", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void filter(String text) {
        ArrayList<Producto> filteredList = new ArrayList<>();

        // Filtrar los productos según la búsqueda
        for (Producto producto : productoList) {
            if (producto.getNombre().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(producto);
            }
        }

        productoAdapter.updateList(filteredList);

        // Mostrar u ocultar el RecyclerView, los CardViews y el mensaje
        if (text.isEmpty()) {
            // Si el campo de búsqueda está vacío
            recyclerView.setVisibility(View.GONE); // Ocultar el RecyclerView
            textViewMensaje.setVisibility(View.GONE); // Ocultar mensaje
            cardView1.setVisibility(View.VISIBLE); // Mostrar el primer CardView
            cardView2.setVisibility(View.VISIBLE); // Mostrar el segundo CardView
        } else if (filteredList.isEmpty()) {
            // Si no hay resultados para la búsqueda
            recyclerView.setVisibility(View.GONE); // Ocultar el RecyclerView
            textViewMensaje.setText("Producto no conseguido."); // Mensaje de no encontrado
            textViewMensaje.setVisibility(View.VISIBLE); // Mostrar mensaje
            cardView1.setVisibility(View.VISIBLE); // Mostrar el primer CardView
            cardView2.setVisibility(View.VISIBLE); // Mostrar el segundo CardView
        } else {
            // Si hay resultados
            recyclerView.setVisibility(View.VISIBLE); // Mostrar el RecyclerView
            textViewMensaje.setVisibility(View.GONE); // Ocultar mensaje
            cardView1.setVisibility(View.GONE); // Ocultar el primer CardView
            cardView2.setVisibility(View.GONE); // Ocultar el segundo CardView
        }
    }
}

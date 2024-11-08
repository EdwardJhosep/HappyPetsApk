package com.example.happypets.view_cliente;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.happypets.R;
import com.example.happypets.adapters_cliente.CarritoFragment;
import com.example.happypets.adapters_cliente.ProductoAdapter;
import com.example.happypets.models.Producto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import android.os.Handler;

public class ProductoCliente extends Fragment {

    private RecyclerView recyclerView;
    private ProductoAdapter productoAdapter;
    private ArrayList<Producto> productoList = new ArrayList<>();
    private ArrayList<Producto> productoListOriginal = new ArrayList<>();
    private EditText editTextSearch;
    private CardView cardView1;
    private CardView cardView2;
    private TextView textViewMensaje;
    private ImageView imageViewCard2;
    private ImageView iconCarrito;

    private String userId;
    private String token;

    private int[] imagesCard2 = {R.drawable.image4, R.drawable.image5, R.drawable.image6};
    private int currentImageIndex2 = 0;
    private Handler handler2 = new Handler();

    public static ProductoCliente newInstance(String userId, String token) {
        ProductoCliente fragment = new ProductoCliente();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("token", token);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_producto_cliente, container, false);

        if (getArguments() != null) {
            userId = getArguments().getString("userId");
            token = getArguments().getString("token");
        }

        cardView1 = view.findViewById(R.id.cardView1);
        cardView2 = view.findViewById(R.id.cardView2);
        textViewMensaje = view.findViewById(R.id.textViewMensaje);
        editTextSearch = view.findViewById(R.id.editTextSearch);
        recyclerView = view.findViewById(R.id.recyclerViewProductos);
        imageViewCard2 = view.findViewById(R.id.imageViewCard2);
        iconCarrito = view.findViewById(R.id.iconCarrito);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        productoAdapter = new ProductoAdapter(productoList, userId, token);
        recyclerView.setAdapter(productoAdapter);

        new GetProductosTask().execute("https://api.happypetshco.com/api/ListarProductos");

        recyclerView.setVisibility(View.GONE);
        cardView1.setVisibility(View.VISIBLE);
        cardView2.setVisibility(View.VISIBLE);

        // Llamada a ListarCarritoTask al iniciar
        new ListarCarritoTask().execute(userId);

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

        startImageSliderCard2();

        iconCarrito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CarritoFragment carritoFragment = CarritoFragment.newInstance(userId, token);
                carritoFragment.show(getChildFragmentManager(), carritoFragment.getTag());
            }
        });

        return view;
    }


    private void startImageSliderCard2() {
        handler2.postDelayed(new Runnable() {
            @Override
            public void run() {
                currentImageIndex2 = (currentImageIndex2 + 1) % imagesCard2.length;
                imageViewCard2.setImageResource(imagesCard2[currentImageIndex2]);
                handler2.postDelayed(this, 3000);
            }
        }, 3000);
    }

    private class GetProductosTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + token);

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
                            productoJson.getString("imagen"),
                            productoJson.getString("colores") // Recuperar colores del JSON
                    );
                    productoList.add(producto);
                }
                productoListOriginal.addAll(productoList);
                productoAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                Toast.makeText(getContext(), "Error al procesar datos", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void filter(String text) {
        ArrayList<Producto> filteredList = new ArrayList<>();
        for (Producto producto : productoListOriginal) {
            if (producto.getNombre().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(producto);
            }
        }

        productoAdapter.updateList(filteredList);

        if (text.isEmpty()) {
            recyclerView.setVisibility(View.VISIBLE);
            textViewMensaje.setVisibility(View.GONE);
            cardView1.setVisibility(View.VISIBLE);
            cardView2.setVisibility(View.VISIBLE);
            productoAdapter.updateList(productoListOriginal);
        } else {
            if (filteredList.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                textViewMensaje.setText("Producto no conseguido.");
                textViewMensaje.setVisibility(View.VISIBLE);
                cardView1.setVisibility(View.VISIBLE);
                cardView2.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                textViewMensaje.setVisibility(View.GONE);
                cardView1.setVisibility(View.GONE);
                cardView2.setVisibility(View.GONE);
            }
        }
    }

    private class ListarCarritoTask extends AsyncTask<String, Void, String> {

        private static final String API_URL = "https://api.happypetshco.com/api/MostrarCarrito=";

        // Declare carritoArray as an ArrayList to hold the cart items
        private ArrayList<JSONObject> carritoArray;

        // Constructor
        public ListarCarritoTask() {
            carritoArray = new ArrayList<>(); // Initialize the carritoArray
        }

        @Override
        protected String doInBackground(String... params) {
            String userId = params[0];
            String apiUrl = API_URL + userId;

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
                e.printStackTrace(); // Log the exception
                return null; // Return null on exception
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    // Verificar si la respuesta contiene el campo 'carrito'
                    if (jsonResponse.has("carrito")) {
                        JSONArray jsonArray = jsonResponse.getJSONArray("carrito");
                        carritoArray.clear(); // Clear the array before adding new items
                        for (int i = 0; i < jsonArray.length(); i++) {
                            carritoArray.add(jsonArray.getJSONObject(i)); // Populate the carritoArray with products
                        }
                        // Check if carritoArray has products
                        if (carritoArray.size() > 0) {
                            iconCarrito.setImageResource(R.drawable.ic_cart_full); // Ícono para carrito con productos
                        } else {
                            iconCarrito.setImageResource(R.drawable.ic_carrito); // Ícono para carrito vacío
                        }
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
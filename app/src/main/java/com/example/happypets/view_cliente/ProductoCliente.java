package com.example.happypets.view_cliente;

import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.happypets.NotificationWorker;
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
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class ProductoCliente extends Fragment {

    private RecyclerView recyclerView;
    private ProductoAdapter productoAdapter;
    private ArrayList<Producto> productoList = new ArrayList<>();
    private ArrayList<Producto> productoListOriginal = new ArrayList<>();
    private EditText editTextSearch;
    private TextView textViewMensaje;
    private ImageView iconCarrito;

    private String userId;
    private String token;

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

        // Initialize views
        textViewMensaje = view.findViewById(R.id.textViewMensaje);
        editTextSearch = view.findViewById(R.id.editTextSearch);
        recyclerView = view.findViewById(R.id.recyclerViewProductos);
        iconCarrito = view.findViewById(R.id.iconCarrito);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, getResources().getDimensionPixelSize(R.dimen.grid_item_spacing), true));
        productoAdapter = new ProductoAdapter(productoList, userId, token);
        recyclerView.setAdapter(productoAdapter);

        // Start periodic notification task
        startNotificationWork();

        // Fetch products and set up search filter
        new GetProductosTask().execute("https://api.happypetshco.com/api/ListarProductos");

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

        // Handle cart icon click
        iconCarrito.setOnClickListener(v -> {
            CarritoFragment carritoFragment = CarritoFragment.newInstance(userId, token);
            carritoFragment.show(getChildFragmentManager(), carritoFragment.getTag());
        });

        // Load cart items
        new ListarCarritoTask().execute(userId);

        return view;
    }

    private void startNotificationWork() {
        Data inputData = new Data.Builder()
                .putString("userId", userId)
                .putString("token", token)
                .build();

        Constraints constraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build();

        PeriodicWorkRequest notificationWorkRequest = new PeriodicWorkRequest.Builder(NotificationWorker.class, 15, TimeUnit.MINUTES)
                .setInputData(inputData)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(getContext()).enqueue(notificationWorkRequest);
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
            textViewMensaje.setVisibility(View.GONE);
            productoAdapter.updateList(productoListOriginal);
        } else {
            if (filteredList.isEmpty()) {
                textViewMensaje.setText("Producto no conseguido.");
                textViewMensaje.setVisibility(View.VISIBLE);
            } else {
                textViewMensaje.setVisibility(View.GONE);
            }
        }
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
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONArray jsonArray = jsonObject.getJSONArray("productos");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject productoJson = jsonArray.getJSONObject(i);
                    String categoria = productoJson.getJSONObject("categorias").getString("nombre");

                    // Crear un objeto Producto
                    Producto producto = new Producto(
                            productoJson.getInt("id"),
                            productoJson.getString("nm_producto"),
                            productoJson.getString("descripcion"),
                            categoria,
                            productoJson.optString("sub_categorias_id", ""),
                            productoJson.optString("sub_sub_categorias_id", ""),
                            productoJson.getString("precio"),
                            productoJson.getString("descuento"),
                            productoJson.getString("stock"),
                            productoJson.getString("imagen"),
                            productoJson.getString("colores")
                    );

                    productoList.add(producto);
                }

                productoListOriginal.addAll(productoList);

                // Shuffle the product list to display items in random order
                Collections.shuffle(productoList);

                productoAdapter.notifyDataSetChanged();

            } catch (JSONException e) {
                Toast.makeText(getContext(), "Error al procesar datos", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    // Clase para controlar el espaciado entre los productos en el RecyclerView
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private final int spanCount;
        private final int spacing;
        private final boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // get item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount;
                outRect.right = (column + 1) * spacing / spanCount;

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount;
                outRect.right = spacing - (column + 1) * spacing / spanCount;
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
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

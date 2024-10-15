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
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import android.os.Handler;

public class ProductoCliente extends Fragment {

    private RecyclerView recyclerView;
    private ProductoAdapter productoAdapter;
    private ArrayList<Producto> productoList = new ArrayList<>();
    private ArrayList<Producto> productoListOriginal = new ArrayList<>(); // Lista original para el filtrado
    private EditText editTextSearch;
    private CardView cardView1;
    private CardView cardView2;
    private TextView textViewMensaje;
    private ImageView imageViewCard1;
    private ImageView imageViewCard2;
    private ImageView iconCarrito;

    private String userId;
    private String token; // Agregar variable para el token

    private int[] imagesCard2 = {R.drawable.image4, R.drawable.image5, R.drawable.image6};
    private int currentImageIndex2 = 0;
    private Handler handler2 = new Handler();

    // Método para crear una instancia del fragmento
    public static ProductoCliente newInstance(String userId, String token) {
        ProductoCliente fragment = new ProductoCliente();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("token", token); // Agregar el token a los argumentos
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_producto_cliente, container, false);

        // Obtener el userId y token de los argumentos
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
            token = getArguments().getString("token"); // Obtener el token
        }

        // Inicialización de vistas
        cardView1 = view.findViewById(R.id.cardView1);
        cardView2 = view.findViewById(R.id.cardView2);
        textViewMensaje = view.findViewById(R.id.textViewMensaje);
        editTextSearch = view.findViewById(R.id.editTextSearch);
        recyclerView = view.findViewById(R.id.recyclerViewProductos);
        imageViewCard1 = view.findViewById(R.id.imageViewCard1);
        imageViewCard2 = view.findViewById(R.id.imageViewCard2);
        iconCarrito = view.findViewById(R.id.iconCarrito);

        // Configuración del RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        productoAdapter = new ProductoAdapter(productoList, userId, token); // Pasar el token al adaptador
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

        startImageSliderCard2();

        // Cambiar el listener del icono del carrito
        iconCarrito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crear una instancia del CarritoFragment y pasar el userId y el token
                CarritoFragment carritoFragment = CarritoFragment.newInstance(userId, token);
                // Mostrar el BottomSheet
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
                handler2.postDelayed(this, 3000); // Repetir cada 3 segundos
            }
        }, 3000); // Primer cambio después de 3 segundos
    }

    private class GetProductosTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                // Establecer el token en el encabezado de autorización
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
                            productoJson.getString("imagen")
                    );
                    productoList.add(producto);
                }
                productoListOriginal.addAll(productoList); // Guarda la lista original
                productoAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                Toast.makeText(getContext(), "Error al procesar datos", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void filter(String text) {
        ArrayList<Producto> filteredList = new ArrayList<>();

        // Filtrar los productos según el texto ingresado
        for (Producto producto : productoListOriginal) { // Filtrar usando la lista original
            if (producto.getNombre().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(producto);
            }
        }

        // Actualizar el adaptador con la lista filtrada
        productoAdapter.updateList(filteredList);

        // Mostrar u ocultar el RecyclerView y el mensaje de acuerdo al resultado de la búsqueda
        if (text.isEmpty()) {
            // Si no hay texto, ocultar el mensaje y mostrar todo
            recyclerView.setVisibility(View.VISIBLE);
            textViewMensaje.setVisibility(View.GONE);
            cardView1.setVisibility(View.VISIBLE);
            cardView2.setVisibility(View.VISIBLE);
            productoAdapter.updateList(productoListOriginal); // Asegúrate de mostrar todos los productos
        } else {
            if (filteredList.isEmpty()) {
                // Si no hay productos coincidentes, mostrar el mensaje y ocultar el RecyclerView
                recyclerView.setVisibility(View.GONE);
                textViewMensaje.setText("Producto no conseguido.");
                textViewMensaje.setVisibility(View.VISIBLE);
                cardView1.setVisibility(View.VISIBLE);
                cardView2.setVisibility(View.VISIBLE);
            } else {
                // Si hay productos coincidentes, ocultar el mensaje y mostrar el RecyclerView
                recyclerView.setVisibility(View.VISIBLE);
                textViewMensaje.setVisibility(View.GONE);
                cardView1.setVisibility(View.GONE);
                cardView2.setVisibility(View.GONE);
            }
        }
    }
}

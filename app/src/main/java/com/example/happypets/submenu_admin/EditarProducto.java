package com.example.happypets.submenu_admin;

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
import android.widget.Toast;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.happypets.R;
import com.example.happypets.adapters_admin.ProductoAdapterEditar;
import com.example.happypets.models.Producto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class EditarProducto extends Fragment {

    private RecyclerView recyclerView;
    private ProductoAdapterEditar productoAdapterEditar;
    private ArrayList<Producto> productoList = new ArrayList<>();
    private EditText editTextSearch;
    private String token;

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_editar_prodcuto, container, false);

        editTextSearch = view.findViewById(R.id.editTextSearch);
        recyclerView = view.findViewById(R.id.recyclerViewProductos);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        productoAdapterEditar = new ProductoAdapterEditar(productoList, token);
        recyclerView.setAdapter(productoAdapterEditar);

        // Obtener los productos cuando se cargue el fragmento
        new GetProductosTask().execute("https://api.happypetshco.com/api/ListarProductos");

        // Filtrar productos mientras se escribe
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
                // Parsear el JSON de la respuesta
                JSONObject jsonObject = new JSONObject(result);
                JSONArray jsonArray = jsonObject.getJSONArray("productos");

                // Iterar sobre los productos
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject productoJson = jsonArray.getJSONObject(i);

                    // Extraer la categoría del producto (el campo 'categorias')
                    JSONObject categoriaJson = productoJson.getJSONObject("categorias");
                    String categoria = categoriaJson.getString("nombre");

                    // Extraer la subcategoría si existe
                    String subCategoria = productoJson.optString("sub_categorias_id", "");  // Usar optString para evitar null si no existe

                    // Extraer la sub-subcategoría si existe
                    String subSubCategoria = productoJson.optString("sub_sub_categorias_id", "");  // Usar optString para evitar null si no existe

                    // Crear un objeto Producto con la información
                    Producto producto = new Producto(
                            productoJson.getInt("id"),
                            productoJson.getString("nm_producto"),
                            productoJson.getString("descripcion"),
                            categoria,  // Categoría
                            subCategoria,  // Subcategoría
                            subSubCategoria,  // Sub-subcategoría
                            productoJson.getString("precio"),
                            productoJson.getString("descuento"),
                            productoJson.getString("stock"),
                            productoJson.getString("imagen"),
                            productoJson.getString("colores")  // Colores
                    );
                    productoList.add(producto);
                }
                productoAdapterEditar.notifyDataSetChanged();
            } catch (JSONException e) {
                Toast.makeText(getContext(), "Error al procesar datos", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void filter(String text) {
        ArrayList<Producto> filteredList = new ArrayList<>();
        for (Producto producto : productoList) {
            if (producto.getNombre().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(producto);
            }
        }
        productoAdapterEditar.updateList(filteredList);
    }

    // Método público para recargar los productos
    public void recargarProductos() {
        // Limpiar la lista actual
        productoList.clear();

        // Hacer la solicitud a la API para obtener los productos de nuevo
        new GetProductosTask().execute("https://api.happypetshco.com/api/ListarProductos");
    }

    // Este método se llamará cuando se detecte un cambio en el campo de búsqueda para recargar los productos
    private void updateProductsOnSearchChange(String searchQuery) {
        if (searchQuery.isEmpty()) {
            // Si no hay texto en el campo de búsqueda, recargar los productos completos
            recargarProductos();
        }
    }
}
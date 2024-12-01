package com.example.happypets.vista_previa;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.happypets.Login;
import com.example.happypets.R;
import com.example.happypets.view_cliente.MenuCliente;
import com.example.happypets.models.Producto;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InicioActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductoAdapterPreview productoAdapterEditar;
    private ArrayList<Producto> productoList = new ArrayList<>();
    private String token;
    private ViewPager2 viewPager;
    private Handler handler;
    private Runnable runnable;
    private VideoView videoView;

    private List<Integer> images = Arrays.asList(
            R.drawable.inicio1, R.drawable.inicio2, R.drawable.inicio3, R.drawable.inicio4
    );
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (!isInternetAvailable()) {
            showNoInternetDialog();
        }

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false); // Default value is false if not found
        String token = sharedPreferences.getString("token", null); // You can check for the token if needed

        if (isLoggedIn && token != null) {
            // The user is logged in, redirect to MenuCliente with the login data (token, etc.)
            Intent intent = new Intent(InicioActivity.this, MenuCliente.class);
            intent.putExtra("token", token);  // Send token to MenuCliente
            startActivity(intent);
            finish(); // Finish the current activity so the user can't navigate back to it
        } else {
            // The user is not logged in, show the login screen
            Log.d("LoginState", "User is not logged in.");
        }




        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        // Initialize the UI components
        Button btnIrOtraClase = findViewById(R.id.Ingresar);

        recyclerView = findViewById(R.id.recyclerViewProductos);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Retrieve token, possibly from shared preferences or passed in intent

        // Initialize the adapter with the token
        productoAdapterEditar = new ProductoAdapterPreview(productoList, token);
        recyclerView.setAdapter(productoAdapterEditar);

        // Fetch products when the activity is created
        new GetProductosTask().execute("https://api.happypetshco.com/api/ListarProductos");

        viewPager = findViewById(R.id.imageCarousel);
        ImageCarouselAdapter adapter = new ImageCarouselAdapter(images);
        viewPager.setAdapter(adapter);
        setupAutoSlide();
        videoView = findViewById(R.id.videoView);

        // Obtén la URI del archivo de video en la carpeta raw
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video);

        // Establece la URI del video en el VideoView
        videoView.setVideoURI(videoUri);

        // Agregar controles de medios (play, pause, etc.)
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        // Inicia la reproducción automática del video
        videoView.start();
        btnIrOtraClase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crear un Intent para ir a la otra actividad
                Intent intent = new Intent(InicioActivity.this, Login.class);
                startActivity(intent);  // Iniciar la nueva actividad
            }
        });

    }
    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }
    private void showNoInternetDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Sin Conexión a Internet")
                .setMessage("No hay conexión a Internet. Verifique su conexión y reintente.")
                .setCancelable(false) // No permite cerrar la alerta tocando fuera de ella
                .setPositiveButton("Reintentar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Reintentar la verificación de conexión
                        if (isInternetAvailable()) {
                            dialog.dismiss(); // Cerrar el diálogo si la conexión es exitosa
                        } else {
                            Toast.makeText(InicioActivity.this, "Todavía sin conexión. Intente más tarde.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Salir", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Cerrar la aplicación si el usuario decide salir
                        finishAffinity(); // Cierra la actividad y todas las actividades anteriores
                    }
                })
                .show();
    }
    private void setupAutoSlide() {
        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                int nextItem = viewPager.getCurrentItem() + 1;
                viewPager.setCurrentItem(nextItem, true);
                handler.postDelayed(this, 4000); // Changes every 4 seconds
            }
        };
        handler.postDelayed(runnable, 4000);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }
    private class GetProductosTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            try {
                // Establish connection and set headers
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + token);

                // Read the response
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
                // Parse the JSON response
                JSONObject jsonObject = new JSONObject(result);
                JSONArray jsonArray = jsonObject.getJSONArray("productos");

                // Lista temporal para los productos con más descuento
                ArrayList<Producto> productosConDescuento = new ArrayList<>();

                // Loop through the products and add them to the list
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject productoJson = jsonArray.getJSONObject(i);

                    // Extract product data
                    JSONObject categoriaJson = productoJson.getJSONObject("categorias");
                    String categoria = categoriaJson.getString("nombre");

                    String subCategoria = productoJson.optString("sub_categorias_id", "");
                    String subSubCategoria = productoJson.optString("sub_subcategorias_id", "");

                    // Create a Product object
                    Producto producto = new Producto(
                            productoJson.getInt("id"),
                            productoJson.getString("nm_producto"),
                            productoJson.getString("descripcion"),
                            categoria,
                            subCategoria,
                            subSubCategoria,
                            productoJson.getString("precio"),
                            productoJson.getString("descuento"),
                            productoJson.getString("stock"),
                            productoJson.getString("imagen"),
                            productoJson.getString("colores")
                    );

                    productosConDescuento.add(producto);
                }

                // Ordenar los productos por el descuento de mayor a menor
                productosConDescuento.sort((p1, p2) -> {
                    try {
                        double descuento1 = p1.getDescuento() != null && !p1.getDescuento().isEmpty()
                                ? Double.parseDouble(p1.getDescuento())
                                : 0.0;
                        double descuento2 = p2.getDescuento() != null && !p2.getDescuento().isEmpty()
                                ? Double.parseDouble(p2.getDescuento())
                                : 0.0;
                        return Double.compare(descuento2, descuento1);
                    } catch (NumberFormatException e) {
                        Log.e("SortError", "Error al convertir descuento: " + e.getMessage());
                        return 0; // Mantener el orden actual en caso de error
                    }
                });


                // Tomar solo los dos productos con mayor descuento
                if (productosConDescuento.size() > 2) {
                    productosConDescuento = new ArrayList<>(productosConDescuento.subList(0, 2));
                }

                // Actualizar la lista de productos en el adaptador
                productoList.clear();
                productoList.addAll(productosConDescuento);

                // Notify the adapter that the data has changed
                productoAdapterEditar.notifyDataSetChanged();
            } catch (JSONException e) {
                Toast.makeText(InicioActivity.this, "Error al procesar datos", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    // Filter products based on the search text
    private void filter(String text) {
        ArrayList<Producto> filteredList = new ArrayList<>();
        for (Producto producto : productoList) {
            if (producto.getNombre().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(producto);
            }
        }
        productoAdapterEditar.updateList(filteredList);
    }

}

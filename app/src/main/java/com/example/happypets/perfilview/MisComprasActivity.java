package com.example.happypets.perfilview;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.happypets.R;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MisComprasActivity extends AppCompatActivity {

    private ListView listView;
    private CarritoAdapterFinal carritoAdapterFinal;
    private ArrayList<JSONObject> carritoItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_compras);

        listView = findViewById(R.id.listViewCarrito);

        // Retrieve the token and userId from the Intent
        String token = getIntent().getStringExtra("token");
        String userId = getIntent().getStringExtra("userId");

        // Fetch data
        new FetchCarritoDataTask(token).execute("https://api.happypetshco.com/api/MostrarCarrito=" + userId);

        carritoAdapterFinal = new CarritoAdapterFinal(this, carritoItems, token, userId);
        listView.setAdapter(carritoAdapterFinal);
    }

    private class FetchCarritoDataTask extends AsyncTask<String, Void, String> {
        private String token;

        FetchCarritoDataTask(String token) {
            this.token = token;
        }

        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Authorization", "Bearer " + token);
                urlConnection.setConnectTimeout(10000);
                urlConnection.setReadTimeout(10000);

                int statusCode = urlConnection.getResponseCode();
                if (statusCode != HttpURLConnection.HTTP_OK) {
                    return "Error: " + statusCode + " - " + urlConnection.getResponseMessage();
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

            } catch (Exception e) {
                e.printStackTrace();
                return "Error fetching data: " + e.getMessage();
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.contains("Error")) {
                Toast.makeText(MisComprasActivity.this, result, Toast.LENGTH_LONG).show();
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray carritoArray = jsonObject.getJSONArray("carrito");

                    // Clear the previous items to avoid duplication
                    carritoItems.clear();

                    // Populate carritoItems with data from API response, filtering by "pagado"
                    for (int i = 0; i < carritoArray.length(); i++) {
                        JSONObject item = carritoArray.getJSONObject(i);

                        // Check if the "pagado" field is "Confirmado"
                        String pagado = item.optString("pagado", "");
                        if (pagado.equals("Confirmado")) {

                            // Extract product details
                            JSONObject producto = item.getJSONObject("producto");
                            item.put("producto_nombre", producto.optString("nm_producto", "N/A"));
                            item.put("producto_imagen", producto.optString("imagen", ""));
                            item.put("updated_at", producto.optString("updated_at", ""));

                            // Add item to carritoItems
                            carritoItems.add(item);
                        }
                    }

                    // Notify the adapter that the data has changed
                    carritoAdapterFinal.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MisComprasActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}

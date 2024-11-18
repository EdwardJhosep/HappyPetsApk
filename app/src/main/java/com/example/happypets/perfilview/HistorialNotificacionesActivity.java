package com.example.happypets.perfilview;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.happypets.R;
import com.example.happypets.adapters_cliente.NotificationsAdapter;
import com.example.happypets.models.Notification;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HistorialNotificacionesActivity extends AppCompatActivity {

    private String userId;
    private String token;
    private ListView notificationListViewNoLeidas;
    private NotificationsAdapter adapter;
    private List<Notification> notificationsList = new ArrayList<>();

    private static final int PERMISSION_REQUEST_POST_NOTIFICATIONS = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_notificaciones); // Asegúrate de tener este layout

        notificationListViewNoLeidas = findViewById(R.id.notificationListViewNoLeidas);

        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra("userId");
            token = intent.getStringExtra("token");
        }

        adapter = new NotificationsAdapter(this, notificationsList, userId, token);
        notificationListViewNoLeidas.setAdapter(adapter);

        new FetchNotificationsTask().execute("https://api.happypetshco.com/api/NotiNovedadesAll=" + userId);

        solicitarPermisosNotificaciones();
    }

    /**
     * Método para solicitar permisos de notificaciones en dispositivos Android Tiramisu (API 33) o superior.
     */
    private void solicitarPermisosNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_POST_NOTIFICATIONS);
            }
        }
    }

    /**
     * Manejar la respuesta del usuario a la solicitud de permisos.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_REQUEST_POST_NOTIFICATIONS){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permiso para notificaciones concedido", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permiso para notificaciones denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * AsyncTask para obtener las notificaciones desde la API.
     */
    private class FetchNotificationsTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Authorization", "Bearer " + token);
                urlConnection.connect();

                int responseCode = urlConnection.getResponseCode();
                if(responseCode != HttpURLConnection.HTTP_OK){
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                return result.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    if (jsonResponse.has("notificaciones")) {
                        JSONArray notifications = jsonResponse.getJSONArray("notificaciones");
                        notificationsList.clear();
                        for (int i = 0; i < notifications.length(); i++) {
                            JSONObject notification = notifications.getJSONObject(i);

                            String id = notification.getString("id"); // Asegúrate de que el id esté presente en la respuesta

                            // Extraemos los demás datos de la notificación
                            String message = notification.getJSONObject("data").getString("mensaje");
                            String status = notification.getJSONObject("data").getString("estado");
                            String observations = notification.getJSONObject("data").getString("observaciones");

                            // Añadimos la notificación a la lista
                            notificationsList.add(new Notification(id, message, status, observations));
                        }
                        // Actualizamos el adaptador para reflejar los cambios
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(HistorialNotificacionesActivity.this, "No se encontraron notificaciones", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(HistorialNotificacionesActivity.this, "Error al procesar las notificaciones", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(HistorialNotificacionesActivity.this, "Error al cargar las notificaciones", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

package com.example.happypets;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.happypets.vista_previa.InicioActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NotificationWorker extends Worker {

    private static final String CHANNEL_ID = "notificaciones_channel";
    private static final String PREFS_NAME = "NotificationPrefs";
    private static final String LAST_NOTIFICATION_KEY = "last_notification_time";
    private static final long MIN_NOTIFICATION_INTERVAL = 30 * 60 * 1000; // 30 minutos

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String userId = getInputData().getString("userId");
        String token = getInputData().getString("token");

        // Validar entrada
        if (userId == null || token == null) {
            Log.e("NotificationWorker", "Faltan parámetros necesarios (userId o token)");
            return Result.failure();
        }

        try {
            // Control de frecuencia de notificaciones
            if (!shouldSendNotification(getApplicationContext())) {
                return Result.success(); // Salir si no ha pasado el intervalo mínimo
            }

            // Llamar a la API y procesar la respuesta
            String result = fetchNotificationsFromApi(userId, token);
            if (result != null) {
                JSONObject jsonResponse = new JSONObject(result);
                if (jsonResponse.has("notificaciones")) {
                    JSONArray notifications = jsonResponse.getJSONArray("notificaciones");

                    // Mostrar cada notificación
                    for (int i = 0; i < notifications.length(); i++) {
                        JSONObject notification = notifications.getJSONObject(i);
                        String message = notification.optJSONObject("data").optString("mensaje", "Tienes novedades");
                        showNotification(getApplicationContext(), message, "Nueva Notificación de HappyPets");
                    }

                    // Actualizar última hora de notificación
                    updateLastNotificationTime(getApplicationContext());
                }
            }
            return Result.success();
        } catch (Exception e) {
            Log.e("NotificationWorker", "Error al procesar notificaciones", e);
            return Result.failure();
        }
    }

    private String fetchNotificationsFromApi(String userId, String token) throws Exception {
        StringBuilder result = new StringBuilder();
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL("https://api.happypetshco.com/api/NotiNovedades=" + userId);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Authorization", "Bearer " + token);
            urlConnection.setConnectTimeout(5000); // 5 segundos
            urlConnection.setReadTimeout(5000);

            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
            } else {
                Log.e("NotificationWorker", "Error en la API: Código " + responseCode);
                return null;
            }
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return result.toString();
    }

    private void showNotification(Context context, String message, String title) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) return;

        // Crear canal de notificaciones si es necesario
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Notificaciones", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notificaciones de novedades de HappyPets");
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        // Intent para abrir la actividad principal al tocar la notificación
        Intent intent = new Intent(context, InicioActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Crear la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notificacion)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent);

        // Mostrar la notificación
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private boolean shouldSendNotification(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long lastNotificationTime = prefs.getLong(LAST_NOTIFICATION_KEY, 0);
        long currentTime = System.currentTimeMillis();

        // Verificar si ha pasado el intervalo mínimo
        return (currentTime - lastNotificationTime >= MIN_NOTIFICATION_INTERVAL);
    }

    private void updateLastNotificationTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putLong(LAST_NOTIFICATION_KEY, System.currentTimeMillis()).apply();
    }
}

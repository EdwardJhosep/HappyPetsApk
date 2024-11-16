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
    private static final long NOTIFICATION_INTERVAL = 2 * 60 * 60 * 1000; // 2 hours in milliseconds
    private static final String PREFS_NAME = "NotificationPrefs";
    private static final String LAST_NOTIFICATION_TIME_KEY = "lastNotificationTime";

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String userId = getInputData().getString("userId");
        String token = getInputData().getString("token");

        try {
            String result = fetchNotificationsFromApi(userId, token);
            if (result != null) {
                JSONObject jsonResponse = new JSONObject(result);
                if (jsonResponse.has("notificaciones")) {
                    JSONArray notifications = jsonResponse.getJSONArray("notificaciones");
                    for (int i = 0; i < notifications.length(); i++) {
                        JSONObject notification = notifications.getJSONObject(i);
                        String message = notification.getJSONObject("data").getString("mensaje");

                        // Get the last notification time from SharedPreferences
                        SharedPreferences prefs = getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                        long lastNotificationTime = prefs.getLong(LAST_NOTIFICATION_TIME_KEY, 0);

                        // Check if 2 hours have passed since the last notification
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastNotificationTime > NOTIFICATION_INTERVAL) {
                            showNotification(getApplicationContext(), message, "Nueva Notificación de HappyPets");

                            // Save the current time as the last notification time
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putLong(LAST_NOTIFICATION_TIME_KEY, currentTime);
                            editor.apply();
                        }
                    }
                }
            }
            return Result.success();
        } catch (Exception e) {
            Log.e("NotificationWorker", "Error al obtener notificaciones", e);
            return Result.failure();
        }
    }

    private String fetchNotificationsFromApi(String userId, String token) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL("https://api.happypetshco.com/api/NotiNovedades=" + userId);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Authorization", "Bearer " + token);
        urlConnection.connect();

        BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        reader.close();
        urlConnection.disconnect();

        return result.toString();
    }

    private void showNotification(Context context, String message, String title) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Notificaciones", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notificaciones de novedades de HappyPets");
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, InicioActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notificacion)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}

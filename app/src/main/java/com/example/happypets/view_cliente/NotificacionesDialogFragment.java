package com.example.happypets.view_cliente;

import android.Manifest;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.happypets.R;
import com.example.happypets.adapters_cliente.NotificationAdapter;
import com.example.happypets.vista_previa.InicioActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class NotificacionesDialogFragment extends DialogFragment {

    private String userId;
    private String token;
    private ListView petsListView;
    private NotificationAdapter adapter;
    private List<Notification> notificationsList = new ArrayList<>();

    private static final String CHANNEL_ID = "notificaciones_channel";

    public static NotificacionesDialogFragment newInstance(String userId, String token) {
        NotificacionesDialogFragment fragment = new NotificacionesDialogFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("token", token);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notificaciones, container, false);
        petsListView = view.findViewById(R.id.petsListView);
        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.card_background);

        if (getArguments() != null) {
            userId = getArguments().getString("userId");
            token = getArguments().getString("token");
        }


        adapter = new NotificationAdapter(getContext(), notificationsList);
        petsListView.setAdapter(adapter);

        // Verifica la conexión a Internet antes de intentar obtener notificaciones
        if (!isNetworkAvailable()) {
            Toast.makeText(getContext(), "No hay conexión a internet", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Llamada a la API para obtener notificaciones
        new FetchNotificationsTask().execute("https://api.happypetshco.com/api/NotiNovedades=" + userId);

        // Asegurarse de tener permisos para notificaciones
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.8), ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setGravity(Gravity.END | Gravity.RIGHT);
            dialog.getWindow().setWindowAnimations(R.style.DialogSlideAnimation);
        }
    }

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
                            String message = notification.getJSONObject("data").getString("mensaje");
                            String status = notification.getJSONObject("data").getString("estado");
                            String observations = notification.getJSONObject("data").getString("observaciones");

                            notificationsList.add(new Notification(message, status, observations));

                            // Muestra la notificación en la bandeja del sistema
                            showNotification(message, "Nueva Notificación de HappyPets");
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "No se encontraron notificaciones", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Error al procesar las notificaciones", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Error al cargar las notificaciones", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static class Notification {
        private String message;
        private String status;
        private String observations;

        public Notification(String message, String status, String observations) {
            this.message = message;
            this.status = status;
            this.observations = observations;
        }

        public String getMessage() {
            return message;
        }

        public String getStatus() {
            return status;
        }

        public String getObservations() {
            return observations;
        }
    }

    private void showNotification(String message, String title) {
        // Crea el canal de notificaciones (para Android 8.0 y superior)
        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) {
            Log.e("Notification", "Notification Manager is null.");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = notificationManager.getNotificationChannel(CHANNEL_ID);
            if (channel == null) {
                CharSequence name = "Notificaciones";
                String description = "Notificaciones de novedades de HappyPets";
                int importance = NotificationManager.IMPORTANCE_HIGH;
                channel = new NotificationChannel(CHANNEL_ID, name, importance);
                channel.setDescription(description);
                channel.enableLights(true);
                channel.setLightColor(Color.BLUE);
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{0, 500, 1000});
                notificationManager.createNotificationChannel(channel);
            }
        }

        Intent intent = new Intent(getContext(), InicioActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notificacion) // Icono por defecto para la notificación
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Prioridad alta para la notificación
                .setAutoCancel(true) // La notificación desaparecerá al hacer clic
                .setColor(ContextCompat.getColor(getContext(), R.color.primary_color)) // Color personalizado para la notificación
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message)) // Permite expandir el mensaje si es largo
                .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE) // Agrega sonido y vibración por defecto
                .setContentIntent(pendingIntent); // Configura el PendingIntent que abrirá InicioActivity cuando se haga clic

        notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
    }

    // Verifica si hay conexión a internet antes de intentar obtener notificaciones
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}

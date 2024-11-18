package com.example.happypets.view_cliente;

import android.Manifest;
import android.app.Dialog;
import com.example.happypets.models.Notification;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.happypets.R;
import com.example.happypets.adapters_cliente.NotificationAdapter;

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
    private ListView notificationListViewNoLeidas;
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
        notificationListViewNoLeidas = view.findViewById(R.id.notificationListViewNoLeidas);
        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.card_background);

        if (getArguments() != null) {
            userId = getArguments().getString("userId");
            token = getArguments().getString("token");
        }
        // Pass the token along with the userId to the adapter
        adapter = new NotificationAdapter(getContext(), notificationsList, userId, token);
        notificationListViewNoLeidas.setAdapter(adapter);



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

}
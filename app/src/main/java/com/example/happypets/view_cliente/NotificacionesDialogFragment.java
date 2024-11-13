package com.example.happypets.view_cliente;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    private ListView petsListView;
    private NotificationAdapter adapter;
    private List<Notification> notificationsList = new ArrayList<>();

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

        new FetchNotificationsTask().execute("https://api.happypetshco.com/api/NotiNovedades=" + userId);

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
}


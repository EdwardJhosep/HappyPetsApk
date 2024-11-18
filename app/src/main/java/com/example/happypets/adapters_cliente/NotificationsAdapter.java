package com.example.happypets.adapters_cliente;

import com.example.happypets.models.Notification;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.example.happypets.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationsAdapter extends BaseAdapter {

    private Context context;
    private String userId;  // Store userId in adapter
    private List<Notification> notifications;
    private String token;  // Store userId in adapter


    // Update the constructor to accept userId
    public NotificationsAdapter(Context context, List<Notification> notifications, String userId, String token) {
        this.context = context;
        this.notifications = notifications;
        this.userId = userId;  // Store userId in adapter
        this.token = token;  // Store token in adapter
    }



    @Override
    public int getCount() {
        return notifications.size();
    }

    @Override
    public Object getItem(int position) {
        return notifications.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_notifications, parent, false);
        }
        // Bind data to the views
        Notification notification = notifications.get(position);
        TextView messageTextView = convertView.findViewById(R.id.messageTextView);
        TextView statusTextView = convertView.findViewById(R.id.statusTextView);
        TextView observationsTextView = convertView.findViewById(R.id.observationsTextView);

        // Cambiar color solo a los títulos
        SpannableString messageText = new SpannableString("Mensaje: " + notification.getMessage());
        messageText.setSpan(new ForegroundColorSpan(Color.parseColor("#FF5722")), 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);  // Color naranja para "Mensaje"
        messageTextView.setText(messageText);

        SpannableString statusText = new SpannableString("Estado: " + notification.getStatus());
        statusText.setSpan(new ForegroundColorSpan(Color.parseColor("#0019C1")), 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);  // Color verde para "Estado"
        statusTextView.setText(statusText);

        SpannableString observationsText = new SpannableString("Observaciones: " + notification.getObservations());
        observationsText.setSpan(new ForegroundColorSpan(Color.parseColor("#EB5D25")), 0, 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);  // Color azul para "Observaciones"
        observationsTextView.setText(observationsText);
        return convertView;
    }

    private class MarkNotificationTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String notificationId = params[0];
            String currentDateTime = params[1];

            if (notificationId == null || notificationId.isEmpty() || userId == null || userId.isEmpty()) {
                return "Error: ID de notificación o usuario inválido.";
            }

            try {
                // URL with specific format
                String urlString = "https://api.happypetshco.com/api/NotiNovedadesUpdate=" + notificationId + "=" + userId;
                URL url = new URL(urlString);

                // Open the connection
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("Authorization", "Bearer " + token);  // Pass the token here

                // Get the server response
                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return "✔";
                } else {
                    // Read the error stream to get the error message
                    InputStream errorStream = connection.getErrorStream();
                    if (errorStream != null) {
                        String errorResponse = new BufferedReader(new InputStreamReader(errorStream))
                                .lines()
                                .collect(Collectors.joining("\n"));
                        return "✔" + errorResponse;
                    } else {
                        return "Error desconocido al actualizar el estado de la notificación.";
                    }
                }

            } catch (MalformedURLException e) {
                Log.e("API Error", "URL malformada: " + e.getMessage(), e);
                return "Error: URL malformada.";
            } catch (IOException e) {
                Log.e("API Error", "Error de conexión: " + e.getMessage(), e);
                return "Error de conexión. Intenta más tarde.";
            } catch (Exception e) {
                Log.e("API Error", "Error inesperado: " + e.getMessage(), e);
                return "Error inesperado al procesar la solicitud.";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
        }
    }

}

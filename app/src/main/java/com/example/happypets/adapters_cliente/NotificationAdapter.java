package com.example.happypets.adapters_cliente;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.happypets.R;
import com.example.happypets.view_cliente.NotificacionesDialogFragment.Notification;

import java.util.List;

public class NotificationAdapter extends BaseAdapter {

    private Context context;
    private List<Notification> notifications;

    public NotificationAdapter(Context context, List<Notification> notifications) {
        this.context = context;
        this.notifications = notifications;
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
            convertView = inflater.inflate(R.layout.item_notification, parent, false);

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
}

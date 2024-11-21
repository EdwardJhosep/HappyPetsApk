package com.example.happypets.adapters_cliente;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.happypets.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class HistorialCitasAdapter extends ArrayAdapter<JSONObject> {

    private final Context context;
    private final List<JSONObject> citasList;

    public HistorialCitasAdapter(Context context, List<JSONObject> citas) {
        super(context, R.layout.item_historial_cita, citas);
        this.context = context;
        this.citasList = citas;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_historial_cita, parent, false);
        }

        // Obtener el objeto JSON que representa una cita
        JSONObject cita = citasList.get(position);

        // Encontrar las vistas en el layout de cada ítem
        TextView citaFechaHora = convertView.findViewById(R.id.citaFechaHora);
        TextView citaFechaHoraTitulo = convertView.findViewById(R.id.citaFechaHoraTitulo);
        TextView citaSintomas = convertView.findViewById(R.id.citaSintomas);
        TextView citaEstado = convertView.findViewById(R.id.citaEstado);
        TextView citaTratamiento = convertView.findViewById(R.id.citaTratamiento);
        TextView citaImporte = convertView.findViewById(R.id.citaImporte);
        TextView citaObservaciones = convertView.findViewById(R.id.citaObservaciones);
        TextView citaProximaCita = convertView.findViewById(R.id.citaProximaCita);

        try {
            // Extraer los valores de la cita desde el objeto JSON
            String fecha = cita.getString("fecha");
            String hora = cita.getString("hora");
            String sintomas = cita.optString("sintomas", "No disponibles");
            String estado = cita.getString("estado");
            String tratamiento = cita.optString("tratamiento", "No disponibles");
            String importe = String.format("%.2f", cita.getDouble("importe")); // Formatear importe a dos decimales
            String observaciones = cita.optString("observaciones", "No disponibles");
            String proximaCita = cita.optString("proxima_cita", "No disponible");

            // Asignar solo la fecha a citaFechaHoraTitulo
            citaFechaHoraTitulo.setText(fecha);

            // Asignar fecha y hora con título a citaFechaHora
            String fechaHora = "Fecha: " + fecha + "\nHora: " + hora;
            citaFechaHora.setText(fechaHora);

            // Establecer los valores en los demás TextViews correspondientes
            citaSintomas.setText("Síntomas: " + sintomas);
            citaEstado.setText("Estado: " + estado);
            citaTratamiento.setText("Tratamiento: " + tratamiento);
            citaImporte.setText("Importe: S/. " + importe);
            citaObservaciones.setText("Observaciones: " + observaciones);
            citaProximaCita.setText("Próxima Cita: " + proximaCita);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return convertView;
    }
}

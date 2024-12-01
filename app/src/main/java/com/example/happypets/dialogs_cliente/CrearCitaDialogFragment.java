package com.example.happypets.dialogs_cliente;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.happypets.R;
import com.example.happypets.models.Mascota;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrearCitaDialogFragment extends DialogFragment {

    private String token;
    private String tipoServicio;

    private String userId;
    private String servicioId;
    private EditText fechaEditText;
    private EditText horasHospedajeEditText;
    private EditText horaEditText;
    private TextView userIdTextView;
    private Spinner mascotaSpinner;
    private CheckBox yapeCheckBox;
    private CheckBox efectivoCheckBox;
    private EditText sintomasEditText;

    private ArrayList<Mascota> petsList = new ArrayList<>();

    public CrearCitaDialogFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Light_Dialog);
    }

    @Override
    public void onStart() {
        super.onStart();
        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setAttributes(params);

        // Apply rounded background
        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.card_background);
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_crear_cita_cliente, container, false);


        // Retrieve arguments passed to the dialog
        if (getArguments() != null) {
            token = getArguments().getString("token");
            userId = getArguments().getString("userId");
            servicioId = getArguments().getString("servicioId");
            tipoServicio = getArguments().getString("tipoServicio");
        }

        horasHospedajeEditText = view.findViewById(R.id.horasHospedajeEditText);
        sintomasEditText = view.findViewById(R.id.sintomasEditText);

        List<String> hospedajeVariations = Arrays.asList("Hospedaje", "hospedaje", "HOSPEDAJE", "hospeda", "HOSPEDAEJE", "hospedaje");

        boolean isHospedaje = hospedajeVariations.stream().anyMatch(variation -> tipoServicio != null && tipoServicio.toLowerCase().contains(variation.toLowerCase()));

        if (isHospedaje) {
            horasHospedajeEditText.setVisibility(View.VISIBLE);
            sintomasEditText.setVisibility(View.GONE);
        } else {
            horasHospedajeEditText.setVisibility(View.GONE);
            sintomasEditText.setVisibility(View.VISIBLE);
        }

        yapeCheckBox = view.findViewById(R.id.yapeCheckBox);
        efectivoCheckBox = view.findViewById(R.id.efectivoCheckBox);
        sintomasEditText = view.findViewById(R.id.sintomasEditText);
        fechaEditText = view.findViewById(R.id.fechaEditText);
        horaEditText = view.findViewById(R.id.horaEditText);
        userIdTextView = view.findViewById(R.id.userIdTextView);
        mascotaSpinner = view.findViewById(R.id.MascotaSpinner);
        obtenerHistorialMascotas(userId);


        horaEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    getContext(),
                    (timePicker, hourOfDay, minuteOfHour) -> {
                        if (hourOfDay < 8 || hourOfDay > 17) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setMessage("Por favor, seleccione una hora entre 8 AM y 5 PM.")
                                    .setTitle("Hora no válida")
                                    .setPositiveButton("Aceptar", (dialog, id) -> dialog.dismiss())
                                    .create()
                                    .show();
                            return;
                        }

                        String amPm;
                        if (hourOfDay >= 12) {
                            amPm = "PM";
                            hourOfDay = hourOfDay == 12 ? 12 : hourOfDay - 12;
                        } else {
                            amPm = "AM";
                            hourOfDay = hourOfDay == 0 ? 12 : hourOfDay;
                        }

                        String selectedTime = String.format("%02d:%02d %s", hourOfDay, minuteOfHour, amPm);
                        horaEditText.setText(selectedTime); // Establecer la hora en el EditText
                    },
                    hour, minute, false); // 'false' para formato de 12 horas

            timePickerDialog.show();
        });

        fechaEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year1, int month1, int dayOfMonth1) {
                            // Formato la fecha seleccionada en el formato deseado
                            String selectedDate = String.format("%02d/%02d/%04d", dayOfMonth1, month1 + 1, year1);
                            fechaEditText.setText(selectedDate);
                        }
                    },
                    year, month, dayOfMonth);

            long currentDateMillis = calendar.getTimeInMillis();

            datePickerDialog.getDatePicker().setMinDate(currentDateMillis);

            datePickerDialog.show();
        });



        yapeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (efectivoCheckBox.isChecked()) {
                    efectivoCheckBox.setChecked(false);
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                LinearLayout layout = new LinearLayout(getContext());
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setPadding(50, 50, 50, 50);
                layout.setGravity(Gravity.CENTER_HORIZONTAL);

                ImageView imageView = new ImageView(getContext());
                imageView.setImageResource(R.drawable.ic_integration_in_progress);
                layout.addView(imageView);

                TextView message = new TextView(getContext());
                message.setText("ᴇꜱᴛᴇ ꜱᴇʀᴠɪᴄɪᴏ ɴᴏ ꜱᴇ ᴇɴᴄᴜᴇɴᴛʀᴀ ᴅɪꜱᴘᴏɴɪʙʟᴇ, ꜱᴇ ᴇɴᴄᴜᴇɴᴛʀᴀ ᴇɴ ᴘʀᴏᴄᴇꜱᴏ ᴅᴇ ɪɴᴛᴇɢʀᴀᴄɪÓɴ.");
                message.setTextColor(Color.RED);
                message.setTextSize(18);
                message.setPadding(0, 20, 0, 0);
                message.setGravity(Gravity.CENTER);
                layout.addView(message);

                builder.setView(layout)
                        .setCancelable(false)
                        .setPositiveButton("Aceptar", (dialog, id) -> {
                            efectivoCheckBox.setChecked(true);
                        })
                        .create()
                        .show();
            }
        });

        efectivoCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && yapeCheckBox.isChecked()) {
                yapeCheckBox.setChecked(false);
            }
        });



        Button reservarButton = view.findViewById(R.id.reservarButton);
        reservarButton.setOnClickListener(v -> {
            String fecha = fechaEditText.getText().toString().trim();
            String hora = horaEditText.getText().toString().trim();
            String horasHospedaje = horasHospedajeEditText.getText().toString().trim(); // Obtener horas de hospedaje
            String sintomas = sintomasEditText.getText().toString().trim();

            if (TextUtils.isEmpty(fecha) || TextUtils.isEmpty(hora) || mascotaSpinner.getSelectedItem() == null) {
                Toast.makeText(getContext(), "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            Mascota selectedMascota = (Mascota) mascotaSpinner.getSelectedItem();

            String idMascota = selectedMascota.getId();
            String estado = "Pendiente";

            if (isHospedaje && !TextUtils.isEmpty(horasHospedaje)) {
                crearCita(fecha, hora, idMascota, estado, sintomas, servicioId, horasHospedaje);
            } else {
                crearCita(fecha, hora, idMascota, estado, sintomas, servicioId, null);
            }
        });
        return view;
    }

    private void obtenerHistorialMascotas(String userId) {
        if (userId == null || userId.isEmpty()) {
            Log.e("API_ERROR", "El userId es nulo o vacío");
            if (getContext() != null) {
                Toast.makeText(getContext(), "El ID de usuario es inválido.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        String url = "https://api.happypetshco.com/api/MascotasUsuario=" + userId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, // GET method
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("API_RESPONSE", response.toString());
                        try {
                            JSONArray mascotasArray = response.getJSONArray("mascotas");
                            petsList.clear();  // Limpiar cualquier dato existente

                            if (mascotasArray.length() > 0) {
                                for (int i = 0; i < mascotasArray.length(); i++) {
                                    JSONObject mascota = mascotasArray.getJSONObject(i);
                                    petsList.add(new Mascota(
                                            mascota.getString("id"),
                                            mascota.getString("nombre"), // Solo almacenar el nombre
                                            mascota.getString("edad"),
                                            mascota.getString("especie"),
                                            mascota.getString("raza"),
                                            mascota.getString("sexo"),
                                            mascota.getString("estado"),
                                            mascota.getString("imagen")
                                    ));
                                }

                                ArrayAdapter<Mascota> adapter = new ArrayAdapter<Mascota>(
                                        getContext(),
                                        android.R.layout.simple_spinner_item,
                                        petsList
                                )
                                {
                                    @Override
                                    public View getDropDownView(int position, View convertView, ViewGroup parent) {
                                        View view = super.getDropDownView(position, convertView, parent);
                                        TextView textView = (TextView) view.findViewById(android.R.id.text1);
                                        textView.setText(petsList.get(position).getNombre());
                                        return view;
                                    }

                                    @Override
                                    public View getView(int position, View convertView, ViewGroup parent) {
                                        View view = super.getView(position, convertView, parent);
                                        TextView textView = (TextView) view.findViewById(android.R.id.text1);
                                        textView.setText(petsList.get(position).getNombre());
                                        return view;
                                    }
                                };
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                mascotaSpinner.setAdapter(adapter);
                            } else {
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), "No se encontraron mascotas", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("API_RESPONSE_ERROR", "Error al procesar JSON: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("API_ERROR", "Error de red: " + error.getMessage());
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error al obtener el historial de mascotas", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(getContext()).add(jsonObjectRequest);
    }

    private void crearCita(String fecha, String hora, String idMascota, String estado, String sintomas, String idServicio, String horasHospedaje) {
        String url = "https://api.happypetshco.com/api/NuevaCita";

        // Crear un objeto JSON con los datos de la cita
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("fecha", fecha);
            requestBody.put("hora", hora);
            requestBody.put("id_mascota", idMascota);
            requestBody.put("estado", estado);
            requestBody.put("sintomas", sintomas);
            requestBody.put("id_servicio", idServicio);

            if (horasHospedaje != null) {
                requestBody.put("horas_hospedaje", horasHospedaje);
            }

            // Agregar método de pago según la selección del usuario
            if (yapeCheckBox.isChecked()) {
                requestBody.put("tipo_pago", "Yape");
            } else if (efectivoCheckBox.isChecked()) {
                requestBody.put("tipo_pago", "Efectivo");
            } else {
                // Si no se seleccionó ningún método de pago, podrías manejar esto
                Toast.makeText(getContext(), "Por favor, seleccione un método de pago", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, // Método POST
                url,
                requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("API_RESPONSE", response.toString());
                        Toast.makeText(getContext(), "Cita creada exitosamente", Toast.LENGTH_SHORT).show();
                        dismiss(); // Cerrar el diálogo
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("API_ERROR", "Error de red: " + error.getMessage());
                        Toast.makeText(getContext(), "Error al crear la cita", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(getContext()).add(jsonObjectRequest);
    }
}

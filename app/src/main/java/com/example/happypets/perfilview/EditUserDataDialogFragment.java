package com.example.happypets.perfilview;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.happypets.R;

import org.json.JSONException;
import org.json.JSONObject;

public class EditUserDataDialogFragment extends DialogFragment {

    private EditText editTextUbicacion, editTextTelefono;
    private Button buttonSave, buttonCancel;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Crear el diálogo
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // Inflar el layout del formulario flotante
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_edit_user_data, null);

        editTextUbicacion = view.findViewById(R.id.editTextUbicacion);
        editTextTelefono = view.findViewById(R.id.editTextTelefono);
        buttonSave = view.findViewById(R.id.buttonSave);
        buttonCancel = view.findViewById(R.id.buttonCancel);

        // Obtener los argumentos pasados desde la actividad
        Bundle args = getArguments();
        if (args != null) {
            String userId = args.getString("userId");
            String token = args.getString("token");

            // Obtener los valores de telefono y ubicacion, si existen
            String telefono = args.getString("telefono", "");  // Valor predeterminado vacío
            String ubicacion = args.getString("ubicacion", "");  // Valor predeterminado vacío

            // Mostrar los datos actuales en los EditText
            editTextTelefono.setText(telefono);
            editTextUbicacion.setText(ubicacion);

            // Imprimir userId y token en un Toast (opcional para debug)
            String message = "UserId: " + userId + "\nToken: " + token;
        }

        // Configurar el botón de guardar
        buttonSave.setOnClickListener(v -> {
            String ubicacion = editTextUbicacion.getText().toString();
            String telefono = editTextTelefono.getText().toString();

            // Validar que los campos no estén vacíos
            if (telefono.isEmpty() || ubicacion.isEmpty()) {
                Toast.makeText(getActivity(), "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Obtener el userId y token de los argumentos
            String userId = args != null ? args.getString("userId") : null;
            String token = args != null ? args.getString("token") : null;

            if (userId != null && token != null) {
                // Llamar a la API para actualizar los datos del usuario
                updateUser(userId, telefono, ubicacion, token);
            }
        });

        // Configurar el botón de cancelar
        buttonCancel.setOnClickListener(v -> dismiss());

        // Establecer el contenido del diálogo
        dialog.setContentView(view);

        // Ajustar el tamaño del diálogo (puedes modificar estos valores)
        Window window = dialog.getWindow();
        if (window != null) {
            // Configurar el ancho del diálogo (puedes usar un porcentaje o un valor absoluto en píxeles)
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;  // Ancho completo
            params.height = WindowManager.LayoutParams.WRAP_CONTENT; // Alto ajustable según el contenido
            window.setAttributes(params);
        }

        return dialog;
    }


    private void updateUser(String userId, String telefono, String ubicacion, String token) {
        // Construir la URL de la API con el userId
        String url = "https://api.happypetshco.com/api/ActualizarUsuario=" + userId;

        // Crear el objeto JSON con los datos a actualizar
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("telefono", telefono);
            jsonBody.put("ubicacion", ubicacion);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Realizar la solicitud PUT para actualizar los datos del usuario
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, jsonBody,
                response -> {
                    // Mostrar un mensaje indicando que la actualización fue exitosa
                    Toast.makeText(getActivity(), "Datos actualizados correctamente.", Toast.LENGTH_SHORT).show();
                    dismiss(); // Cerrar el diálogo
                },
                error -> {
                    // Manejar el error
                    Toast.makeText(getActivity(), "Error al actualizar los datos.", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                // Agregar el token de autorización en los encabezados
                java.util.HashMap<String, String> headers = new java.util.HashMap<>();
                headers.put("Authorization", "Bearer " + token);  // Usar el token aquí
                return headers;
            }
        };

        // Agregar la solicitud a la cola
        queue.add(jsonObjectRequest);
    }
}

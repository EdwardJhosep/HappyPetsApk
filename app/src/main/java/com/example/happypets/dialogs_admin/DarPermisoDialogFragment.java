package com.example.happypets.dialogs_admin;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.happypets.R;
import com.example.happypets.models.User;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;

public class DarPermisoDialogFragment extends DialogFragment {
    private String token;
    private String userId;
    private User usuario;

    // Vista de los CheckBox
    private CheckBox checkAdmin;
    private CheckBox checkUsuario;
    private CheckBox checkCajero;
    private CheckBox checkVeterinario;

    // Constructor modificado
    public DarPermisoDialogFragment(String token, String userId, User usuario) {
        this.token = token;
        this.userId = userId;
        this.usuario = usuario;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_dar_permiso, container, false); // Infla el XML
        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.card_background);

        // Inicializa las vistas del diálogo
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView nombreTextView = view.findViewById(R.id.nombreTextView);
        Button darPermisoButton = view.findViewById(R.id.darPermisoButton);

        // Inicializa los CheckBox
        checkAdmin = view.findViewById(R.id.checkAdmin);
        checkUsuario = view.findViewById(R.id.checkUsuario);
        checkCajero = view.findViewById(R.id.checkCajero);
        checkVeterinario = view.findViewById(R.id.checkVeterinario);

        // Muestra el nombre del usuario en el TextView
        nombreTextView.setText(usuario.getNombres());

        // Marca los CheckBox según los permisos del usuario
        marcarCheckBoxes(usuario.getPermisos());

        // Maneja el clic en el botón darPermisoButton
        darPermisoButton.setOnClickListener(v -> {
            darPermiso(); // Llama a la función para dar permisos
            // No cerrar el diálogo aquí
        });

        return view; // Devuelve la vista inflada
    }

    private void marcarCheckBoxes(List<String> permisos) {
        // Marca los CheckBox según los permisos del usuario
        checkAdmin.setChecked(permisos.contains("Administrador"));
        checkUsuario.setChecked(permisos.contains("Usuario"));
        checkCajero.setChecked(permisos.contains("Cajero"));
        checkVeterinario.setChecked(permisos.contains("Veterinario"));
    }

    private void darPermiso() {
        // Crear un cliente OkHttp
        OkHttpClient client = new OkHttpClient();

        // Construir la URL para actualizar el usuario
        String url = "https://api.happypetshco.com/api/ActualizarUsuario=" + userId;

        // Obtener los permisos seleccionados
        StringBuilder permisosSeleccionados = new StringBuilder();
        if (checkAdmin.isChecked()) permisosSeleccionados.append("\"Administrador\",");
        if (checkUsuario.isChecked()) permisosSeleccionados.append("\"Usuario\",");
        if (checkCajero.isChecked()) permisosSeleccionados.append("\"Cajero\",");
        if (checkVeterinario.isChecked()) permisosSeleccionados.append("\"Veterinario\",");

        // Eliminar la última coma si es necesario
        if (permisosSeleccionados.length() > 0) {
            permisosSeleccionados.setLength(permisosSeleccionados.length() - 1); // Eliminar la última coma
        }

        // Crear el cuerpo de la solicitud JSON
        String json = "{\"permisos\": [" + permisosSeleccionados + "]}"; // Usar los permisos seleccionados
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));

        // Crear la solicitud
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .addHeader("Authorization", "Bearer " + token) // Agregar el token de autorización
                .build();

        // Ejecutar la solicitud
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                // Mostrar mensaje de error en la UI
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error al dar permisos", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Procesar la respuesta exitosa
                    String responseBody = response.body().string();
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Permisos actualizados", Toast.LENGTH_SHORT).show();
                        dismiss(); // Cierra el diálogo aquí
                    });
                } else {
                    // Manejar la respuesta de error
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Error: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}

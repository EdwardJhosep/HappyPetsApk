package com.example.happypets.view_cliente;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.happypets.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AgregarMascotaDialogFragment extends DialogFragment {

    private String userId;

    public static AgregarMascotaDialogFragment newInstance(String userId) {
        AgregarMascotaDialogFragment fragment = new AgregarMascotaDialogFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_agregar_mascota_dialog_fragment, container, false);

        if (getArguments() != null) {
            userId = getArguments().getString("userId");
        }

        EditText nombreEditText = view.findViewById(R.id.nombreEditText);
        EditText edadEditText = view.findViewById(R.id.edadEditText);
        EditText especieEditText = view.findViewById(R.id.especieEditText);
        EditText razaEditText = view.findViewById(R.id.razaEditText);
        EditText sexoEditText = view.findViewById(R.id.sexoEditText);
        Button agregarButton = view.findViewById(R.id.agregarButton);

        agregarButton.setOnClickListener(v -> {
            String nombre = nombreEditText.getText().toString();
            String edad = edadEditText.getText().toString();
            String especie = especieEditText.getText().toString();
            String raza = razaEditText.getText().toString();
            String sexo = sexoEditText.getText().toString();

            agregarMascota(nombre, edad, especie, raza, sexo, userId);
        });

        return view;
    }

    private void agregarMascota(String nombre, String edad, String especie, String raza, String sexo, String idUsuario) {
        new Thread(() -> {
            try {
                URL url = new URL("https://api-happypetshco-com.preview-domain.com/api/NuevaMascota");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("nombre", nombre);
                jsonObject.put("edad", edad);
                jsonObject.put("especie", especie);
                jsonObject.put("raza", raza);
                jsonObject.put("sexo", sexo);
                jsonObject.put("id_usuario", idUsuario); // Pasar el ID del usuario

                OutputStream os = conn.getOutputStream();
                os.write(jsonObject.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_CREATED) {
                    // Mascota agregada exitosamente
                    requireActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Mascota registrada exitosamente", Toast.LENGTH_SHORT).show());
                    dismiss(); // Cerrar el diálogo
                } else {
                    // Manejar errores
                    requireActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Error al registrar la mascota", Toast.LENGTH_SHORT).show());
                }
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}

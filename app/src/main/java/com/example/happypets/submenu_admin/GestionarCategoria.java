package com.example.happypets.submenu_admin;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.happypets.R;

import org.json.JSONObject;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GestionarCategoria extends Fragment {

    private String token;
    private EditText etCategoriaNombre;
    private Button btnAgregarCategoria;

    // Método para recibir el token desde la actividad
    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_gestionar_categoria, container, false);

        etCategoriaNombre = view.findViewById(R.id.categoria_edit_text);
        btnAgregarCategoria = view.findViewById(R.id.agregar_categoria_button);

        if (token == null) {
            Toast.makeText(getContext(), "Token no recibido", Toast.LENGTH_SHORT).show();
            Log.e("GestionarCategoria", "Token no recibido");
        }

        btnAgregarCategoria.setOnClickListener(v -> {
            String nombreCategoria = etCategoriaNombre.getText().toString().trim();
            if (!nombreCategoria.isEmpty()) {
                // Asegura que solo la primera letra esté en mayúscula
                nombreCategoria = capitalizeFirstLetter(nombreCategoria);
                verificarCategoriaExistente(nombreCategoria);
            } else {
                Toast.makeText(getContext(), "Por favor, ingrese un nombre para la categoría", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    private void verificarCategoriaExistente(String nombreCategoria) {
        new VerificarCategoriaTask().execute(nombreCategoria);
    }

    private void agregarCategoria(String nombreCategoria) {
        new AgregarCategoriaTask().execute(nombreCategoria);
    }

    // Clase AsyncTask para verificar si la categoría ya existe
    private class VerificarCategoriaTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            String nombreCategoria = params[0];
            try {
                URL url = new URL("https://api.happypetshco.com/api/Categorias/exists?nombre=" + nombreCategoria);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + token);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Si la categoría existe, devolver true
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();

                    JSONObject jsonResponse = new JSONObject(content.toString());
                    return jsonResponse.getBoolean("exists");
                } else {
                    return false;
                }
            } catch (Exception e) {
                Log.e("GestionarCategoria", "Error al verificar la categoría", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean categoriaExistente) {
            if (categoriaExistente) {
                Toast.makeText(getContext(), "La categoría ya existe", Toast.LENGTH_SHORT).show();
            } else {
                String nombreCategoria = etCategoriaNombre.getText().toString().trim();
                agregarCategoria(nombreCategoria);
            }
        }
    }

    // Clase AsyncTask para agregar la categoría
    private class AgregarCategoriaTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String nombreCategoria = params[0];
            try {
                URL url = new URL("https://api.happypetshco.com/api/Categorias");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + token);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("nombre", nombreCategoria);

                OutputStream os = connection.getOutputStream();
                os.write(jsonBody.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_CREATED) {
                    return "Categoría agregada exitosamente";
                } else {
                    return "Categoria" + responseCode;
                }

            } catch (Exception e) {
                Log.e("GestionarCategoria", "Error en la solicitud", e);
                return "Error en la conexión";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getContext(), result, Toast.LENGTH_SHORT).show();

            if (result.equals("Categoría agregada exitosamente")) {
                // Limpiar el formulario después de agregar la categoría
                etCategoriaNombre.setText("");
            }
        }
    }
}
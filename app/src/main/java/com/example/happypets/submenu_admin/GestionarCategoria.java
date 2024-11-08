package com.example.happypets.submenu_admin;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.happypets.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GestionarCategoria extends Fragment {

    private String token;
    private EditText etCategoriaNombre, etSubcategoriaNombre, etSubSubcategoriaNombre;
    private Button btnAgregarCategoria, btnAgregarSubcategoria, btnAgregarSubSubcategoria;
    private Spinner spCategoria, spSubcategoria;

    private Map<String, String> categoriasMap = new HashMap<>(); // Mapa de categorías con id y nombre
    private Map<String, String> subcategoriasMap = new HashMap<>(); // Mapa de subcategorías con id y nombre

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_gestionar_categoria, container, false);

        etCategoriaNombre = view.findViewById(R.id.categoria_edit_text);
        btnAgregarCategoria = view.findViewById(R.id.agregar_categoria_button);
        etSubcategoriaNombre = view.findViewById(R.id.subcategoria_edit_text);
        btnAgregarSubcategoria = view.findViewById(R.id.agregar_subcategoria_button);
        etSubSubcategoriaNombre = view.findViewById(R.id.subsubcategoria_edit_text);
        btnAgregarSubSubcategoria = view.findViewById(R.id.agregar_subsubcategoria_button);
        spCategoria = view.findViewById(R.id.categoria_spinner);
        spSubcategoria = view.findViewById(R.id.subcategoria_spinner);

        if (token == null) {
            Toast.makeText(getContext(), "Token no recibido", Toast.LENGTH_SHORT).show();
            Log.e("GestionarCategoria", "Token no recibido");
        } else {
            obtenerCategorias();
            ObtenerSubcategoriasTask();// Cargar categorías al iniciar
        }

        btnAgregarCategoria.setOnClickListener(v -> {
            String nombreCategoria = etCategoriaNombre.getText().toString().trim();
            if (!nombreCategoria.isEmpty()) {
                nombreCategoria = capitalizeFirstLetter(nombreCategoria);
                agregarCategoria(nombreCategoria);
            } else {
                Toast.makeText(getContext(), "Por favor, ingrese un nombre para la categoría", Toast.LENGTH_SHORT).show();
            }
        });

        btnAgregarSubcategoria.setOnClickListener(v -> {
            String nombreSubcategoria = etSubcategoriaNombre.getText().toString().trim();
            String categoriaId = categoriasMap.get(spCategoria.getSelectedItem().toString());
            if (!nombreSubcategoria.isEmpty() && categoriaId != null) {
                nombreSubcategoria = capitalizeFirstLetter(nombreSubcategoria);
                agregarSubcategoria(nombreSubcategoria, categoriaId);
            } else {
                Toast.makeText(getContext(), "Seleccione una categoría y agregue un nombre para la subcategoría", Toast.LENGTH_SHORT).show();
            }
        });
        btnAgregarSubSubcategoria.setOnClickListener(v -> {
            String nombreSubSubcategoria = etSubSubcategoriaNombre.getText().toString().trim();
            String subcategoriaId = subcategoriasMap.get(spSubcategoria.getSelectedItem().toString());
            if (!nombreSubSubcategoria.isEmpty() && subcategoriaId != null) {
                nombreSubSubcategoria = capitalizeFirstLetter(nombreSubSubcategoria);
                agregarSubSubcategoria(nombreSubSubcategoria, subcategoriaId);
            } else {
                Toast.makeText(getContext(), "Seleccione una subcategoría y agregue un nombre para la sub-subcategoría", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void obtenerCategorias() {
        new ObtenerCategoriasTask().execute();
    }
    private void ObtenerSubcategoriasTask() {
        new ObtenerSubcategoriasTask().execute();
    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    private void agregarCategoria(String nombreCategoria) {
        new AgregarCategoriaTask().execute(nombreCategoria);
    }

    private void agregarSubcategoria(String nombreSubcategoria, String categoriaId) {
        new AgregarSubcategoriaTask().execute(nombreSubcategoria, categoriaId);
    }
    private void agregarSubSubcategoria(String nombreSubSubcategoria, String subcategoriaId) {
        new AgregarSubSubcategoriaTask().execute(nombreSubSubcategoria, subcategoriaId);
    }
    private class ObtenerCategoriasTask extends AsyncTask<Void, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            ArrayList<String> categoriasList = new ArrayList<>();
            try {
                URL url = new URL("https://api.happypetshco.com/api/ListarCategorias");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + token);

                // Verificar el código de respuesta
                int responseCode = connection.getResponseCode();
                Log.d("GestionarCategoria", "Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Leer la respuesta de la API
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Mostrar el contenido de la respuesta para depuración
                    Log.d("GestionarCategoria", "API Response: " + response.toString());

                    // Convertir el String en un objeto JSON y acceder al arreglo "categorias"
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray categoriasArray = jsonResponse.getJSONArray("categorias");

                    // Iterar sobre el arreglo y extraer los datos de cada categoría
                    for (int i = 0; i < categoriasArray.length(); i++) {
                        JSONObject categoria = categoriasArray.getJSONObject(i);
                        String id = categoria.getString("id");
                        String nombre = categoria.getString("nombre");
                        categoriasList.add(nombre);
                        categoriasMap.put(nombre, id); // Guardar id y nombre en el mapa
                    }
                } else {
                    Log.e("GestionarCategoria", "Error en la respuesta de la API: Código " + responseCode);
                }
            } catch (Exception e) {
                Log.e("GestionarCategoria", "Error al obtener categorías", e);
            }
            return categoriasList;
        }

        @Override
        protected void onPostExecute(ArrayList<String> categorias) {
            if (categorias.isEmpty()) {
                Toast.makeText(getContext(), "No se encontraron categorías", Toast.LENGTH_SHORT).show();
            } else {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, categorias);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spCategoria.setAdapter(adapter);
            }
        }
    }
    private class ObtenerSubcategoriasTask extends AsyncTask<Void, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            ArrayList<String> subcategoriasList = new ArrayList<>();
            try {
                // URL para obtener las subcategorías
                URL url = new URL("https://api.happypetshco.com/api/ListarSubCategorias");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + token);

                // Verificar el código de respuesta
                int responseCode = connection.getResponseCode();
                Log.d("GestionarSubcategoria", "Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Leer la respuesta de la API
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Mostrar el contenido de la respuesta para depuración
                    Log.d("GestionarSubcategoria", "API Response: " + response.toString());

                    // Convertir el String en un objeto JSON y acceder al arreglo "subcategorias"
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray subcategoriasArray = jsonResponse.getJSONArray("subcategorias");

                    // Iterar sobre el arreglo y extraer los datos de cada subcategoría
                    for (int i = 0; i < subcategoriasArray.length(); i++) {
                        JSONObject subcategoria = subcategoriasArray.getJSONObject(i);
                        String id = subcategoria.getString("id");
                        String nombre = subcategoria.getString("nombre");
                        subcategoriasList.add(nombre);
                        subcategoriasMap.put(nombre, id);
                    }
                } else {
                    Log.e("GestionarSubcategoria", "Error en la respuesta de la API: Código " + responseCode);
                }
            } catch (Exception e) {
                Log.e("GestionarSubcategoria", "Error al obtener subcategorías", e);
            }
            return subcategoriasList;
        }

        @Override
        protected void onPostExecute(ArrayList<String> subcategorias) {
            if (subcategorias.isEmpty()) {
                Toast.makeText(getContext(), "No se encontraron subcategorías", Toast.LENGTH_SHORT).show();
            } else {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, subcategorias);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spSubcategoria.setAdapter(adapter);
            }
        }
    }



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

                try (OutputStream os = connection.getOutputStream()) {
                    os.write(jsonBody.toString().getBytes("UTF-8"));
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_CREATED) {
                    return "Categoría agregada exitosamente";
                } else if (responseCode == HttpURLConnection.HTTP_CONFLICT) {
                    return "La categoría ya existe";
                } else {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        Log.e("GestionarCategoria", "Error: " + response.toString());
                    }
                    return "Error al agregar la categoría";
                }

            } catch (Exception e) {
                Log.e("GestionarCategoria", "Error en la solicitud", e);
                return "Error en la conexión";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getContext(), result, Toast.LENGTH_SHORT).show();
            if ("Categoría agregada exitosamente".equals(result)) {
                etCategoriaNombre.setText("");
            }
        }
    }

    private class AgregarSubcategoriaTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String nombreSubcategoria = params[0];
            String categoriaId = params[1];
            try {
                URL url = new URL("https://api.happypetshco.com/api/SubCategorias");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + token);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Crear el JSON con los nombres de parámetros que el backend espera
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("nombre", nombreSubcategoria);
                jsonBody.put("categorias_id", categoriaId); // Cambiado a "categorias_id"

                try (OutputStream os = connection.getOutputStream()) {
                    os.write(jsonBody.toString().getBytes("UTF-8"));
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_CREATED) {
                    return "Subcategoría agregada exitosamente";
                } else if (responseCode == HttpURLConnection.HTTP_CONFLICT) {
                    return "La subcategoría ya existe";
                } else {
                    return "Error al agregar la subcategoría";
                }

            } catch (Exception e) {
                Log.e("GestionarCategoria", "Error en la solicitud", e);
                return "Error en la conexión";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getContext(), result, Toast.LENGTH_SHORT).show();
            if ("Subcategoría agregada exitosamente".equals(result)) {
                etSubcategoriaNombre.setText("");
            }
        }
    }
    private class AgregarSubSubcategoriaTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String nombreSubSubcategoria = params[0];
            String subcategoriaId = params[1];

            try {
                URL url = new URL("https://api.happypetshco.com/api/SubSubCategorias");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + token);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("nombre", nombreSubSubcategoria);
                jsonBody.put("sub_categorias_id", subcategoriaId);

                try (OutputStream os = connection.getOutputStream()) {
                    os.write(jsonBody.toString().getBytes("UTF-8"));
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_CREATED) {
                    return "Sub-subcategoría agregada exitosamente";
                } else {
                    return "Error al agregar la sub-subcategoría";
                }

            } catch (Exception e) {
                Log.e("GestionarCategoria", "Error en la solicitud", e);
                return "Error en la conexión";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getContext(), result, Toast.LENGTH_SHORT).show();
            if ("Sub-subcategoría agregada exitosamente".equals(result)) {
                etSubSubcategoriaNombre.setText("");
            }
        }
    }
}
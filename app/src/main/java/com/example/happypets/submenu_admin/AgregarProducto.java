package com.example.happypets.submenu_admin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.happypets.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class AgregarProducto extends Fragment {

    private static final int REQUEST_CODE_PERMISSIONS = 1001;
    private static final int REQUEST_CODE_PICK_IMAGE = 1;
    private static final int REQUEST_CODE_PICK_IMAGE_FROM_GALLERY = 2;

    private EditText editTextNombre, editTextDescripcion, editTextCategoria, editTextPrecio, editTextStock;
    private Button buttonAgregar, buttonSeleccionarImagen, buttonSeleccionarDesdeGaleria;
    private ImageView imageViewProducto;
    private Uri uriImagen;
    private String token;
    private Spinner spinnerCategoria;
    private Map<String, String> categoriasMap = new HashMap<>();
    private List<String> categoriasList = new ArrayList<>();
    private ArrayAdapter<String> categoriaAdapter;
    private CheckBox checkBlanco, checkRojo, checkAzul, checkVerde, checkMorado, checkAmarillo, checkNegro;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_agregar_producto, container, false);

        editTextNombre = view.findViewById(R.id.editTextNombre);
        editTextDescripcion = view.findViewById(R.id.editTextDescripcion);
        spinnerCategoria = view.findViewById(R.id.categoria_spinner);
        editTextPrecio = view.findViewById(R.id.editTextPrecio);
        editTextStock = view.findViewById(R.id.editTextStock);
        buttonAgregar = view.findViewById(R.id.buttonAgregar);
        buttonSeleccionarImagen = view.findViewById(R.id.buttonSeleccionarImagen);
        buttonSeleccionarDesdeGaleria = view.findViewById(R.id.buttonSeleccionarDesdeGaleria);
        imageViewProducto = view.findViewById(R.id.imageViewProducto);

        checkBlanco = view.findViewById(R.id.checkBlanco);
        checkRojo = view.findViewById(R.id.checkRojo);
        checkAzul = view.findViewById(R.id.checkAzul);
        checkVerde = view.findViewById(R.id.checkVerde);
        checkMorado = view.findViewById(R.id.checkMorado);
        checkAmarillo = view.findViewById(R.id.checkAmarillo);
        checkNegro = view.findViewById(R.id.checkNegro);

        // Llamar a la tarea para obtener categorías
        new ObtenerCategoriasTask().execute();

        buttonSeleccionarImagen.setOnClickListener(v -> {
            if (hasCameraPermissions()) {
                seleccionarImagen();
            } else {
                requestCameraPermissions();
            }
        });

        buttonSeleccionarDesdeGaleria.setOnClickListener(v -> {
            if (hasCameraPermissions()) {
                seleccionarImagenDesdeGaleria();
            } else {
                requestCameraPermissions();
            }
        });

        buttonAgregar.setOnClickListener(v -> agregarProducto());

        return view;
    }
    public void setToken(String token) {
        this.token = token;
    }
    private boolean hasCameraPermissions() {
        return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA)) {
            Toast.makeText(getActivity(), "Se necesita el permiso de cámara para tomar una foto.", Toast.LENGTH_SHORT).show();
        }
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_PERMISSIONS);
    }

    private void seleccionarImagen() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
        }
    }

    private void seleccionarImagenDesdeGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE_FROM_GALLERY);
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
        protected void onPostExecute(ArrayList<String> result) {
            super.onPostExecute(result);
            // Actualizar el spinner con las categorías obtenidas
            categoriasList.add(0, "Selecciona una categoría"); // Agregar opción predeterminada
            categoriaAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, result);
            categoriaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategoria.setAdapter(categoriaAdapter);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && data != null) {
            if (requestCode == REQUEST_CODE_PICK_IMAGE) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    imageViewProducto.setImageBitmap(imageBitmap);
                    uriImagen = saveImageToFile(imageBitmap);
                }
            } else if (requestCode == REQUEST_CODE_PICK_IMAGE_FROM_GALLERY) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {
                        Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);
                        imageViewProducto.setImageBitmap(imageBitmap);
                        uriImagen = saveImageToFile(imageBitmap);
                    } catch (IOException e) {
                        Log.e("AgregarProducto", "Error al obtener la imagen de la galería", e);
                    }
                }
            }
        }
    }

    private Uri saveImageToFile(Bitmap bitmap) {
        try {
            File imageFile = new File(getActivity().getExternalFilesDir(null), "imagen_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            return Uri.fromFile(imageFile);
        } catch (IOException e) {
            Log.e("AgregarProducto", "Error al guardar la imagen", e);
            return null;
        }
    }

    private void agregarProducto() {
        String nombre = editTextNombre.getText().toString().trim();
        String descripcion = editTextDescripcion.getText().toString().trim();
        String categoria = (String) spinnerCategoria.getSelectedItem();  // Obtener el nombre de la categoría
        String precio = editTextPrecio.getText().toString().trim();
        String stock = editTextStock.getText().toString().trim();

        // Verificar que los campos no estén vacíos
        if (nombre.isEmpty() || descripcion.isEmpty() || categoria.equals("Selecciona una categoría") || precio.isEmpty() || stock.isEmpty()) {
            Toast.makeText(getActivity(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener el ID de la categoría seleccionada utilizando el mapa categoriasMap
        String categoriaId = categoriasMap.get(categoria);

        if (categoriaId == null) {
            Toast.makeText(getActivity(), "Categoría no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        // Llamar a la tarea para agregar el producto
        new AgregarProductoTask(nombre, descripcion, categoriaId, precio, stock).execute();
    }

    private class AgregarProductoTask extends AsyncTask<Void, Void, String> {
        private final String nombre, descripcion, categoriaId, precio, stock;

        public AgregarProductoTask(String nombre, String descripcion, String categoriaId, String precio, String stock) {
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.categoriaId = categoriaId;
            this.precio = precio;
            this.stock = stock;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                // Preparar el cuerpo de la solicitud con los parámetros
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("nombre", nombre)
                        .addFormDataPart("descripcion", descripcion)
                        .addFormDataPart("categoria", categoriaId)  // Usar el ID de la categoría
                        .addFormDataPart("precio", precio)
                        .addFormDataPart("stock", stock)
                        .addFormDataPart("imagen", "imagen.jpg", RequestBody.create(MediaType.parse("image/jpeg"), new File(uriImagen.getPath())))
                        .build();

                // Crear la solicitud HTTP
                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                OkHttpClient client = new OkHttpClient.Builder().addInterceptor(loggingInterceptor).build();

                Request request = new Request.Builder()
                        .url("https://api.happypetshco.com/api/AgregarProducto")
                        .post(requestBody)
                        .addHeader("Authorization", "Bearer " + token)
                        .build();

                // Realizar la solicitud y obtener la respuesta
                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    clearFields();
                    return "Producto agregado con éxito";
                } else {
                    return "Error al agregar producto: " + response.message();
                }

            } catch (Exception e) {
                Log.e("AgregarProducto", "Error al agregar producto", e);
                return "Error al agregar producto";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
        }
    }



    private void clearFields() {
        editTextNombre.setText("");
        editTextDescripcion.setText("");
        editTextPrecio.setText("");
        editTextStock.setText("");
        spinnerCategoria.setSelection(0);
        checkBlanco.setChecked(false);
        checkRojo.setChecked(false);
        checkAzul.setChecked(false);
        checkVerde.setChecked(false);
        checkMorado.setChecked(false);
        checkAmarillo.setChecked(false);
        checkNegro.setChecked(false);
        imageViewProducto.setImageResource(0);
    }
}

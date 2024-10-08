package com.example.happypets.submenu_admin;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.happypets.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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

    private EditText editTextNombre, editTextDescripcion, editTextCategoria, editTextPrecio, editTextStock;
    private Button buttonAgregar, buttonSeleccionarImagen;
    private ImageView imageViewProducto;
    private Uri uriImagen;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_agregar_producto, container, false);

        // Inicializar los elementos de la interfaz
        editTextNombre = view.findViewById(R.id.editTextNombre);
        editTextDescripcion = view.findViewById(R.id.editTextDescripcion);
        editTextCategoria = view.findViewById(R.id.editTextCategoria);
        editTextPrecio = view.findViewById(R.id.editTextPrecio);
        editTextStock = view.findViewById(R.id.editTextStock);
        buttonAgregar = view.findViewById(R.id.buttonAgregar);
        buttonSeleccionarImagen = view.findViewById(R.id.buttonSeleccionarImagen);
        imageViewProducto = view.findViewById(R.id.imageViewProducto);

        // Asignar listeners a los botones
        buttonSeleccionarImagen.setOnClickListener(v -> {
            if (hasCameraPermissions()) {
                seleccionarImagen();
            } else {
                requestCameraPermissions();
            }
        });

        buttonAgregar.setOnClickListener(v -> agregarProducto());

        return view;
    }

    // Método para verificar si los permisos de cámara están concedidos
    private boolean hasCameraPermissions() {
        return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    // Método para solicitar permisos de cámara
    private void requestCameraPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA)) {
            Toast.makeText(getActivity(), "Se necesita el permiso de cámara para tomar una foto.", Toast.LENGTH_SHORT).show();
        }
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_PERMISSIONS);
    }

    // Método para seleccionar una imagen usando la cámara
    private void seleccionarImagen() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
        }
    }

    // Obtener el resultado de la captura de imagen
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == getActivity().RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                imageViewProducto.setImageBitmap(imageBitmap); // Muestra la imagen capturada
                uriImagen = saveImageToFile(imageBitmap); // Guarda la imagen en un archivo
                Log.d("AgregarProducto", "Imagen capturada desde la cámara");
            }
        }
    }

    // Método para guardar la imagen en un archivo y obtener su URI
    private Uri saveImageToFile(Bitmap bitmap) {
        try {
            // Crea un archivo para la imagen
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

    // Método para agregar un producto
    // Método para agregar un producto
    private void agregarProducto() {
        final String nmProducto = editTextNombre.getText().toString().trim();
        final String descripcion = editTextDescripcion.getText().toString().trim();
        final String categoria = editTextCategoria.getText().toString().trim();
        final String precio = editTextPrecio.getText().toString().trim();
        final String stock = editTextStock.getText().toString().trim();

        // Validar que los campos no estén vacíos
        if (nmProducto.isEmpty() || descripcion.isEmpty() || categoria.isEmpty() ||
                precio.isEmpty() || stock.isEmpty() || uriImagen == null) {
            Toast.makeText(getContext(), "Por favor completa todos los campos y selecciona una imagen", Toast.LENGTH_SHORT).show();
            Log.e("AgregarProducto", "Campos vacíos o imagen no seleccionada");
            return;
        }

        new Thread(() -> {
            try {
                Log.d("AgregarProducto", "Iniciando el proceso de agregar producto");

                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                OkHttpClient client = new OkHttpClient.Builder()
                        .addInterceptor(loggingInterceptor)
                        .build();

                MultipartBody.Builder builder = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("nm_producto", nmProducto)
                        .addFormDataPart("descripcion", descripcion)
                        .addFormDataPart("categoria", categoria)
                        .addFormDataPart("precio", precio)
                        .addFormDataPart("stock", stock)
                        .addFormDataPart("imagen", uriImagen.getLastPathSegment(), RequestBody.create(MediaType.parse("image/jpeg"), new File(uriImagen.getPath())));

                RequestBody requestBody = builder.build();
                String url = "https://api-happypetshco-com.preview-domain.com/api/NuevoProducto";
                Log.d("AgregarProducto", "URL de la solicitud: " + url);

                Request request = new Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build();

                Log.d("AgregarProducto", "Realizando la solicitud al servidor");
                try (Response response = client.newCall(request).execute()) {
                    Log.d("AgregarProducto", "Respuesta del servidor: " + response.code());
                    if (response.isSuccessful()) {
                        Log.d("AgregarProducto", "Producto agregado correctamente");
                        getActivity().runOnUiThread(() -> {
                            // Limpiar los campos y la imagen
                            limpiarCampos();
                            Toast.makeText(getContext(), "Producto agregado correctamente", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        String errorResponse = response.body() != null ? response.body().string() : "Sin respuesta del servidor";
                        Log.e("AgregarProducto", "Error al agregar el producto: " + errorResponse);
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Error al agregar el producto: " + errorResponse, Toast.LENGTH_SHORT).show());
                    }
                }
            } catch (IOException e) {
                Log.e("AgregarProducto", "Error en la conexión", e);
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error en la conexión: " + e.getMessage() + ", Causa: " + e.getCause(), Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                Log.e("AgregarProducto", "Error inesperado", e);
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error inesperado: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // Método para limpiar los campos de entrada y la imagen
    private void limpiarCampos() {
        editTextNombre.setText("");
        editTextDescripcion.setText("");
        editTextCategoria.setText("");
        editTextPrecio.setText("");
        editTextStock.setText("");
        imageViewProducto.setImageDrawable(null); // Limpiar la imagen
        uriImagen = null; // Resetear el URI de la imagen
    }


    // Manejar la respuesta de permisos solicitados
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                seleccionarImagen();
            } else {
                Toast.makeText(getActivity(), "Permiso denegado. No se puede tomar una foto.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

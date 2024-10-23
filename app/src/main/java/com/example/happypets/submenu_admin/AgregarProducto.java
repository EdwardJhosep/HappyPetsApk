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
import android.widget.CheckBox;
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
    private String token;

    private CheckBox checkBlanco, checkRojo, checkAzul, checkVerde, checkMorado, checkAmarillo, checkNegro;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_agregar_producto, container, false);

        editTextNombre = view.findViewById(R.id.editTextNombre);
        editTextDescripcion = view.findViewById(R.id.editTextDescripcion);
        editTextCategoria = view.findViewById(R.id.editTextCategoria);
        editTextPrecio = view.findViewById(R.id.editTextPrecio);
        editTextStock = view.findViewById(R.id.editTextStock);
        buttonAgregar = view.findViewById(R.id.buttonAgregar);
        buttonSeleccionarImagen = view.findViewById(R.id.buttonSeleccionarImagen);
        imageViewProducto = view.findViewById(R.id.imageViewProducto);

        checkBlanco = view.findViewById(R.id.checkBlanco);
        checkRojo = view.findViewById(R.id.checkRojo);
        checkAzul = view.findViewById(R.id.checkAzul);
        checkVerde = view.findViewById(R.id.checkVerde);
        checkMorado = view.findViewById(R.id.checkMorado);
        checkAmarillo = view.findViewById(R.id.checkAmarillo);
        checkNegro = view.findViewById(R.id.checkNegro);

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == getActivity().RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                imageViewProducto.setImageBitmap(imageBitmap);
                uriImagen = saveImageToFile(imageBitmap);
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

    public void setToken(String token) {
        this.token = token;
    }

    private void agregarProducto() {
        final String nmProducto = editTextNombre.getText().toString().trim();
        final String descripcion = editTextDescripcion.getText().toString().trim();
        final String categoria = editTextCategoria.getText().toString().trim();
        final String precio = editTextPrecio.getText().toString().trim();
        final String stock = editTextStock.getText().toString().trim();

        StringBuilder coloresSeleccionados = new StringBuilder();
        if (checkBlanco.isChecked()) coloresSeleccionados.append("Blanco,");
        if (checkRojo.isChecked()) coloresSeleccionados.append("Rojo,");
        if (checkAzul.isChecked()) coloresSeleccionados.append("Azul,");
        if (checkVerde.isChecked()) coloresSeleccionados.append("Verde,");
        if (checkMorado.isChecked()) coloresSeleccionados.append("Morado,");
        if (checkAmarillo.isChecked()) coloresSeleccionados.append("Amarillo,");
        if (checkNegro.isChecked()) coloresSeleccionados.append("Negro,");

        if (coloresSeleccionados.length() > 0) {
            coloresSeleccionados.setLength(coloresSeleccionados.length() - 2);
        }

        if (nmProducto.isEmpty() || descripcion.isEmpty() || categoria.isEmpty() ||
                precio.isEmpty() || stock.isEmpty() || coloresSeleccionados.length() == 0 || uriImagen == null) {
            Toast.makeText(getContext(), "Por favor completa todos los campos y selecciona una imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
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
                        .addFormDataPart("colores", coloresSeleccionados.toString())
                        .addFormDataPart("imagen", uriImagen.getLastPathSegment(), RequestBody.create(MediaType.parse("image/jpeg"), new File(uriImagen.getPath())));

                RequestBody requestBody = builder.build();
                String url = "https://api-happypetshco-com.preview-domain.com/api/NuevoProducto";

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer " + token)
                        .post(requestBody)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        getActivity().runOnUiThread(() -> {
                            limpiarCampos();
                            Toast.makeText(getContext(), "Producto agregado correctamente", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        String errorResponse = response.body() != null ? response.body().string() : "Sin respuesta del servidor";
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Error al agregar el producto: " + errorResponse, Toast.LENGTH_SHORT).show());
                    }
                }
            } catch (IOException e) {
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error en la conexión: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error inesperado: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void limpiarCampos() {
        editTextNombre.setText("");
        editTextDescripcion.setText("");
        editTextCategoria.setText("");
        editTextPrecio.setText("");
        editTextStock.setText("");
        checkBlanco.setChecked(false);
        checkRojo.setChecked(false);
        checkAzul.setChecked(false);
        checkVerde.setChecked(false);
        checkMorado.setChecked(false);
        checkAmarillo.setChecked(false);
        checkNegro.setChecked(false);
        imageViewProducto.setImageDrawable(null);
        uriImagen = null;
    }

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

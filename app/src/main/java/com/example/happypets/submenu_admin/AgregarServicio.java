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
import android.widget.ImageButton;
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

public class AgregarServicio extends Fragment {

    private static final int REQUEST_CODE_PERMISSIONS = 1001;
    private static final int REQUEST_CODE_PICK_IMAGE = 1;
    private static final int REQUEST_CODE_PICK_IMAGE_FROM_GALLERY = 2;

    private EditText editTextTipo, editTextDescripcion;
    private Button buttonAgregar;
    private ImageButton buttonSeleccionarImagen, buttonSeleccionarDesdeGaleria;
    private ImageView imageViewServicio;
    private Uri uriImagen;
    private String token;

    // Método estático para crear una nueva instancia del fragmento y pasar el token
    public static AgregarServicio newInstance(String token) {
        AgregarServicio fragment = new AgregarServicio();
        Bundle args = new Bundle();
        args.putString("token", token);  // Guardamos el token en el Bundle
        fragment.setArguments(args);  // Pasamos el Bundle al fragmento
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_agregar_servicio, container, false);

        // Recuperar el token desde los argumentos
        Bundle args = getArguments();
        if (args != null) {
            token = args.getString("token"); // Guardamos el token aquí
        }

        // Inicializar las vistas
        editTextTipo = view.findViewById(R.id.editTextTipo);
        editTextDescripcion = view.findViewById(R.id.editTextDescripcion);
        buttonAgregar = view.findViewById(R.id.buttonAgregar);
        buttonSeleccionarImagen = view.findViewById(R.id.buttonSeleccionarImagen);
        buttonSeleccionarDesdeGaleria = view.findViewById(R.id.buttonSeleccionarDesdeGaleria);
        imageViewServicio = view.findViewById(R.id.imageViewServicio);

        // Configuración de botones y listeners
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

        buttonAgregar.setOnClickListener(v -> agregarServicio());

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

    private void seleccionarImagenDesdeGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE_FROM_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && data != null) {
            if (requestCode == REQUEST_CODE_PICK_IMAGE) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    imageViewServicio.setImageBitmap(imageBitmap);
                    uriImagen = saveImageToFile(imageBitmap);
                }
            } else if (requestCode == REQUEST_CODE_PICK_IMAGE_FROM_GALLERY) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {
                        Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);
                        imageViewServicio.setImageBitmap(imageBitmap);
                        uriImagen = saveImageToFile(imageBitmap); // Guarda la imagen como archivo
                    } catch (IOException e) {
                        Log.e("AgregarServicio", "Error al obtener la imagen de la galería", e);
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
            Log.e("AgregarServicio", "Error al guardar la imagen", e);
            return null;
        }
    }

    private void agregarServicio() {
        final String tipo = editTextTipo.getText().toString().trim();
        final String descripcion = editTextDescripcion.getText().toString().trim();

        if (tipo.isEmpty() || descripcion.isEmpty() || uriImagen == null) {
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
                        .addFormDataPart("tipo", tipo)
                        .addFormDataPart("descripcion", descripcion);

                // Aquí se usa el URI directamente para el archivo de la imagen
                if (uriImagen != null) {
                    builder.addFormDataPart("imagen", getFileName(uriImagen), RequestBody.create(MediaType.parse("image/jpeg"), new File(uriImagen.getPath())));
                }

                RequestBody requestBody = builder.build();
                String url = "https://api.happypetshco.com/api/NuevoServicio";

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer " + token) // Usar el token aquí
                        .post(requestBody)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        getActivity().runOnUiThread(() -> {
                            limpiarCampos();
                            Toast.makeText(getContext(), "Servicio agregado correctamente", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        String errorResponse = response.body() != null ? response.body().string() : "Sin respuesta del servidor";
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error al agregar servicio: " + errorResponse, Toast.LENGTH_SHORT).show());
                    }
                }
            } catch (Exception e) {
                Log.e("AgregarServicio", "Error al agregar servicio", e);
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error al agregar servicio", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void limpiarCampos() {
        editTextTipo.setText("");
        editTextDescripcion.setText("");
        imageViewServicio.setImageResource(0); // Limpiar imagen
        uriImagen = null; // Resetear URI
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}

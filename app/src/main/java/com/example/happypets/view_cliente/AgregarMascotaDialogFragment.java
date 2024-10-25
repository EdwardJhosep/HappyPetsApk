package com.example.happypets.view_cliente;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

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

public class AgregarMascotaDialogFragment extends DialogFragment {

    private static final int REQUEST_IMAGE_CAPTURE = 1; // Código de solicitud para tomar una foto

    private String userId;
    private String token;

    private EditText etNombre, etEdad, etEspecie, etRaza, etSexo;
    private ImageView ivImagen;
    private Button btnAgregarMascota, btnSeleccionarImagen;
    private Uri imagenUri;

    public static AgregarMascotaDialogFragment newInstance(String userId, String token) {
        AgregarMascotaDialogFragment fragment = new AgregarMascotaDialogFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("token", token);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_agregar_mascota_dialog_fragment, container, false);

        // Inicializar vistas
        etNombre = view.findViewById(R.id.etNombre);
        etEdad = view.findViewById(R.id.etEdad);
        etEspecie = view.findViewById(R.id.etEspecie);
        etRaza = view.findViewById(R.id.etRaza);
        etSexo = view.findViewById(R.id.etSexo);
        ivImagen = view.findViewById(R.id.ivImagen);
        btnAgregarMascota = view.findViewById(R.id.btnAgregarMascota);
        btnSeleccionarImagen = view.findViewById(R.id.tomarfot);

        // Obtener los argumentos
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
            token = getArguments().getString("token");
        }

        // Configurar el botón para agregar mascota
        btnAgregarMascota.setOnClickListener(v -> agregarMascota());

        // Configurar el botón para seleccionar imagen
        btnSeleccionarImagen.setOnClickListener(v -> tomarFoto());

        return view;
    }

    private void agregarMascota() {
        String nombre = etNombre.getText().toString().trim();
        String edad = etEdad.getText().toString().trim();
        String especie = etEspecie.getText().toString().trim();
        String raza = etRaza.getText().toString().trim();
        String sexo = etSexo.getText().toString().trim();

        if (nombre.isEmpty() || edad.isEmpty() || especie.isEmpty() || raza.isEmpty() || sexo.isEmpty() || imagenUri == null) {
            Toast.makeText(getContext(), "Por favor completa todos los campos, incluyendo la imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar un diálogo de progreso
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Agregando mascota...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Crear el cliente HTTP
        OkHttpClient client = new OkHttpClient();

        // Crear el cuerpo de la solicitud
        File file = new File(imagenUri.getPath());
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("nombre", nombre)
                .addFormDataPart("edad", edad)
                .addFormDataPart("especie", especie)
                .addFormDataPart("raza", raza)
                .addFormDataPart("sexo", sexo)
                .addFormDataPart("id_usuario", userId)
                .addFormDataPart("imagen", file.getName(), RequestBody.create(MediaType.parse("image/*"), file))
                .build();

        // Crear la solicitud
        Request request = new Request.Builder()
                .url("https://api-happypetshco-com.preview-domain.com/api/NuevaMascota")

                .post(requestBody)
                .addHeader("Authorization", "Bearer " + token) // Agregar token de autorización
                .build();

        // Realizar la llamada asíncrona
        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    // Mostrar mensaje de éxito
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Mascota agregada exitosamente", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        dismiss(); // Cerrar el diálogo
                    });
                } else {
                    // Mostrar mensaje de error con detalles
                    String errorResponse = response.body().string(); // Captura la respuesta del error
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error al agregar la mascota: " + errorResponse, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error de conexión: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                });
            }
        }).start();
    }

    private void tomarFoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ivImagen.setImageBitmap(imageBitmap);

            // Guardar la imagen en un archivo y obtener la URI correspondiente
            try {
                // Crea un archivo temporal para almacenar la imagen
                File file = new File(getContext().getExternalFilesDir(null), "temp_image.jpg");
                FileOutputStream fos = new FileOutputStream(file);
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
                imagenUri = Uri.fromFile(file); // Guarda la URI de la imagen en el archivo
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error al guardar la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            // Ajustar el tamaño del diálogo
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            // O puedes establecer un tamaño específico
            // getDialog().getWindow().setLayout(600, ViewGroup.LayoutParams.WRAP_CONTENT); // 600 píxeles de ancho
        }
    }
}


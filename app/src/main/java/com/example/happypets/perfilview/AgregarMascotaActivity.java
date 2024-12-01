package com.example.happypets.perfilview;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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

import android.content.pm.PackageManager;

public class AgregarMascotaActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1; // Código de solicitud para tomar una foto
    private static final int REQUEST_PERMISSION_CAMERA = 100; // Código para solicitud de permisos

    private String userId;
    private String token;

    private EditText etNombre, etEspecie, etRaza;
    private ImageView ivImagen;
    private Button btnAgregarMascota, btnSeleccionarImagen;
    private Uri imagenUri;

    private Spinner spEdad;  // Spinner para seleccionar la unidad de edad
    private EditText etEdad;

    private String edadUnidad = "Años"; // Por defecto, la unidad de edad es "Años"

    private Spinner spSexo;  // Spinner para seleccionar el sexo de la mascota
    private String sexo = "Macho"; // Valor por defecto

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_mascota);

        // Inicializar vistas
        etNombre = findViewById(R.id.etNombre);
        etEdad = findViewById(R.id.etEdad);
        etEspecie = findViewById(R.id.etEspecie);
        etRaza = findViewById(R.id.etRaza);
        ivImagen = findViewById(R.id.ivImagen);
        btnAgregarMascota = findViewById(R.id.btnAgregarMascota);
        btnSeleccionarImagen = findViewById(R.id.tomarfot);
        spEdad = findViewById(R.id.spEdad);  // Inicializar el Spinner para edad
        spSexo = findViewById(R.id.spSexo);  // Inicializar el Spinner para sexo

        // Configurar el Spinner para la unidad de edad
        ArrayAdapter<CharSequence> adapterEdad = ArrayAdapter.createFromResource(this,
                R.array.edad_unidades, android.R.layout.simple_spinner_item);
        adapterEdad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spEdad.setAdapter(adapterEdad);

        spEdad.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                edadUnidad = parentView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // No se hace nada si no se selecciona nada
            }
        });

        // Configurar el Spinner para el sexo de la mascota
        ArrayAdapter<CharSequence> adapterSexo = ArrayAdapter.createFromResource(this,
                R.array.sexo_mascota, android.R.layout.simple_spinner_item);
        adapterSexo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSexo.setAdapter(adapterSexo);

        spSexo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                sexo = parentView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // No se hace nada si no se selecciona nada
            }
        });

        // Obtener los datos del Intent
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        token = intent.getStringExtra("token");

        // Comprobar permisos de cámara
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Si no tenemos el permiso, lo solicitamos
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_CAMERA);
        }

        // Configurar el botón para agregar mascota
        btnAgregarMascota.setOnClickListener(v -> agregarMascota());

        // Configurar el botón para seleccionar imagen
        btnSeleccionarImagen.setOnClickListener(v -> tomarFoto());
    }

    private void agregarMascota() {
        String nombre = etNombre.getText().toString().trim();
        String edad = etEdad.getText().toString().trim();
        String especie = etEspecie.getText().toString().trim();
        String raza = etRaza.getText().toString().trim();

        if (nombre.isEmpty() || edad.isEmpty() || especie.isEmpty() || raza.isEmpty() || imagenUri == null) {
            Toast.makeText(this, "Por favor completa todos los campos, incluyendo la imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear el AlertDialog de confirmación
        new android.app.AlertDialog.Builder(this)
                .setMessage("¿Está seguro de agregar esta mascota? Recuerde revisar los datos ingresados.")
                .setCancelable(false)  // No permite cerrar la alerta tocando fuera de ella
                .setPositiveButton("Sí", (dialog, id) -> {
                    // Si el usuario confirma, proceder con la solicitud de agregar la mascota
                    mostrarDialogoProgreso();
                })
                .setNegativeButton("No", (dialog, id) -> {
                    // Si el usuario cancela, no hacer nada
                    dialog.dismiss();
                })
                .show();
    }

    private void mostrarDialogoProgreso() {
        // Mostrar un diálogo de progreso
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Agregando mascota...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Crear el cliente HTTP
        OkHttpClient client = new OkHttpClient();

        // Crear el cuerpo de la solicitud
        File file = new File(imagenUri.getPath());
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("nombre", etNombre.getText().toString())
                .addFormDataPart("edad", etEdad.getText().toString() + " " + edadUnidad)  // Se agrega la unidad de la edad
                .addFormDataPart("especie", etEspecie.getText().toString())
                .addFormDataPart("raza", etRaza.getText().toString())
                .addFormDataPart("sexo", sexo)  // Se agrega el sexo seleccionado
                .addFormDataPart("id_usuario", userId)
                .addFormDataPart("imagen", file.getName(), RequestBody.create(MediaType.parse("image/*"), file))
                .build();

        // Crear la solicitud
        Request request = new Request.Builder()
                .url("https://api.happypetshco.com/api/NuevaMascota")
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + token) // Agregar token de autorización
                .build();

        // Realizar la llamada asíncrona
        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    // Mostrar mensaje de éxito
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Mascota agregada exitosamente", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        finish(); // Cerrar la actividad
                    });
                } else {
                    // Mostrar mensaje de error con detalles
                    String errorResponse = response.body().string(); // Captura la respuesta del error
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Error al agregar la mascota: " + errorResponse, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error de conexión: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                });
            }
        }).start();
    }


    private void tomarFoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ivImagen.setImageBitmap(imageBitmap);

            // Guardar la imagen en un archivo y obtener la URI correspondiente
            try {
                // Crea un archivo temporal para almacenar la imagen
                File file = new File(getExternalFilesDir(null), "temp_image.jpg");
                FileOutputStream fos = new FileOutputStream(file);
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
                imagenUri = Uri.fromFile(file); // Guarda la URI de la imagen en el archivo
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al guardar la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }



@Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // El permiso fue concedido, puedes tomar fotos
                Toast.makeText(this, "Permiso de cámara concedido", Toast.LENGTH_SHORT).show();
            } else {
                // El permiso fue denegado, puedes mostrar un mensaje
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

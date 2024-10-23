package com.example.happypets.adapters_admin;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.happypets.R;
import com.example.happypets.models.Producto;
import com.example.happypets.submenu_admin.Submenu_AdminProductos;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FormularioEditarAdapter {
    private final Context context;
    private final Producto producto;
    private final String token;

    public FormularioEditarAdapter(Context context, Producto producto, String token) {
        this.context = context;
        this.producto = producto;
        this.token = token;
    }

    public void showEditDialog() {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.formulario_editar_producto, null);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setView(dialogView);

        EditText editarNombre = dialogView.findViewById(R.id.editarNombre);
        EditText editarDescripcion = dialogView.findViewById(R.id.editarDescripcion);
        EditText editarPrecio = dialogView.findViewById(R.id.editarPrecio);
        EditText editarDescuento = dialogView.findViewById(R.id.editarDescuento);
        EditText editarStock = dialogView.findViewById(R.id.editarStock);
        Button botonGuardar = dialogView.findViewById(R.id.botonGuardar);
        Button botonEliminar = dialogView.findViewById(R.id.botonEliminar);

        // Rellenar campos con los datos actuales del producto
        editarNombre.setText(producto.getNombre());
        editarDescripcion.setText(producto.getDescripcion());
        editarPrecio.setText(producto.getPrecio());
        editarDescuento.setText(producto.getDescuento());
        editarStock.setText(producto.getStock());

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        // Listener para guardar cambios
        botonGuardar.setOnClickListener(v -> {
            String nuevoNombre = editarNombre.getText().toString();
            String nuevaDescripcion = editarDescripcion.getText().toString();
            String nuevoPrecio = editarPrecio.getText().toString();
            String nuevoDescuento = editarDescuento.getText().toString();
            String nuevoStock = editarStock.getText().toString();

            if (areFieldsValid(nuevoNombre, nuevaDescripcion, nuevoPrecio, nuevoStock)) {
                editarProductoEnAPI(nuevoNombre, nuevaDescripcion, nuevoPrecio, nuevoDescuento, nuevoStock);
            } else {
                Toast.makeText(context, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            }
        });

        // Listener para eliminar producto
        botonEliminar.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Confirmar Eliminación")
                    .setMessage("¿Estás seguro de que deseas eliminar este producto?")
                    .setPositiveButton("Eliminar", (dialog1, which) -> {
                        eliminarProductoEnAPI(producto.getId());
                        dialog1.dismiss();
                    })
                    .setNegativeButton("Cancelar", (dialog12, which) -> dialog12.cancel())
                    .show();
        });
    }

    private boolean areFieldsValid(String nombre, String descripcion, String precio, String stock) {
        return !TextUtils.isEmpty(nombre) && !TextUtils.isEmpty(descripcion) &&
                !TextUtils.isEmpty(precio) && !TextUtils.isEmpty(stock);
    }

    private void editarProductoEnAPI(String nombre, String descripcion, String precio, String descuento, String stock) {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("nm_producto", nombre);
            jsonObject.put("descripcion", descripcion);
            jsonObject.put("categoria", producto.getCategoria());
            jsonObject.put("precio", precio);
            jsonObject.put("descuento", descuento);
            jsonObject.put("colores", producto.getColores());
            jsonObject.put("stock", stock);

            // Imprimir solo la categoría y el ID en un Toast
            String categoria = producto.getCategoria();
            int id = producto.getId();
            showToast("Categoría: " + categoria + ", ID: " + id +"token"+token);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error al crear JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("FormularioEditarAdapter", "JSON Body: " + jsonObject.toString());

        Request request = new Request.Builder()
                .url("https://api-happypetshco-com.preview-domain.com/api/ActualizarProducto=" + producto.getId())
                .put(RequestBody.create(mediaType, jsonObject.toString()))
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                showToast("Error al editar producto");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    showToast("Producto editado correctamente");
                } else {
                    String errorMessage = obtenerMensajeError(response);
                    Log.d("FormularioEditarAdapter", "Error Response: " + errorMessage);
                    showToast(errorMessage);
                }
                response.close(); // Cerrar el Response para liberar recursos
            }
        });
    }


    private String obtenerMensajeError(Response response) throws IOException {
        String errorMessage;
        switch (response.code()) {
            case 400:
                errorMessage = "Error en la solicitud: " + response.body().string();
                break;
            case 404:
                errorMessage = "Producto no encontrado.";
                break;
            case 500:
                errorMessage = "Error interno del servidor.";
                break;
            default:
                errorMessage = "Error desconocido: " + response.body().string();
                break;
        }
        return errorMessage;
    }

    private void eliminarProductoEnAPI(int id) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://api-happypetshco-com.preview-domain.com/api/EliminarProducto=" + id;

        Request request = new Request.Builder()
                .url(url)
                .delete()
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                showToast("Error al eliminar producto");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    showToast("Producto eliminado correctamente");
                } else {
                    String errorMessage = response.body().string();
                    showToast("Error: " + errorMessage);
                }
            }
        });
    }

    private void showToast(String message) {
        if (context instanceof Submenu_AdminProductos) {
            ((Submenu_AdminProductos) context).runOnUiThread(() ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
        }
    }
}

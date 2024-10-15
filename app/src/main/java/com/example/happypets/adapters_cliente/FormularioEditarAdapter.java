package com.example.happypets.adapters_cliente;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
    private Context context;
    private Producto producto;
    private String token;

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
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText editarStock = dialogView.findViewById(R.id.editarStock);
        Button botonGuardar = dialogView.findViewById(R.id.botonGuardar);
        Button botonEliminar = dialogView.findViewById(R.id.botonEliminar);

        editarNombre.setText(producto.getNombre());
        editarDescripcion.setText(producto.getDescripcion());
        editarPrecio.setText(producto.getPrecio());
        editarDescuento.setText(producto.getDescuento());
        editarStock.setText(producto.getStock());

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        botonGuardar.setOnClickListener(v -> {
            String nuevoNombre = editarNombre.getText().toString();
            String nuevaDescripcion = editarDescripcion.getText().toString();
            String nuevoPrecio = editarPrecio.getText().toString();
            String nuevoDescuento = editarDescuento.getText().toString();
            String nuevoStock = editarStock.getText().toString();

            if (!TextUtils.isEmpty(nuevoNombre) && !TextUtils.isEmpty(nuevaDescripcion) &&
                    !TextUtils.isEmpty(nuevoPrecio) && !TextUtils.isEmpty(nuevoStock)) {
                editarProductoEnAPI(nuevoNombre, nuevaDescripcion, nuevoPrecio, nuevoDescuento, nuevoStock, editarNombre, editarDescripcion, editarPrecio, editarDescuento, editarStock);
            } else {
                Toast.makeText(context, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            }
        });

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

    // Método para limpiar el formulario
    private void limpiarFormulario(EditText editarNombre, EditText editarDescripcion,
                                   EditText editarPrecio, EditText editarDescuento,
                                   EditText editarStock) {
        editarNombre.setText("");
        editarDescripcion.setText("");
        editarPrecio.setText("");
        editarDescuento.setText("");
        editarStock.setText("");
    }

    private void editarProductoEnAPI(String nombre, String descripcion, String precio, String descuento, String stock,
                                     EditText editarNombre, EditText editarDescripcion,
                                     EditText editarPrecio, EditText editarDescuento,
                                     EditText editarStock) {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", producto.getId());
            jsonObject.put("nm_producto", nombre);
            jsonObject.put("descripcion", descripcion);
            jsonObject.put("categoria", producto.getCategoria());
            jsonObject.put("precio", precio);
            jsonObject.put("descuento", descuento);
            jsonObject.put("stock", stock);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("FormularioEditarAdapter", "JSON Body: " + jsonObject.toString());

        RequestBody body = RequestBody.create(mediaType, jsonObject.toString());
        Request request = new Request.Builder()
                .url("https://api-happypetshco-com.preview-domain.com/api/EditarProducto")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                if (context instanceof Submenu_AdminProductos) {
                    ((Submenu_AdminProductos) context).runOnUiThread(() ->
                            Toast.makeText(context, "Error al editar producto", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("FormularioEditarAdapter", "Response Code: " + response.code());

                if (response.isSuccessful()) {
                    if (context instanceof Submenu_AdminProductos) {
                        ((Submenu_AdminProductos) context).runOnUiThread(() -> {
                            Toast.makeText(context, "Producto editado correctamente", Toast.LENGTH_SHORT).show();
                            // Llamar al método para limpiar el formulario
                            limpiarFormulario(editarNombre, editarDescripcion, editarPrecio, editarDescuento, editarStock);
                        });
                    }
                } else {
                    Log.d("FormularioEditarAdapter", "Error Response: " + responseBody);
                    if (context instanceof Submenu_AdminProductos) {
                        ((Submenu_AdminProductos) context).runOnUiThread(() ->
                                Toast.makeText(context, "Error: " + responseBody, Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }

    private void eliminarProductoEnAPI(int id) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://api-happypetshco-com.preview-domain.com/api/EliminarProducto?id=" + id;

        Request request = new Request.Builder()
                .url(url)
                .get() // Cambiar a GET para eliminar
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                if (context instanceof Submenu_AdminProductos) {
                    ((Submenu_AdminProductos) context).runOnUiThread(() ->
                            Toast.makeText(context, "Error al eliminar producto", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                if (response.isSuccessful()) {
                    if (context instanceof Submenu_AdminProductos) {
                        ((Submenu_AdminProductos) context).runOnUiThread(() ->
                                Toast.makeText(context, "Producto eliminado correctamente", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    if (context instanceof Submenu_AdminProductos) {
                        ((Submenu_AdminProductos) context).runOnUiThread(() ->
                                Toast.makeText(context, "Error: " + responseBody, Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }
}

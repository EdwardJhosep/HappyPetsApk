package com.example.happypets.adapters_cliente;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
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

    public FormularioEditarAdapter(Context context, Producto producto) {
        this.context = context;
        this.producto = producto;
    }

    public void showEditDialog() {
        // Inflar el diálogo de edición
        View dialogView = LayoutInflater.from(context).inflate(R.layout.formulario_editar_producto, null);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setView(dialogView);

        // Obtener referencias a los campos del formulario
        EditText editarNombre = dialogView.findViewById(R.id.editarNombre);
        EditText editarDescripcion = dialogView.findViewById(R.id.editarDescripcion);
        EditText editarPrecio = dialogView.findViewById(R.id.editarPrecio);
        EditText editarDescuento = dialogView.findViewById(R.id.editarDescuento);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText editarStock = dialogView.findViewById(R.id.editarStock); // Añadir stock
        Button botonGuardar = dialogView.findViewById(R.id.botonGuardar);
        Button botonEliminar = dialogView.findViewById(R.id.botonEliminar); // Botón para eliminar

        // Prellenar el formulario con los valores actuales
        editarNombre.setText(producto.getNombre());
        editarDescripcion.setText(producto.getDescripcion());
        editarPrecio.setText(producto.getPrecio());
        editarDescuento.setText(producto.getDescuento());
        editarStock.setText(producto.getStock()); // Prellenar el stock

        // Crear el diálogo
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        // Configurar el botón de guardar
        botonGuardar.setOnClickListener(v -> {
            // Obtener los nuevos valores
            String nuevoNombre = editarNombre.getText().toString();
            String nuevaDescripcion = editarDescripcion.getText().toString();
            String nuevoPrecio = editarPrecio.getText().toString();
            String nuevoDescuento = editarDescuento.getText().toString();
            String nuevoStock = editarStock.getText().toString(); // Obtener nuevo stock

            // Validar y actualizar el producto
            if (!TextUtils.isEmpty(nuevoNombre) && !TextUtils.isEmpty(nuevaDescripcion) &&
                    !TextUtils.isEmpty(nuevoPrecio) && !TextUtils.isEmpty(nuevoStock)) {

                // Llamar a la función para editar el producto en la API
                editarProductoEnAPI(nuevoNombre, nuevaDescripcion, nuevoPrecio, nuevoDescuento, nuevoStock);
            }

            // Cerrar el diálogo
            dialog.dismiss();
        });

        // Configurar el botón de eliminar
        botonEliminar.setOnClickListener(v -> {
            // Llamar a la función para eliminar el producto en la API
            eliminarProductoEnAPI(producto.getId());
            dialog.dismiss(); // Cerrar el diálogo después de eliminar
        });
    }

    private void editarProductoEnAPI(String nombre, String descripcion, String precio, String descuento, String stock) {
        // Crear cliente OkHttp
        OkHttpClient client = new OkHttpClient();

        // Crear el cuerpo de la solicitud
        MediaType mediaType = MediaType.parse("application/json");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", producto.getId()); // Suponiendo que 'id' está disponible en el objeto Producto
            jsonObject.put("nm_producto", nombre);
            jsonObject.put("descripcion", descripcion);
            jsonObject.put("categoria", producto.getCategoria()); // Suponiendo que la categoría se mantiene
            jsonObject.put("precio", precio);
            jsonObject.put("descuento", descuento);
            jsonObject.put("stock", stock);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Crear la solicitud
        RequestBody body = RequestBody.create(mediaType, jsonObject.toString());
        Request request = new Request.Builder()
                .url("https://api-happypetshco-com.preview-domain.com/api/EditarProducto")
                .post(body)
                .addHeader("Content-Type", "application/json")
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
                if (response.isSuccessful()) {
                    if (context instanceof Submenu_AdminProductos) {
                        ((Submenu_AdminProductos) context).runOnUiThread(() -> {
                            Toast.makeText(context, "Producto editado correctamente", Toast.LENGTH_SHORT).show();
                            // Aquí puedes llamar a un método para actualizar la lista de productos
                            ((Submenu_AdminProductos) context).actualizarListaProductos();
                        });
                    }
                } else {
                    String errorResponse = response.body().string();
                    if (context instanceof Submenu_AdminProductos) {
                        ((Submenu_AdminProductos) context).runOnUiThread(() ->
                                Toast.makeText(context, "Error: " + errorResponse, Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }

    private void eliminarProductoEnAPI(int id) {
        // Crear cliente OkHttp
        OkHttpClient client = new OkHttpClient();

        // Construir la URL correctamente con el ID del producto
        String url = "https://api-happypetshco-com.preview-domain.com/api/EliminarProducto=" + id;

        // Crear la solicitud de eliminación
        Request request = new Request.Builder()
                .url(url)  // Usar la URL construida
                .get()     // Método GET para eliminar
                .addHeader("Content-Type", "application/json")
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
                String responseBody = response.body().string(); // Obtener el cuerpo de la respuesta

                if (response.isSuccessful()) {
                    if (context instanceof Submenu_AdminProductos) {
                        ((Submenu_AdminProductos) context).runOnUiThread(() -> {
                            Toast.makeText(context, "Producto eliminado correctamente", Toast.LENGTH_SHORT).show();
                            // Llamar a un método para actualizar la lista de productos
                            ((Submenu_AdminProductos) context).actualizarListaProductos();
                        });
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
    public void actualizarListaProductos() {
        // Hacer una solicitud a la API para obtener los productos actualizados
        obtenerProductosDesdeAPI();

        // Luego de obtener los productos actualizados, asegúrate de notificar al adaptador
        adaptador.notifyDataSetChanged();
    }

}

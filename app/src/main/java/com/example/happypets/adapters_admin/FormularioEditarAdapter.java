package com.example.happypets.adapters_admin;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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

        // Obtener referencias a los campos de edición
        EditText editNombre = dialogView.findViewById(R.id.editarNombre);
        EditText editDescripcion = dialogView.findViewById(R.id.editarDescripcion);
        EditText editCategoria = dialogView.findViewById(R.id.editarCategoria);
        EditText editPrecio = dialogView.findViewById(R.id.editarPrecio);
        EditText editStock = dialogView.findViewById(R.id.editarStock);
        EditText editDescuento = dialogView.findViewById(R.id.editarDescuento); // Campo de descuento
        CheckBox checkBlanco = dialogView.findViewById(R.id.checkBlanco);
        CheckBox checkRojo = dialogView.findViewById(R.id.checkRojo);
        CheckBox checkAzul = dialogView.findViewById(R.id.checkAzul);
        CheckBox checkVerde = dialogView.findViewById(R.id.checkVerde);
        CheckBox checkMorado = dialogView.findViewById(R.id.checkMorado);
        CheckBox checkAmarillo = dialogView.findViewById(R.id.checkAmarillo);
        CheckBox checkNegro = dialogView.findViewById(R.id.checkNegro);
        Button botonActualizar = dialogView.findViewById(R.id.botonActualizar);
        Button botonEliminar = dialogView.findViewById(R.id.botonEliminar);

        // Prellenar los campos con los datos del producto actual
        editNombre.setText(producto.getNombre());
        editDescripcion.setText(producto.getDescripcion());
        editCategoria.setText(producto.getCategoria());
        editPrecio.setText(String.valueOf(producto.getPrecio()));
        editStock.setText(String.valueOf(producto.getStock()));

        // Verificar si el descuento es null o vacío, y asignar '0' si es necesario
        String descuento = producto.getDescuento();
        if (descuento == null || descuento.isEmpty()) {
            descuento = "0";  // Asignamos '0' si es null o vacío
        }
        editDescuento.setText(descuento); // Seteamos el descuento en el campo

        // Prellenar los CheckBoxes según los colores actuales
        String[] coloresActuales = producto.getColores().split(",");
        for (String color : coloresActuales) {
            switch (color.trim()) {
                case "Blanco":
                    checkBlanco.setChecked(true);
                    break;
                case "Rojo":
                    checkRojo.setChecked(true);
                    break;
                case "Azul":
                    checkAzul.setChecked(true);
                    break;
                case "Verde":
                    checkVerde.setChecked(true);
                    break;
                case "Morado":
                    checkMorado.setChecked(true);
                    break;
                case "Amarillo":
                    checkAmarillo.setChecked(true);
                    break;
                case "Negro":
                    checkNegro.setChecked(true);
                    break;
            }
        }

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        // Listener para actualizar producto
        botonActualizar.setOnClickListener(v -> {
            // Obtener datos de los campos de edición
            String nombre = editNombre.getText().toString();
            String descripcion = editDescripcion.getText().toString();
            String categoria = editCategoria.getText().toString();
            String precio = editPrecio.getText().toString();
            String stock = editStock.getText().toString();
            String descuentoInput = editDescuento.getText().toString(); // Aquí hemos renombrado la variable

            // Validación: Si el campo de descuento está vacío o no es un número válido, asignar 0
            if (descuentoInput.isEmpty() || !isNumeric(descuentoInput)) {
                descuentoInput = "0"; // Asignar 0 si no es válido
            }

            // Obtener colores seleccionados
            StringBuilder coloresSeleccionados = new StringBuilder();
            if (checkBlanco.isChecked()) coloresSeleccionados.append("Blanco,");
            if (checkRojo.isChecked()) coloresSeleccionados.append("Rojo,");
            if (checkAzul.isChecked()) coloresSeleccionados.append("Azul,");
            if (checkVerde.isChecked()) coloresSeleccionados.append("Verde,");
            if (checkMorado.isChecked()) coloresSeleccionados.append("Morado,");
            if (checkAmarillo.isChecked()) coloresSeleccionados.append("Amarillo,");
            if (checkNegro.isChecked()) coloresSeleccionados.append("Negro,");

            // Eliminar la última coma si existe
            if (coloresSeleccionados.length() > 0) {
                coloresSeleccionados.setLength(coloresSeleccionados.length() - 1);
            }

            // Llamar a la función para actualizar el producto
            actualizarProductoEnAPI(producto.getId(), nombre, descripcion, categoria, precio, stock, descuentoInput, coloresSeleccionados.toString());
            dialog.dismiss();
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

    // Función para verificar si una cadena es un número válido
    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void actualizarProductoEnAPI(int id, String nombre, String descripcion, String categoria, String precio, String stock, String descuento, String colores) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://api.happypetshco.com/api/ActualizarProducto";

        // Crear el cuerpo de la solicitud
        JSONObject body = new JSONObject();
        try {
            body.put("id", id);
            body.put("nm_producto", nombre);
            body.put("descripcion", descripcion);
            body.put("categoria", categoria);
            body.put("precio", precio);
            body.put("stock", stock);
            body.put("descuento", descuento); // Añadir descuento al cuerpo de la solicitud
            body.put("colores", colores); // Añadir colores al cuerpo de la solicitud
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(body.toString(), okhttp3.MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(url)
                .put(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                showToast("Error al actualizar producto");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    showToast("Producto actualizado correctamente");
                    // Redirigir al Submenu_AdminProductos después de actualizar con éxito
                    redirectToSubmenuAdminProductos();
                } else {
                    String errorMessage = response.body().string();
                    showToast("Error: " + errorMessage);
                }
            }
        });
    }

    private void eliminarProductoEnAPI(int id) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://api.happypetshco.com/api/EliminarProducto=" + id;

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
                showToast("Error de conexión");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    showToast("Producto eliminado correctamente");
                    redirectToSubmenuAdminProductos();
                } else {
                    String errorMessage = response.body().string();
                    showToast("Error al eliminar el producto: " + errorMessage);
                }
            }
        });
    }

    // Función para redirigir a Submenu_AdminProductos
    private void redirectToSubmenuAdminProductos() {
        if (context instanceof Submenu_AdminProductos) {
            ((Submenu_AdminProductos) context).refreshProductos();
        }
    }

    private void showToast(final String message) {
        ((android.app.Activity) context).runOnUiThread(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
    }
}
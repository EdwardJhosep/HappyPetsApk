package com.example.happypets.adapters_cliente;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.example.happypets.R;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ListarCarritoAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<JSONObject> productos;
    private OkHttpClient client;
    private String userId; // Agregar variable para userId
    private String token;  // Agregar variable para token

    public ListarCarritoAdapter(Context context, ArrayList<JSONObject> productos, String userId, String token) {
        this.context = context;
        this.productos = productos;
        this.client = new OkHttpClient(); // Inicializa el cliente HTTP
        this.userId = userId; // Inicializar userId
        this.token = token; // Inicializar token
    }

    @Override
    public int getCount() {
        return productos.size();
    }

    @Override
    public Object getItem(int position) {
        return productos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_carrito, parent, false);
        }

        TextView tvNombreProducto = convertView.findViewById(R.id.tvNombreProducto);
        TextView tvCantidad = convertView.findViewById(R.id.tvCantidad);
        TextView tvColor = convertView.findViewById(R.id.tvColor);
        TextView tvImporte = convertView.findViewById(R.id.tvImporte);
        ImageView ivProducto = convertView.findViewById(R.id.ivProducto);
        ImageButton buttonEliminar = convertView.findViewById(R.id.buttonEliminar); // Referencia al botón de eliminar

        try {
            JSONObject producto = productos.get(position);

            // Imprimir el objeto JSON para verificar su contenido
            System.out.println("Producto JSON: " + producto.toString());

            // Verificar si el campo id existe
            if (producto.has("id")) {
                final String idCarrito = producto.getString("id"); // Obtén el ID del carrito

                if (producto.has("producto")) {
                    JSONObject detalleProducto = producto.getJSONObject("producto");

                    // Configurar detalles del producto
                    tvNombreProducto.setText(detalleProducto.optString("nm_producto", "Nombre no disponible"));
                    tvCantidad.setText("Cantidad: " + producto.optString("cantidad", "0"));
                    tvColor.setText("Color: " + producto.optString("color", "Sin color"));
                    tvImporte.setText("Importe: S/ " + producto.optString("importe", "0.00"));

                    // Cargar la imagen del producto usando Glide
                    String imagenUrl = "https://api-happypetshco-com.preview-domain.com/ServidorProductos/" + detalleProducto.optString("imagen", "default_image.png");
                    Glide.with(context)
                            .load(imagenUrl)
                            .placeholder(R.drawable.logo) // Imagen por defecto mientras se carga
                            .error(R.drawable.logo) // Imagen en caso de error
                            .into(ivProducto);

                    // Configurar el botón de eliminar
                    buttonEliminar.setOnClickListener(v -> mostrarDialogoConfirmacion(idCarrito)); // Mostrar el diálogo de confirmación
                } else {
                    throw new Exception("El producto no tiene información válida.");
                }
            } else {
                throw new Exception("No se encontró el id en el producto.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarToast("Error al cargar el producto: " + e.getMessage()); // Mensaje de error más detallado
        }

        return convertView;
    }

    // Método para mostrar el diálogo de confirmación
    private void mostrarDialogoConfirmacion(String idCarrito) {
        new AlertDialog.Builder(context)
                .setTitle("Confirmar Eliminación")
                .setMessage("¿Estás seguro de que deseas eliminar este producto del carrito?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarProducto(idCarrito))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Método para eliminar el producto del carrito
    private void eliminarProducto(String idCarrito) {
        String url = "https://api-happypetshco-com.preview-domain.com/api/EliminarCarrito?id=" + idCarrito; // Ajusta la URL según tu API

        Request request = new Request.Builder()
                .url(url)
                .delete() // Cambiado a delete si el endpoint lo requiere
                .addHeader("Authorization", "Bearer " + token) // Establecer el token en el encabezado
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                mostrarToast("Error al eliminar el producto");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Eliminar el producto de la lista y notificar el cambio
                    mostrarToast("Producto eliminado correctamente");
                    productos.removeIf(producto -> {
                        try {
                            return producto.getString("id").equals(idCarrito);
                        } catch (Exception e) {
                            return false;
                        }
                    });
                    notifyDataSetChanged();
                } else {
                    // Manejo de error si no se pudo eliminar el producto
                    mostrarToast("No se pudo eliminar el producto: " + response.message());
                }
            }
        });
    }

    // Método para mostrar un Toast en la UI
    private void mostrarToast(final String mensaje) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show());
    }
}

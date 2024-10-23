package com.example.happypets.adapters_cliente;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.happypets.R;

import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ListarCarritoAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<JSONObject> productos;
    private String userId;
    private String token;

    public ListarCarritoAdapter(Context context, ArrayList<JSONObject> productos, String userId, String token) {
        this.context = context;
        this.productos = productos;
        this.userId = userId;
        this.token = token;
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
        ImageButton buttonEliminar = convertView.findViewById(R.id.buttonEliminar);

        try {
            JSONObject producto = productos.get(position);
            if (producto.has("id")) {
                final String id = producto.getString("id");
                if (producto.has("producto")) {
                    JSONObject detalleProducto = producto.getJSONObject("producto");
                    tvNombreProducto.setText(detalleProducto.optString("nm_producto", "Nombre no disponible"));
                    tvCantidad.setText("Cantidad: " + producto.optString("cantidad", "0"));
                    tvColor.setText("Color: " + producto.optString("color", "Sin color"));
                    tvImporte.setText("Importe: S/ " + producto.optString("importe", "0.00"));
                    String imagenUrl = "https://api-happypetshco-com.preview-domain.com/ServidorProductos/" + detalleProducto.optString("imagen", "default_image.png");
                    Glide.with(context)
                            .load(imagenUrl)
                            .placeholder(R.drawable.logo)
                            .error(R.drawable.logo)
                            .into(ivProducto);
                }

                // Configurar el botón de eliminar
                buttonEliminar.setOnClickListener(v -> eliminarProducto(id));
            }
        } catch (Exception e) {
            Log.e("ListarCarritoAdapter", "Error al cargar el producto en getView: " + e.getMessage(), e);
            mostrarToast("Error al cargar el producto: " + e.getMessage());
        }

        return convertView;
    }
    private void eliminarProducto(String id) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                String url = "https://api-happypetshco-com.preview-domain.com/api/EliminarCarrito=" + id;
                Request request = new Request.Builder()
                        .url(url)
                        .delete()
                        .addHeader("Authorization", "Bearer " + token) // Asegúrate de incluir el token
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        // Si la eliminación fue exitosa, actualiza la lista
                        ((Activity) context).runOnUiThread(() -> {
                            productos.removeIf(producto -> producto.optString("id").equals(id));
                            notifyDataSetChanged();
                            mostrarToast("Producto eliminado del carrito.");
                        });
                    } else {
                        mostrarToast("Error al eliminar el producto: " + response.message());
                    }
                }
            } catch (Exception e) {
                Log.e("ListarCarritoAdapter", "Error al eliminar producto: " + e.getMessage(), e);
                mostrarToast("Error al eliminar el producto.");
            }
        }).start();
    }

    private void mostrarToast(final String mensaje) {
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show());
    }
}

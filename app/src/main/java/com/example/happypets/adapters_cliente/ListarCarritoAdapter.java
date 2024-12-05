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
import android.widget.CheckBox;
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
    private ArrayList<String> selectedProductIds = new ArrayList<>(); // Lista de IDs seleccionados
    private double totalSum = 0.0; // Suma total de los productos seleccionados

    // Interfaz para comunicación con el fragmento
    public interface OnSelectedProductsChangedListener {
        void onSelectedProductsChanged(ArrayList<String> selectedProductIds, double totalSum);
    }

    private OnSelectedProductsChangedListener listener;

    public ListarCarritoAdapter(Context context, ArrayList<JSONObject> productos, String userId, String token) {
        this.context = context;
        this.productos = productos;
        this.userId = userId;
        this.token = token;
    }

    // Add this method to allow setting the listener
    public void setOnSelectedProductsChangedListener(OnSelectedProductsChangedListener listener) {
        this.listener = listener;
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
        CheckBox checkboxProducto = convertView.findViewById(R.id.checkBoxSeleccionar); // Checkbox

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
                    String imagenUrl = "https://api.happypetshco.com/ServidorProductos/" + detalleProducto.optString("imagen", "default_image.png");
                    Glide.with(context)
                            .load(imagenUrl)
                            .placeholder(R.drawable.logo)
                            .error(R.drawable.logo)
                            .into(ivProducto);
                }

                // Lógica para selección del checkbox
                checkboxProducto.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedProductIds.add(id); // Añadir ID del producto a la lista
                        updateTotalSum();
                    } else {
                        selectedProductIds.remove(id); // Eliminar ID del producto de la lista
                        updateTotalSum();
                    }
                });

                // Lógica para eliminar producto
                buttonEliminar.setOnClickListener(v -> {
                    new android.app.AlertDialog.Builder(context)
                            .setTitle("Eliminar Producto")
                            .setMessage("¿Estás seguro de que deseas eliminar este producto del carrito?")
                            .setPositiveButton("Sí", (dialog, which) -> eliminarProducto(id))
                            .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                            .show();
                });
            }
        } catch (Exception e) {
            Log.e("ListarCarritoAdapter", "Error al cargar el producto en getView: " + e.getMessage(), e);
            mostrarToast("Error al cargar el producto: " + e.getMessage());
        }

        return convertView;
    }

    private void updateTotalSum() {
        totalSum = 0.0;
        StringBuilder selectedProducts = new StringBuilder("Productos seleccionados: ");

        // Calcular la suma total y generar la lista de productos seleccionados
        for (String id : selectedProductIds) {
            for (JSONObject producto : productos) {
                try {
                    if (producto.getString("id").equals(id)) {
                        double importe = Double.parseDouble(producto.getString("importe"));
                        totalSum += importe;
                        String productName = producto.getJSONObject("producto").optString("nm_producto", "Nombre no disponible");
                        selectedProducts.append(productName).append(" (ID: ").append(id).append("), ");
                    }
                } catch (Exception e) {
                    Log.e("ListarCarritoAdapter", "Error al calcular el total: " + e.getMessage(), e);
                }
            }
        }

        // Añadir el total al final de la cadena de productos seleccionados
        selectedProducts.append("\nTotal: ").append(String.format("%.2f", totalSum));

        Log.d("ListarCarritoAdapter", selectedProducts.toString());

        // Notificar al fragmento sobre los productos seleccionados y el total
        if (listener != null) {
            listener.onSelectedProductsChanged(selectedProductIds, totalSum);  // Pasar el total junto con los IDs
        }
    }



    private void eliminarProducto(String id) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                String url = "https://api.happypetshco.com/api/EliminarCarrito=" + id;
                Request request = new Request.Builder()
                        .url(url)
                        .delete()
                        .addHeader("Authorization", "Bearer " + token)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        ((Activity) context).runOnUiThread(() -> {
                            // Eliminar el producto de la lista
                            productos.removeIf(producto -> producto.optString("id").equals(id));
                            selectedProductIds.remove(id);  // Eliminar el ID del producto de la lista seleccionada
                            updateTotalSum();  // Recalcular la suma total
                            notifyDataSetChanged();  // Actualizar la vista del adaptador
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


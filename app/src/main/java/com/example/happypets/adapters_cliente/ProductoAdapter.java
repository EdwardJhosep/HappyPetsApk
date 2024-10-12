package com.example.happypets.adapters_cliente;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.happypets.R;
import com.example.happypets.models.Producto;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    private ArrayList<Producto> productos;
    private String userId; // Añadir el userId

    // Modificar el constructor para aceptar userId
    public ProductoAdapter(ArrayList<Producto> productos, String userId) {
        this.productos = productos;
        this.userId = userId;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = productos.get(position);

        // Establecer los valores de texto solo si no están vacíos
        holder.nombreProducto.setText(TextUtils.isEmpty(producto.getNombre()) ? "Nombre no disponible" : producto.getNombre());
        holder.descripcionProducto.setText(TextUtils.isEmpty(producto.getDescripcion()) ? "Descripción no disponible" : producto.getDescripcion());

        // Obtener el precio y el descuento
        String precio = producto.getPrecio();
        String descuento = producto.getDescuento();

        if (TextUtils.isEmpty(precio)) {
            holder.precioProducto.setText("Precio no disponible");
        } else {
            double precioOriginal = Double.parseDouble(precio);
            double descuentoValor = TextUtils.isEmpty(descuento) || descuento.equals("0") || "null".equals(descuento)
                    ? 0
                    : Double.parseDouble(descuento);

            if (descuentoValor > 0) {
                double precioConDescuento = precioOriginal - descuentoValor;
                holder.precioProducto.setText("Antes: S/. " + precioOriginal + "\nAhora: S/. " + precioConDescuento);
                holder.descuentoProducto.setVisibility(View.VISIBLE);
                holder.descuentoProducto.setText("Descuento: S/. " + descuentoValor);
            } else {
                holder.precioProducto.setText("S/. " + precioOriginal);
                holder.descuentoProducto.setVisibility(View.GONE); // Ocultar si no hay descuento
            }
        }

        // Cargar la imagen con Picasso
        Picasso.get()
                .load("https://api-happypetshco-com.preview-domain.com/ServidorProductos/" + producto.getImagen())
                .placeholder(R.drawable.logo) // Reemplaza con tu drawable de marcador de posición
                .error(R.drawable.logo) // Reemplaza con tu drawable de error
                .into(holder.imagenProducto, new Callback() {
                    @Override
                    public void onSuccess() {
                        // La imagen se cargó correctamente
                    }

                    @Override
                    public void onError(Exception e) {
                        // Manejo de errores, puedes registrar el error o mostrar un mensaje
                    }
                });

        // Configurar el clic en el producto para mostrar el userId en un Toast
        holder.itemView.setOnClickListener(v -> {
            // Obtener el contexto desde el holder
            Context context = holder.itemView.getContext();
            // Mostrar un Toast con el userId
            Toast.makeText(v.getContext(), "Producto ID: " + producto.getId() + ", User ID: " + userId, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return productos.size();
    }

    // Método para actualizar la lista
    public void updateList(ArrayList<Producto> newList) {
        productos = newList;
        notifyDataSetChanged();
    }

    public static class ProductoViewHolder extends RecyclerView.ViewHolder {

        TextView nombreProducto, descripcionProducto, precioProducto, descuentoProducto;
        ImageView imagenProducto;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreProducto = itemView.findViewById(R.id.nombreProducto);
            descripcionProducto = itemView.findViewById(R.id.descripcionProducto);
            precioProducto = itemView.findViewById(R.id.precioProducto);
            descuentoProducto = itemView.findViewById(R.id.descuentoProducto); // Añade el descuento
            imagenProducto = itemView.findViewById(R.id.imagenProducto);
        }
    }
}
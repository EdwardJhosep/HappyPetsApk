package com.example.happypets.adapters_cliente;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.happypets.R;
import com.example.happypets.models.Producto;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    private ArrayList<Producto> productos;

    public ProductoAdapter(ArrayList<Producto> productos) {
        this.productos = productos;
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
        holder.precioProducto.setText(TextUtils.isEmpty(producto.getPrecio()) ? "Precio no disponible" : "S/. " + producto.getPrecio());

        // Establecer el descuento
        String descuento = producto.getDescuento();
        if (TextUtils.isEmpty(descuento) || descuento.equals("0") || "null".equals(descuento)) {
            holder.descuentoProducto.setVisibility(View.GONE); // Ocultar si no hay descuento
        } else {
            holder.descuentoProducto.setVisibility(View.VISIBLE);
            holder.descuentoProducto.setText("Descuento: S/. " + descuento);
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
        Button botonComprar; // Botón para comprar el producto

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

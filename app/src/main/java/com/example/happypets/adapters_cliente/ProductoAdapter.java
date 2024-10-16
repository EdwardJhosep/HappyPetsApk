package com.example.happypets.adapters_cliente;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.example.happypets.R;
import com.example.happypets.models.Producto;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    private ArrayList<Producto> productos;
    private String userId; // Añadir el userId
    private String token;   // Añadir el token

    // Modificar el constructor para aceptar userId y token
    public ProductoAdapter(ArrayList<Producto> productos, String userId, String token) {
        this.productos = productos;
        this.userId = userId;
        this.token = token; // Inicializar el token
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

        double precioOriginal = TextUtils.isEmpty(precio) ? 0 : Double.parseDouble(precio);
        double descuentoValor = TextUtils.isEmpty(descuento) || descuento.equals("0") || "null".equals(descuento)
                ? 0
                : Double.parseDouble(descuento);

        double precioConDescuento; // Declarar la variable sin inicializar
        if (descuentoValor > 0 && descuentoValor <= precioOriginal) {
            precioConDescuento = precioOriginal - descuentoValor; // Calcular el precio con descuento
            holder.precioProducto.setText(" ̶A̶n̶t̶:̶S̶/̶:̶" + precioOriginal + "\nS/. " + precioConDescuento);
            holder.descuentoProducto.setVisibility(View.VISIBLE);
            holder.descuentoProducto.setText("Descuento: S/. " + descuentoValor);
        } else {
            precioConDescuento = precioOriginal; // Asignar precioOriginal si no hay descuento
            holder.precioProducto.setText("S/. " + precioOriginal);
            holder.descuentoProducto.setVisibility(View.GONE); // Ocultar si no hay descuento
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
                        Log.e("ProductoAdapter", "Error loading image: " + e.getMessage());
                    }
                });

        // Configurar el clic en el producto para mostrar el userId en un Toast
        holder.itemView.setOnClickListener(v -> {
            // Obtener el contexto desde el holder
            Context context = holder.itemView.getContext();
            // Mostrar un Toast con el userId
        });

        // Cambiar para usar el precioConDescuento si está disponible
        final String productPrice = (descuentoValor > 0) ? String.valueOf(precioConDescuento) : String.valueOf(precioOriginal);
        holder.imageButtonCarrito.setOnClickListener(v -> {
            // Crear y mostrar el CarritoAdapter
            CarritoAdapter carritoDialog = CarritoAdapter.newInstance(userId, String.valueOf(producto.getId()), productPrice, token); // Añadir token aquí
            carritoDialog.show(((AppCompatActivity) holder.itemView.getContext()).getSupportFragmentManager(), "CarritoDialog");
        });
    }

    @Override
    public int getItemCount() {
        return productos.size();
    }

    // Método para actualizar la lista
    public void updateList(ArrayList<Producto> newList) {
        productos.clear();
        productos.addAll(newList);
        notifyDataSetChanged();
    }

    public static class ProductoViewHolder extends RecyclerView.ViewHolder {

        TextView nombreProducto, descripcionProducto, precioProducto, descuentoProducto;
        ImageView imagenProducto, imageButtonCarrito; // Añadir el botón del carrito

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreProducto = itemView.findViewById(R.id.nombreProducto);
            descripcionProducto = itemView.findViewById(R.id.descripcionProducto);
            precioProducto = itemView.findViewById(R.id.precioProducto);
            descuentoProducto = itemView.findViewById(R.id.descuentoProducto); // Añade el descuento
            imagenProducto = itemView.findViewById(R.id.imagenProducto);
            imageButtonCarrito = itemView.findViewById(R.id.imageButtonCarrito); // Asegúrate de que este ID es correcto
        }
    }
}

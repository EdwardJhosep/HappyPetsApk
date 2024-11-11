package com.example.happypets.adapters_admin;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.happypets.R;
import com.example.happypets.models.Producto;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ProductoAdapterEditar extends RecyclerView.Adapter<ProductoAdapterEditar.ProductoViewHolder> {

    private ArrayList<Producto> productos;
    private String token;

    public ProductoAdapterEditar(ArrayList<Producto> productos, String token) {
        this.productos = productos;
        this.token = token;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto_editar, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = productos.get(position);
        holder.nombreProducto.setText(TextUtils.isEmpty(producto.getNombre()) ? "Nombre no disponible" : producto.getNombre());
        holder.descripcionProducto.setText(TextUtils.isEmpty(producto.getDescripcion()) ? "Descripción no disponible" : producto.getDescripcion());

        String colores = producto.getColores();  // Obtén los colores

        // Obtener el precio y el descuento
        String precio = producto.getPrecio();
        String descuento = producto.getDescuento(); // Asumiendo que es un porcentaje en formato de cadena

        double precioOriginal = TextUtils.isEmpty(precio) ? 0 : Double.parseDouble(precio);
        double descuentoPorcentaje = TextUtils.isEmpty(descuento) || descuento.equals("0") || "null".equals(descuento)
                ? 0
                : Double.parseDouble(descuento);

        double precioConDescuento; // Declarar la variable sin inicializar
        if (descuentoPorcentaje > 0 && descuentoPorcentaje <= 100) {
            // Calcular el precio con descuento
            precioConDescuento = precioOriginal - (precioOriginal * (descuentoPorcentaje / 100));
            holder.precioProducto.setText(" ̶A̶n̶t̶:̶S̶/̶:̶" + precioOriginal + "\nS/. " + precioConDescuento);
            holder.descuentoProducto.setVisibility(View.VISIBLE);
            holder.descuentoProducto.setText("Descuento: " + descuentoPorcentaje + "%");
        } else {
            precioConDescuento = precioOriginal; // Asignar precioOriginal si no hay descuento
            holder.precioProducto.setText("S/. " + precioOriginal);
            holder.descuentoProducto.setVisibility(View.GONE); // Ocultar si no hay descuento
        }





        // Mostrar colores
        if (TextUtils.isEmpty(colores)) {
            holder.coloresProducto.setText("Colores no disponibles");
        } else {
            holder.coloresProducto.setText("Colores: " + colores);
        }

        Picasso.get()
                .load("https://api.happypetshco.com/ServidorProductos/" + producto.getImagen())
                .placeholder(R.drawable.logo)
                .error(R.drawable.logo)
                .into(holder.imagenProducto, new Callback() {
                    @Override
                    public void onSuccess() {}

                    @Override
                    public void onError(Exception e) {}
                });

        holder.imageButtonEditar.setOnClickListener(view -> {
            FormularioEditarAdapter formularioEditarAdapter = new FormularioEditarAdapter(view.getContext(), producto, token);
            formularioEditarAdapter.showEditDialog();
        });
    }


    @Override
    public int getItemCount() {
        return productos.size();
    }

    public void updateList(ArrayList<Producto> newList) {
        productos = newList;
        notifyDataSetChanged();
    }

    public static class ProductoViewHolder extends RecyclerView.ViewHolder {

        TextView nombreProducto, descripcionProducto, precioProducto, descuentoProducto, coloresProducto;  // Nuevo TextView para colores
        ImageView imagenProducto;
        ImageButton imageButtonEditar;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreProducto = itemView.findViewById(R.id.nombreProducto);
            descripcionProducto = itemView.findViewById(R.id.descripcionProducto);
            precioProducto = itemView.findViewById(R.id.precioProducto);
            descuentoProducto = itemView.findViewById(R.id.descuentoProducto);
            coloresProducto = itemView.findViewById(R.id.coloresProducto);
            imagenProducto = itemView.findViewById(R.id.imagenProducto);
            imageButtonEditar = itemView.findViewById(R.id.imageButtonEditar);
        }
    }
}
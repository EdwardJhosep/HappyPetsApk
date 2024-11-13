package com.example.happypets.vista_previa;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.happypets.Login;
import com.example.happypets.R;
import com.example.happypets.models.Producto;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class ProductoAdapterPreview extends RecyclerView.Adapter<ProductoAdapterPreview.ProductoViewHolder> {

    private ArrayList<Producto> productos;
    private String token;

    // Constructor
    public ProductoAdapterPreview(ArrayList<Producto> productoList, String token) {
        this.productos = productoList;
        this.token = token; // Inicializar el token
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto_preview, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = productos.get(position);

        // Establecer los valores de texto solo si no están vacíos
        holder.nombreProducto.setText(TextUtils.isEmpty(producto.getNombre()) ? "Nombre no disponible" : producto.getNombre());
        holder.descripcionProducto.setText(TextUtils.isEmpty(producto.getDescripcion()) ? "Descripción no disponible" : producto.getDescripcion());

        // Mostrar categoría, subcategoría y sub-subcategoría
        holder.categoriaProducto.setText(TextUtils.isEmpty(producto.getCategoria()) ? "Categoría no disponible" : producto.getCategoria());

        // Obtener colores seleccionados
        String colores = producto.getColores();
        holder.layoutColoresSeleccionados.removeAllViews(); // Limpiar colores anteriores

        if (!TextUtils.isEmpty(colores)) {
            String[] coloresArray = colores.split(",");
            for (String color : coloresArray) {
                agregarColorAlLayout(holder.layoutColoresSeleccionados, color.trim());
            }
        } else {
            holder.coloresProducto.setText("Colores no disponibles");
        }

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

        // Cargar la imagen con Picasso
        String imagenUrl = "https://api.happypetshco.com/ServidorProductos/" + producto.getImagen(); // Definir la variable imagenUrl
        Picasso.get()
                .load(imagenUrl)
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

        holder.itemView.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            // Aquí puedes manejar el clic en el ítem del producto si es necesario
        });

// Configurar el clic en el botón de login
        holder.btnLogin.setOnClickListener(v -> {
            // Iniciar la actividad de Login
            Intent intent = new Intent(holder.itemView.getContext(), Login.class);
            holder.itemView.getContext().startActivity(intent);
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

    // Método para agregar un círculo de color al layout
    private void agregarColorAlLayout(LinearLayout layout, String color) {
        int colorValue;
        switch (color.toLowerCase()) {
            case "blanco":
                colorValue = Color.WHITE;
                break;
            case "rojo":
                colorValue = Color.RED;
                break;
            case "azul":
                colorValue = Color.BLUE;
                break;
            case "verde":
                colorValue = Color.GREEN;
                break;
            case "morado":
                colorValue = Color.parseColor("#800080"); // Código hexadecimal para morado
                break;
            case "amarillo":
                colorValue = Color.YELLOW;
                break;
            case "negro":
                colorValue = Color.BLACK;
                break;
            default:
                return; // Si el color no es reconocido, no hacer nada
        }

        // Crear un View para el color
        View colorView = new View(layout.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(50, 50); // Tamaño del círculo
        params.setMargins(5, 5, 5, 5); // Espaciado entre círculos
        colorView.setLayoutParams(params);

        // Configurar el fondo del View como un GradientDrawable
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL); // Forma ovalada (círculo)

        if (colorValue == Color.WHITE) {
            // Si el color es blanco, agregar un borde
            drawable.setStroke(2, Color.BLACK); // Borde de 2 píxeles de grosor y color negro
        }

        drawable.setColor(colorValue); // Color de fondo
        colorView.setBackground(drawable); // Aplicar el drawable al View
        layout.addView(colorView); // Añadir el View al layout
    }

    public static class ProductoViewHolder extends RecyclerView.ViewHolder {

        TextView nombreProducto, descripcionProducto, precioProducto, coloresProducto, descuentoProducto;
        TextView categoriaProducto;
        ImageView imagenProducto, imageButtonCarrito; // Añadir el botón del carrito
        LinearLayout layoutColoresSeleccionados; // Añadir layout para colores seleccionados
        ImageButton btnLogin; // Aquí declaramos el botón

        @SuppressLint("WrongViewCast")
        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreProducto = itemView.findViewById(R.id.nombreProducto);
            descripcionProducto = itemView.findViewById(R.id.descripcionProducto);
            precioProducto = itemView.findViewById(R.id.precioProducto);
            descuentoProducto = itemView.findViewById(R.id.descuentoProducto); // Añade el descuento
            imagenProducto = itemView.findViewById(R.id.imagenProducto);
            imageButtonCarrito = itemView.findViewById(R.id.imageButtonCarrito); // Asegúrate de que este ID es correcto
            layoutColoresSeleccionados = itemView.findViewById(R.id.layoutColoresSeleccionados); // Cambia el ID según tu diseño

            // Inicializar las vistas para las nuevas categorías
            categoriaProducto = itemView.findViewById(R.id.categoriaProducto);

            // Inicializar btnLogin aquí
            btnLogin = itemView.findViewById(R.id.btnLogin); // Esto hace que btnLogin se refiera al ImageButton en item_producto.xml
        }
    }
}

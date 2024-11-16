package com.example.happypets.perfilview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;  // Import Glide for image loading
import com.example.happypets.R;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CarritoAdapterFinal extends BaseAdapter {

    private Context context;
    private ArrayList<JSONObject> carritoItems;
    private String token;
    private String userId;

    // Modify the constructor to accept the token and userId
    public CarritoAdapterFinal(Context context, ArrayList<JSONObject> carritoItems, String token, String userId) {
        this.context = context;
        this.carritoItems = carritoItems;
        this.token = token;
        this.userId = userId;
    }

    @Override
    public int getCount() {
        return carritoItems.size();
    }

    @Override
    public Object getItem(int position) {
        return carritoItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_carrito, parent, false);
        }

        try {
            // Obtener el ítem actual
            JSONObject item = carritoItems.get(position);

            // Configurar la cantidad, color y otros detalles en los TextViews
            TextView cantidadTextView = convertView.findViewById(R.id.textViewCantidad);
            cantidadTextView.setText("Cantidad: " + item.optString("cantidad", "N/A"));

            TextView colorTextView = convertView.findViewById(R.id.textViewColor);
            colorTextView.setText("Color: " + item.optString("color", "No especificado"));

            TextView importeTextView = convertView.findViewById(R.id.textViewImporte);
            importeTextView.setText("Importe: " + item.optString("importe", "N/A"));

            TextView tipoPagoTextView = convertView.findViewById(R.id.textViewTipoPago);
            tipoPagoTextView.setText("Tipo de Pago: " + item.optString("tipo_pago", "No especificado"));

            // Mostrar solo la parte de la fecha de updated_at
            TextView updatedAtTextView = convertView.findViewById(R.id.textViewFecha);
            String updatedAt = item.optString("updated_at", "N/A");

            // Formatear updated_at para mostrar solo la fecha
            if (!updatedAt.equals("N/A")) {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = inputFormat.parse(updatedAt);
                updatedAtTextView.setText("Fecha: " + outputFormat.format(date));
            } else {
                updatedAtTextView.setText("Fecha: N/A");
            }

            // Obtener detalles del producto: nombre e imagen
            String productoNombre = item.optString("producto_nombre", "Producto no disponible");
            String productoImagen = item.optString("producto_imagen", "");

            // Configurar el nombre del producto en el TextView
            TextView productoNombreTextView = convertView.findViewById(R.id.textViewProductoNombre);
            productoNombreTextView.setText(productoNombre);

            // Configurar la imagen del producto en el ImageView
            ImageView productoImageView = convertView.findViewById(R.id.imageViewProducto);
            if (!productoImagen.isEmpty()) {
                String imageUrl = "https://api.happypetshco.com/ServidorProductos/" + productoImagen;
                Glide.with(context)
                        .load(imageUrl)
                        .into(productoImageView);
            }

            // Configurar el código de operación
            TextView codigoOperacionTextView = convertView.findViewById(R.id.textViewCodigoOperacion);
            String codigoOperacion = item.optString("codigo_operacion", "No disponible");
            codigoOperacionTextView.setText(codigoOperacion);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }

}
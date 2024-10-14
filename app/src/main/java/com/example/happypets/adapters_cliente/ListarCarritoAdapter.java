package com.example.happypets.adapters_cliente;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.happypets.R;

import org.json.JSONObject;

import java.util.ArrayList;

public class ListarCarritoAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<JSONObject> productos;

    public ListarCarritoAdapter(Context context, ArrayList<JSONObject> productos) {
        this.context = context;
        this.productos = productos;
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

        try {
            JSONObject producto = productos.get(position);
            JSONObject detalleProducto = producto.getJSONObject("producto");

            // Configurar detalles del producto
            tvNombreProducto.setText(detalleProducto.getString("nm_producto"));
            tvCantidad.setText("Cantidad: " + producto.getString("cantidad"));
            tvColor.setText("Color: " + producto.getString("color"));
            tvImporte.setText("Importe: S/ " + producto.getString("importe"));

            // Cargar la imagen del producto usando Glide
            String imagenUrl = "https://api-happypetshco-com.preview-domain.com/ServidorProductos/" + detalleProducto.getString("imagen");
            Glide.with(context)
                    .load(imagenUrl)
                    .placeholder(R.drawable.logo) // Imagen por defecto mientras se carga
                    .error(R.drawable.logo) // Imagen en caso de error
                    .into(ivProducto);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }
}

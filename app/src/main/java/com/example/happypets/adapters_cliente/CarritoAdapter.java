package com.example.happypets.adapters_cliente;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.example.happypets.R;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CarritoAdapter extends DialogFragment {
    private String userId;
    private String productId;
    private String productPrice;
    private String token;
    private String imagenUrl; // Variable para almacenar la URL de la imagen
    private TextView textViewImporte;
    private EditText editTextColor;
    private String[] coloresArray;

    public static CarritoAdapter newInstance(String userId, String productId, String productPrice, String token, String colores, String imagenUrl) {
        CarritoAdapter fragment = new CarritoAdapter();
        Bundle args = new Bundle();
        args.putString("USER_ID", userId);
        args.putString("PRODUCT_ID", productId);
        args.putString("PRODUCT_PRICE", productPrice);
        args.putString("TOKEN", token);
        args.putString("colores", colores);
        args.putString("IMAGEN_URL", imagenUrl); // Agregar URL de imagen
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_carrito, container, false);

        if (getArguments() != null) {
            userId = getArguments().getString("USER_ID");
            productId = getArguments().getString("PRODUCT_ID");
            productPrice = getArguments().getString("PRODUCT_PRICE");
            token = getArguments().getString("TOKEN");
            String colores = getArguments().getString("colores");
            imagenUrl = getArguments().getString("IMAGEN_URL"); // Obtener la URL de la imagen

            // Separar los colores en un array
            coloresArray = colores.split(",");

            textViewImporte = view.findViewById(R.id.textViewImporte);
            editTextColor = view.findViewById(R.id.editTextColor);

            // Mostrar la imagen del producto
            ImageView imageViewProduct = view.findViewById(R.id.imageViewProduct); // Asegúrate de tener este ImageView en tu layout
            Glide.with(this)
                    .load(imagenUrl)
                    .into(imageViewProduct); // Cargar la imagen usando Glide

            // Mostrar círculos de colores
            showColorCircles(view);

            // Mostrar precio del producto
            TextView textViewProductPrice = view.findViewById(R.id.textViewProductPrice);
            textViewProductPrice.setText("Precio: S/. " + productPrice);
        }

        // Configurar el botón de cerrar
        Button buttonClose = view.findViewById(R.id.buttonClose);
        buttonClose.setOnClickListener(v -> dismiss());

        // Configurar el EditText de cantidad
        EditText editTextCantidad = view.findViewById(R.id.editTextCantidad);
        editTextCantidad.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateImporte(editTextCantidad.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        Button buttonAddToCart = view.findViewById(R.id.buttonAddToCart);
        buttonAddToCart.setOnClickListener(v -> addToCart(editTextCantidad.getText().toString()));

        return view;
    }

    private void showColorCircles(View view) {
        // Aquí se muestran los círculos para cada color en el array
        for (String color : coloresArray) {
            // Crear un círculo con borde
            View colorCircle = new View(getContext());
            int size = 100; // Cambia el tamaño según sea necesario
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(size, size);
            colorCircle.setLayoutParams(layoutParams);

            // Usar un Drawable para crear el círculo con borde
            colorCircle.setBackground(createCircleDrawable(color.trim()));

            // Configurar márgenes para separar los círculos
            ViewGroup.MarginLayoutParams marginParams = new ViewGroup.MarginLayoutParams(layoutParams);
            marginParams.setMargins(16, 16, 16, 16); // Ajusta los valores para el margen según sea necesario
            colorCircle.setLayoutParams(marginParams);

            // Agregar un OnClickListener al círculo de color
            colorCircle.setOnClickListener(v -> {
                // Cambiar el color en el EditText sin modificar el fondo
                editTextColor.setText(color.trim());
            });

            // Agrega el círculo al layout donde deseas mostrarlo
            ((ViewGroup) view.findViewById(R.id.colorCirclesContainer)).addView(colorCircle);
        }
    }

    private Drawable createCircleDrawable(String colorName) {
        int color = getColorFromName(colorName);

        // Crear un drawable de forma circular
        GradientDrawable circleDrawable = new GradientDrawable();
        circleDrawable.setShape(GradientDrawable.OVAL);
        circleDrawable.setColor(color);

        // Configurar el borde para el color blanco
        if (color == Color.WHITE) {
            circleDrawable.setStroke(5, Color.BLACK); // 5 es el grosor del borde
        }

        return circleDrawable;
    }

    private int getColorFromName(String colorName) {
        int colorValue = Color.TRANSPARENT; // Valor por defecto

        switch (colorName.toLowerCase()) {
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
                colorValue = Color.TRANSPARENT; // En caso de que el color no coincida
                break;
        }

        return colorValue; // Devolver el valor de color
    }

    private void updateImporte(String cantidadStr) {
        try {
            int cantidad = Integer.parseInt(cantidadStr);
            if (cantidad < 0) {
                cantidad = 0;
            }
            double price = Double.parseDouble(productPrice);
            double importe = cantidad * price;
            textViewImporte.setText("Importe: S/. " + String.format("%.2f", importe));
        } catch (NumberFormatException e) {
            textViewImporte.setText("Importe: S/. 0.00");
        }
    }

    private void addToCart(String cantidadStr) {
        int cantidad;
        try {
            cantidad = Integer.parseInt(cantidadStr);
            if (cantidad <= 0) {
                showToast("La cantidad debe ser mayor que 0");
                return;
            }
        } catch (NumberFormatException e) {
            showToast("Cantidad no válida");
            return;
        }

        String color = editTextColor.getText().toString().trim();
        double importe = cantidad * Double.parseDouble(productPrice);

        // Crear el JSON para enviar al servidor
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("cantidad", cantidad);
            jsonObject.put("color", color);
            jsonObject.put("importe", importe);
            jsonObject.put("id_producto", productId);
            jsonObject.put("id_usuario", userId);
        } catch (JSONException e) {
            e.printStackTrace();
            showToast("Error al crear el objeto JSON");
            return;
        }

        // Enviar la solicitud POST a la API
        new Thread(() -> {
            try {
                URL url = new URL("https://api.happypetshco.com/api/AgregarCarrito");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + token); // Usar el token en el encabezado
                conn.setDoOutput(true);

                // Enviar el JSON
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonObject.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // Leer la respuesta del servidor
                if (conn.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
                    showToast("Producto agregado al carrito correctamente");
                    dismiss(); // Cerrar el diálogo
                } else {
                    showToast("Error al agregar el producto al carrito");
                }

            } catch (Exception e) {
                e.printStackTrace();
                showToast("Error en la conexión");
            }
        }).start();
    }

    // Método para mostrar un Toast en el hilo principal
    private void showToast(final String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
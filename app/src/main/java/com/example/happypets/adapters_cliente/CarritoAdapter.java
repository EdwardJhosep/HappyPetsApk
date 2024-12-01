package com.example.happypets.adapters_cliente;

import android.app.AlertDialog;
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
    private String imagenUrl;
    private TextView textViewImporte;
    private EditText editTextColor;
    private String[] coloresArray;

    private View selectedColorCircle = null;

    public static CarritoAdapter newInstance(String userId, String productId, String productPrice, String token, String colores, String imagenUrl, String stock) {
        CarritoAdapter fragment = new CarritoAdapter();
        Bundle args = new Bundle();
        args.putString("USER_ID", userId);
        args.putString("PRODUCT_ID", productId);
        args.putString("PRODUCT_PRICE", productPrice);
        args.putString("TOKEN", token);
        args.putString("colores", colores);
        args.putString("IMAGEN_URL", imagenUrl);
        args.putString("STOCK", stock); // Add stock to the Bundle
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_carrito, container, false);
        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.card_background);

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
            textViewProductPrice.setText("S/. " + productPrice);
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

                // Si ya hay un círculo seleccionado, quitarle el borde
                if (selectedColorCircle != null) {
                    selectedColorCircle.setBackground(createCircleDrawable(((TextView) selectedColorCircle.getTag()).getText().toString())); // Reestablecer el círculo previo
                }

                // Marcar el nuevo círculo como seleccionado, cambiando su borde
                selectedColorCircle = colorCircle;
                selectedColorCircle.setBackground(createCircleDrawableWithBorder(color.trim())); // Aplicar borde destacado

            });

            // Guarda el color del círculo en el tag para más tarde
            TextView colorText = new TextView(getContext());
            colorText.setText(color.trim());
            colorCircle.setTag(colorText);

            // Agrega el círculo al layout donde deseas mostrarlo
            ((ViewGroup) view.findViewById(R.id.colorCirclesContainer)).addView(colorCircle);
        }
    }

    // Método para crear un círculo con borde destacado
    private Drawable createCircleDrawableWithBorder(String colorName) {
        int color = getColorFromName(colorName);

        // Crear un drawable de forma circular
        GradientDrawable circleDrawable = new GradientDrawable();
        circleDrawable.setShape(GradientDrawable.OVAL);
        circleDrawable.setColor(color);

        // Configurar el borde para el color seleccionado
        circleDrawable.setStroke(8, Color.BLACK); // Cambiar el grosor del borde para resaltar el círculo

        return circleDrawable;
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
                showAlert("La cantidad debe ser mayor que 0");
                return;
            }

            // Verificar si la cantidad solicitada excede el stock
            int stockDisponible = Integer.parseInt(getArguments().getString("STOCK"));
            if (cantidad > stockDisponible) {
                // Si la cantidad excede el stock, preguntar al usuario si desea continuar con la cantidad disponible
                showStockDialog(stockDisponible);
                return; // No continuar con el proceso hasta que el usuario confirme
            }
        } catch (NumberFormatException e) {
            showAlert("Cantidad no válida");
            return;
        }

        // Obtener el color seleccionado
        String color = editTextColor.getText().toString().trim();

        // Verificar si se ha seleccionado un color
        if (color.isEmpty()) {
            showAlert("Debe seleccionar un color");
            return;
        }

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
            showAlert("Error al crear el objeto JSON");
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
                    showAlert("Error al agregar el producto al carrito");
                }

            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error en la conexión");
            }
        }).start();
    }

    private void showStockDialog(int stockDisponible) {
        new AlertDialog.Builder(getContext())
                .setTitle("Stock insuficiente")
                .setMessage("Solo hay " + stockDisponible + " unidades disponibles. ¿Deseas agregar esa cantidad al carrito?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // Si el usuario acepta, agregar la cantidad disponible al carrito
                    addToCart(String.valueOf(stockDisponible));
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void showToast(final String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show());
        }
    }
    private void showAlert(String message) {
        new AlertDialog.Builder(getContext())
                .setTitle("¡Error!")
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert) // Icono de advertencia
                .setPositiveButton("OK", null)
                .create()
                .show();
    }


    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            // Ajusta el tamaño del diálogo a un valor razonable
            getDialog().getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.95),  // 95% del ancho de la pantalla
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}
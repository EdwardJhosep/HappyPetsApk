package com.example.happypets.adapters_cliente;

import android.graphics.Color; // Asegúrate de importar esto
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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast; // Importa Toast

import com.example.happypets.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CarritoAdapter extends DialogFragment {

    private String userId;
    private String productId;
    private String productPrice; // Variable para precio del producto
    private TextView textViewImporte; // Añadir variable para el TextView del importe
    private EditText editTextColor; // Añadir variable para el EditText del color

    public static CarritoAdapter newInstance(String userId, String productId, String productPrice) {
        CarritoAdapter fragment = new CarritoAdapter();
        Bundle args = new Bundle();
        args.putString("USER_ID", userId);
        args.putString("PRODUCT_ID", productId);
        args.putString("PRODUCT_PRICE", productPrice); // Agregar el precio del producto
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
            productPrice = getArguments().getString("PRODUCT_PRICE"); // Obtener el precio del producto

            // Vincular los TextViews y establecer el texto
            TextView textViewProductPrice = view.findViewById(R.id.textViewProductPrice);
            textViewImporte = view.findViewById(R.id.textViewImporte); // Inicializar el TextView del importe
            editTextColor = view.findViewById(R.id.editTextColor); // Inicializar el EditText del color

            // Mostrar precio del producto
            textViewProductPrice.setText("Precio: S/. " + productPrice);
        }

        // Configurar el botón de cerrar
        Button buttonClose = view.findViewById(R.id.buttonClose);
        buttonClose.setOnClickListener(v -> dismiss()); // Cerrar el diálogo al hacer clic

        // Configurar el EditText de cantidad
        EditText editTextCantidad = view.findViewById(R.id.editTextCantidad);
        editTextCantidad.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Actualizar el importe cuando la cantidad cambie
                updateImporte(editTextCantidad.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Configurar los botones de color
        setupColorButtons(view);

        // Configurar el botón de añadir al carrito
        Button buttonAddToCart = view.findViewById(R.id.buttonAddToCart);
        buttonAddToCart.setOnClickListener(v -> addToCart(editTextCantidad.getText().toString()));

        return view;
    }

    private void setupColorButtons(View view) {
        ImageButton buttonColorRed = view.findViewById(R.id.buttonColorRed);
        ImageButton buttonColorGreen = view.findViewById(R.id.buttonColorGreen);
        ImageButton buttonColorBlue = view.findViewById(R.id.buttonColorBlue);
        ImageButton buttonColorOtro = view.findViewById(R.id.buttonColorOtro);

        buttonColorRed.setOnClickListener(v -> setColorToEditText(Color.RED, "Rojo"));
        buttonColorGreen.setOnClickListener(v -> setColorToEditText(Color.GREEN, "Verde"));
        buttonColorBlue.setOnClickListener(v -> setColorToEditText(Color.BLUE, "Azul"));
        buttonColorOtro.setOnClickListener(v -> setColorToEditText(Color.BLACK, "Otro")); // Cambiar a negro
    }

    private void setColorToEditText(int color, String colorName) {
        editTextColor.setText(colorName);
        editTextColor.setTextColor(color); // Cambiar el color del texto del EditText
    }

    private void updateImporte(String cantidadStr) {
        try {
            int cantidad = Integer.parseInt(cantidadStr);
            if (cantidad < 0) {
                cantidad = 0; // Evitar cantidades negativas
            }
            double price = Double.parseDouble(productPrice);
            double importe = cantidad * price; // Calcular el importe
            textViewImporte.setText("Importe: S/. " + String.format("%.2f", importe)); // Mostrar el importe
        } catch (NumberFormatException e) {
            textViewImporte.setText("Importe: S/. 0.00"); // Mostrar 0 si la cantidad no es válida
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
                URL url = new URL("https://api-happypetshco-com.preview-domain.com/api/AñadirCarrito");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
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

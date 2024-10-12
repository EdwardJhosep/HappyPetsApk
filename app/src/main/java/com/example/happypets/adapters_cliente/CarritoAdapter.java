package com.example.happypets.adapters_cliente;

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
import android.widget.TextView;

import com.example.happypets.R;

public class CarritoAdapter extends DialogFragment {

    private String userId;
    private String productId;
    private String productPrice; // Variable para precio del producto
    private TextView textViewImporte; // Añadir variable para el TextView del importe

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

        return view;
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

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}

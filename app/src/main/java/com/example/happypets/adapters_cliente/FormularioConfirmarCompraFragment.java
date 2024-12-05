package com.example.happypets.adapters_cliente;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.happypets.R;
import com.example.happypets.view_cliente.MenuCliente;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class FormularioConfirmarCompraFragment extends BottomSheetDialogFragment {

    private static final String ARG_PRODUCTOS_IDS = "productos_ids";
    private static final String ARG_TOTAL = "total";
    private TextView txtSelectedProductIds;
    private TextView txtTotal;

    // Método para crear una nueva instancia del fragmento
    public static FormularioConfirmarCompraFragment newInstance(ArrayList<String> selectedProductIds, double total, String token) {
        FormularioConfirmarCompraFragment fragment = new FormularioConfirmarCompraFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_PRODUCTOS_IDS, selectedProductIds);
        args.putDouble(ARG_TOTAL, total);
        args.putString("token", token); // Agregar el token a los argumentos
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_formulario_confirmar_compra, container, false);

        // Obtener los TextViews donde se mostrarán los datos
        txtSelectedProductIds = view.findViewById(R.id.tvProductIds);
        txtTotal = view.findViewById(R.id.tvTotal);

        // Obtener los datos de los argumentos
        ArrayList<String> selectedProductIds = getArguments().getStringArrayList(ARG_PRODUCTOS_IDS);
        double total = getArguments().getDouble(ARG_TOTAL);
        String token = getArguments().getString("token");

        // Mostrar los productos seleccionados y el total
        if (selectedProductIds != null && !selectedProductIds.isEmpty()) {
            StringBuilder ids = new StringBuilder();
            for (String id : selectedProductIds) {
                ids.append(id).append("\n");
            }
            txtSelectedProductIds.setText(ids.toString());
        } else {
            txtSelectedProductIds.setText("No se han seleccionado productos.");
        }

        txtTotal.setText("Total: S/ " + String.format("%.2f", total));

        Button btnFinalizarCompra = view.findViewById(R.id.btnFinalizarCompra);
        btnFinalizarCompra.setOnClickListener(v -> {
            new EnviarConfirmacionPedidoTask(selectedProductIds, total, token, getContext(), FormularioConfirmarCompraFragment.this).execute();
        });

        return view;
    }

    private static class EnviarConfirmacionPedidoTask extends AsyncTask<Void, Void, String> {
        private ArrayList<String> selectedProductIds;
        private double total;
        private String token;
        private Context context;
        private FormularioConfirmarCompraFragment fragment; // Referencia al fragmento

        EnviarConfirmacionPedidoTask(ArrayList<String> selectedProductIds, double total, String token, Context context, FormularioConfirmarCompraFragment fragment) {
            this.selectedProductIds = selectedProductIds;
            this.total = total;
            this.token = token;
            this.context = context;
            this.fragment = fragment;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                // URL del endpoint
                URL url = new URL("https://api.happypetshco.com/api/ConfirmarPedido");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                // Configurar la conexión
                urlConnection.setRequestMethod("PUT");
                urlConnection.setRequestProperty("Authorization", "Bearer " + token);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setDoOutput(true);

                // Crear el JSON para enviar
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("estado", "Pagado");
                jsonObject.put("tipo_pago", "Efectivo");

                // Agregar los IDs de los productos al array "carritos"
                JSONArray productosArray = new JSONArray();
                for (String id : selectedProductIds) {
                    productosArray.put(id);
                }
                jsonObject.put("carritos", productosArray);

                // Enviar los datos al servidor
                try (OutputStream os = urlConnection.getOutputStream()) {
                    byte[] input = jsonObject.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // Obtener la respuesta
                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return "Pedido confirmado correctamente.";
                } else {
                    return "Error al confirmar el pedido. Código de error: " + responseCode;
                }

            } catch (Exception e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result.equals("Pedido confirmado correctamente.")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("¡Pedido Confirmado!")
                        .setMessage("Puedes ir a recoger tu pedido en la tienda física HappyPets - Jirón Aguilar 649, Huánuco.")
                        .setPositiveButton("Aceptar", (dialog, which) -> {
                            Intent intent = new Intent(context, MenuCliente.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.putExtra("token", token);
                            context.startActivity(intent);
                            fragment.dismiss();
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                // Mostrar mensaje de error en caso de fallo
                Toast.makeText(context, result, Toast.LENGTH_LONG).show();
            }
        }
    }
}

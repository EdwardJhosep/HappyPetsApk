package com.example.happypets.adapters_cliente;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import com.example.happypets.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class CarritoFragment extends BottomSheetDialogFragment implements ListarCarritoAdapter.OnSelectedProductsChangedListener {

    private String userId;
    private String token;
    private ArrayList<JSONObject> productos = new ArrayList<>();
    private ListView listView;
    private OnProductosSeleccionadosListener listener;
    private ArrayList<String> selectedProductIds = new ArrayList<>();
    private OnTotalCalculadoListener totalListener;

    // Interfaz para la selección de productos
    public interface OnProductosSeleccionadosListener {
        void onProductosSeleccionados(ArrayList<String> idsSeleccionados);
    }

    // Interfaz para el cálculo del total
    public interface OnTotalCalculadoListener {
        void onTotalCalculado(double total);  // Método para manejar el total calculado
    }

    // Método para crear una nueva instancia del fragmento
    public static CarritoFragment newInstance(String userId, String token) {
        CarritoFragment fragment = new CarritoFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("token", token); // Agregar token a los argumentos
        fragment.setArguments(args);
        return fragment;
    }

    // Configurar la interfaz para comunicación con el fragmento
    public void setOnProductosSeleccionadosListener(OnProductosSeleccionadosListener listener) {
        this.listener = listener;
    }

    // Configurar el listener para el total calculado
    public void setOnTotalCalculadoListener(OnTotalCalculadoListener listener) {
        this.totalListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.listarcarritocliente, container, false);
        listView = view.findViewById(R.id.listViewCarrito);

        // Obtener el userId y token de los argumentos
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
            token = getArguments().getString("token"); // Obtener el token
            new ListarCarritoTask().execute(userId);
        }

        // Configurar el botón "Confirmar compra"
        Button btnConfirmarCompra = view.findViewById(R.id.btnConfirmarCompra);
        btnConfirmarCompra.setOnClickListener(v -> confirmarCompra());

        return view;
    }

    private void confirmarCompra() {
        // Obtener el TextView del total
        TextView totalTextView = getView().findViewById(R.id.tvTotal);

        if (totalTextView != null) {
            // Obtener el texto del TextView, que es el total calculado
            String totalText = totalTextView.getText().toString();

            // Extraer el valor numérico del total (asumiendo que el formato es "Total: S/ XX.XX")
            String totalString = totalText.replace("Total: S/ ", "").trim();

            try {
                double total = Double.parseDouble(totalString);  // Convertir el string a double

                // Verificar si hay productos seleccionados
                if (!selectedProductIds.isEmpty()) {
                    // Mostrar los productos seleccionados como un Toast (puedes personalizar esto más)
                    Toast.makeText(getActivity(), "IDs seleccionados: " + selectedProductIds, Toast.LENGTH_SHORT).show();

                    // Enviar el total al listener
                    if (totalListener != null) {
                        totalListener.onTotalCalculado(total);  // Enviar el total al listener
                    }

                    // Crear un nuevo fragmento y pasar el total al mismo
                    FormularioConfirmarCompraFragment formFragment = FormularioConfirmarCompraFragment.newInstance(selectedProductIds, total, token);
                    formFragment.show(getChildFragmentManager(), formFragment.getTag()); // Usar getChildFragmentManager() para fragmentos dentro de otro fragmento
                } else {
                    Toast.makeText(getActivity(), "No se han seleccionado productos.", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Error al obtener el total", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), "Error al acceder al total", Toast.LENGTH_SHORT).show();
        }
    }

    private class ListarCarritoTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String userId = params[0];
            String apiUrl = "https://api.happypetshco.com/api/MostrarCarrito=" + userId;

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + token); // Establecer el token en el encabezado
                connection.setDoInput(true);
                connection.connect();

                // Verificar el código de respuesta
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Leer la respuesta
                    try (InputStream inputStream = connection.getInputStream();
                         InputStreamReader reader = new InputStreamReader(inputStream)) {
                        StringBuilder response = new StringBuilder();
                        int data;
                        while ((data = reader.read()) != -1) {
                            response.append((char) data);
                        }
                        return response.toString();
                    }
                } else {
                    return null; // Retornar null si la respuesta no es OK
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);

                    // Verificar si la respuesta contiene el campo 'carrito'
                    if (jsonResponse.has("carrito")) {
                        JSONArray jsonArray = jsonResponse.getJSONArray("carrito");

                        if (jsonArray.length() > 0) { // Verificar que el carrito no esté vacío
                            boolean tieneProductosPendientes = false;

                            productos.clear();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject item = jsonArray.getJSONObject(i);

                                // Verificar si el producto tiene el campo "estado" como "Pendiente"
                                String estado = item.optString("estado", "");
                                if (estado.equals("Pendiente")) {
                                    productos.add(item);
                                    tieneProductosPendientes = true;
                                }
                            }

                            if (tieneProductosPendientes) {
                                // Crear el adaptador y configurarlo en el ListView
                                ListarCarritoAdapter adapter = new ListarCarritoAdapter(getActivity(), productos, userId, token);
                                adapter.setOnSelectedProductsChangedListener(CarritoFragment.this);  // Establecer el listener
                                listView.setAdapter(adapter);
                            } else {
                                Toast.makeText(getActivity(), "No hay productos Pendiente en el carrito", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getActivity(), "El carrito está vacío", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "No hay información del carrito", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Error al parsear la respuesta", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "Error en la conexión o el servidor", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Método de la interfaz para manejar la selección de productos
    @Override
    public void onSelectedProductsChanged(ArrayList<String> selectedProductIds, double total) {
        this.selectedProductIds = selectedProductIds; // Actualizar la lista de productos seleccionados

        Log.d("Total Carrito", "Total calculado: " + total);

        TextView totalTextView = getView().findViewById(R.id.tvTotal);  // Asume que tienes un TextView con id 'tvTotal'
        if (totalTextView != null) {
            totalTextView.setText("Total: S/ " + String.format("%.2f", total));  // Mostrar el total calculado
        }

        if (totalListener != null) {
            totalListener.onTotalCalculado(total);  // Notificar al listener el total calculado
        }
    }
}

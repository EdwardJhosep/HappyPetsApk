package com.example.happypets.adapters_cliente;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.happypets.R;
import com.example.happypets.models.ChatMessage;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChatFragment extends DialogFragment {

    private RecyclerView recyclerViewChat;
    private ChatAdapter chatAdapter;
    private EditText editTextMessage;
    private Map<String, ChatMessage> faqMap;
    private OkHttpClient client;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerViewChat = view.findViewById(R.id.recycler_view_chat);
        editTextMessage = view.findViewById(R.id.editTextMessage);
        Button buttonSend = view.findViewById(R.id.buttonSend);

        chatAdapter = new ChatAdapter();
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewChat.setAdapter(chatAdapter);

        client = new OkHttpClient();
        faqMap = new HashMap<>();
        loadProductData();

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        return view;
    }

    private void loadProductData() {
        Request request = new Request.Builder()
                .url("https://api.happypetshco.com/api/ListarProductos")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error al cargar datos de productos", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    getActivity().runOnUiThread(() -> parseProductData(responseBody));
                }
            }
        });
    }

    private void parseProductData(String responseBody) {
        JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonArray productosArray = jsonObject.getAsJsonArray("productos");

        for (int i = 0; i < productosArray.size(); i++) {
            JsonObject producto = productosArray.get(i).getAsJsonObject();
            String nombre = producto.get("nm_producto").getAsString();
            String descripcion = producto.get("descripcion").getAsString();
            String precio = producto.get("precio").getAsString();
            String imagenUrl = "https://api.happypetshco.com/ServidorProductos/" + producto.get("imagen").getAsString();

            String respuesta = "Producto: " + nombre + "\nDescripción: " + descripcion + "\nPrecio: $" + precio;
            faqMap.put(nombre.toLowerCase(), new ChatMessage(respuesta, false, imagenUrl));
        }
    }

    private void sendMessage() {
        String userMessage = editTextMessage.getText().toString().trim();

        if (!userMessage.isEmpty()) {
            chatAdapter.addMessage(new ChatMessage(userMessage, true, null)); // Mensaje del usuario

            ChatMessage response = getResponse(userMessage);
            chatAdapter.addMessage(response); // Mensaje de respuesta con posible imagen

            editTextMessage.setText("");
            recyclerViewChat.scrollToPosition(chatAdapter.getItemCount() - 1);
        }
    }

    private ChatMessage getResponse(String userMessage) {
        ChatMessage bestMatch = faqMap.get(userMessage.toLowerCase());
        return bestMatch != null ? bestMatch : new ChatMessage("Lo siento, no entiendo tu pregunta. ¿Podrías reformularla?", false, null);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
            params.gravity = Gravity.BOTTOM;
            params.y = 300;
            params.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.7);
            getDialog().getWindow().setAttributes(params);
        }
    }
}

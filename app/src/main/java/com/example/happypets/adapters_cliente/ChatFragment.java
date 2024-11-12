package com.example.happypets.adapters_cliente;

import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
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
    private MediaPlayer medioSound; // Audio de respuesta
    private MediaPlayer despedidaSound; // Audio de despedida
    private MediaPlayer bienvenidaSound; // Audio de bienvenida
    private boolean isAudioPlaying = false; // Controla si un audio está en reproducción

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.card_background);

        recyclerViewChat = view.findViewById(R.id.recycler_view_chat);
        editTextMessage = view.findViewById(R.id.editTextMessage);
        Button buttonSend = view.findViewById(R.id.buttonSend);
        Button buttonClose = view.findViewById(R.id.buttonClose);

        chatAdapter = new ChatAdapter(getContext());
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewChat.setAdapter(chatAdapter);

        client = new OkHttpClient();
        faqMap = new HashMap<>();
        loadProductData();

        buttonSend.setOnClickListener(v -> sendMessage());

        buttonClose.setOnClickListener(v -> {
            playFarewellSound(); // Reproducir el sonido de despedida
        });

        // Reproducir sonido de bienvenida cuando se abre el chat
        playWelcomeSound();

        return view;
    }

    private void playWelcomeSound() {
        if (!isAudioPlaying) {
            isAudioPlaying = true;
            bienvenidaSound = MediaPlayer.create(getContext(), R.raw.bienvenida);
            bienvenidaSound.start();

            bienvenidaSound.setOnCompletionListener(mediaPlayer -> {
                isAudioPlaying = false;
                mediaPlayer.release();
            });
        }
    }

    private void playFarewellSound() {
        if (!isAudioPlaying) {
            isAudioPlaying = true;
            despedidaSound = MediaPlayer.create(getContext(), R.raw.despedida);
            despedidaSound.start();

            despedidaSound.setOnCompletionListener(mediaPlayer -> {
                isAudioPlaying = false;
                mediaPlayer.release();
                dismiss(); // Cerrar el chat después de que el audio termine
            });
        }
    }

    private void loadProductData() {
        Request request = new Request.Builder()
                .url("https://api.happypetshco.com/api/ListarProductos")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error al cargar datos de productos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    getActivity().runOnUiThread(() -> parseProductData(responseBody));
                } else {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show();
                    });
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

            // Verificar si el producto tiene descuento
            String descuento = producto.has("descuento") ? producto.get("descuento").getAsString() : null;
            double precioOriginal = Double.parseDouble(precio);
            String precioConDescuento = precio;

            // Si el producto tiene descuento, calcular el precio con descuento
            if (descuento != null) {
                double descuentoValor = Double.parseDouble(descuento);  // Descuento en porcentaje
                double precioFinal = precioOriginal - (precioOriginal * descuentoValor / 100);
                precioConDescuento = String.format("%.2f", precioFinal); // Formatear el precio con descuento
            }

            // Construir el mensaje de respuesta
            String respuesta = "Producto: " + nombre + "\nDescripción: " + descripcion + "\nPrecio: S/" + precioConDescuento;

            faqMap.put(nombre.toLowerCase(), new ChatMessage(respuesta, false, imagenUrl));
        }
    }

    private void sendMessage() {
        String userMessage = editTextMessage.getText().toString().trim();

        if (!TextUtils.isEmpty(userMessage)) {
            chatAdapter.addMessage(new ChatMessage(userMessage, true, null));

            ChatMessage response = getResponse(userMessage);
            chatAdapter.addMessage(response);

            playResponseSound();

            editTextMessage.setText("");
            recyclerViewChat.scrollToPosition(chatAdapter.getItemCount() - 1);
        }
    }

    private ChatMessage getResponse(String userMessage) {
        ChatMessage bestMatch = faqMap.get(userMessage.toLowerCase());
        ChatMessage response;

        if (bestMatch != null) {
            response = bestMatch;
        } else {
            response = new ChatMessage("Lo siento, no entiendo tu pregunta. ¡Solo Ingrese El Nombre Del Producto¡", false, null);
            playErrorSound();  // Reproducir sonido de error cuando no se encuentra una respuesta
        }

        return response;
    }

    private void playErrorSound() {
        if (!isAudioPlaying) {
            isAudioPlaying = true;
            MediaPlayer errorSound = MediaPlayer.create(getContext(), R.raw.error);  // Cargar el archivo error.mp3
            errorSound.start();

            errorSound.setOnCompletionListener(mediaPlayer -> {
                isAudioPlaying = false;
                mediaPlayer.release();
            });
        }
    }


    private void playResponseSound() {
        if (!isAudioPlaying) {
            isAudioPlaying = true;
            medioSound = MediaPlayer.create(getContext(), R.raw.medio);
            medioSound.start();

            medioSound.setOnCompletionListener(mediaPlayer -> {
                isAudioPlaying = false;
                mediaPlayer.release();
            });
        }
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
            setCancelable(false); // Deshabilitar el cierre al hacer clic fuera
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (medioSound != null) {
            medioSound.release();
            medioSound = null;
        }
        if (despedidaSound != null) {
            despedidaSound.release();
            despedidaSound = null;
        }
        if (bienvenidaSound != null) {
            bienvenidaSound.release();
            bienvenidaSound = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (medioSound != null) {
            medioSound.release();
            medioSound = null;
        }
        if (despedidaSound != null) {
            despedidaSound.release();
            despedidaSound = null;
        }
        if (bienvenidaSound != null) {
            bienvenidaSound.release();
            bienvenidaSound = null;
        }
    }
}

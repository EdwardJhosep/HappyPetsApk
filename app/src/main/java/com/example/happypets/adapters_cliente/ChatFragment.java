package com.example.happypets.adapters_cliente;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.happypets.R;
import com.example.happypets.models.ChatMessage;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChatFragment extends DialogFragment {
    private static final String ARG_USER_NAME = "userName";
    private static final int REQUEST_MICROPHONE_PERMISSION = 1;

    private RecyclerView recyclerViewChat;
    private ChatAdapter chatAdapter;
    private EditText editTextMessage;
    private Map<String, ChatMessage> faqMap;
    private OkHttpClient client;
    private TextToSpeech textToSpeech;  // For speech synthesis
    private ImageButton imageButtonMicrophone;

    // Static method to create a new instance of ChatFragment with arguments
    public static ChatFragment newInstance(String userName) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_NAME, userName); // Passing the username as an argument
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.card_background);

        recyclerViewChat = view.findViewById(R.id.recycler_view_chat);
        editTextMessage = view.findViewById(R.id.editTextMessage);
        Button buttonSend = view.findViewById(R.id.buttonSend);
        Button buttonClose = view.findViewById(R.id.buttonClose);
        imageButtonMicrophone = view.findViewById(R.id.imageButtonMicrophone);

        chatAdapter = new ChatAdapter(getContext());
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewChat.setAdapter(chatAdapter);

        client = new OkHttpClient();
        faqMap = new HashMap<>();
        loadProductData();

        buttonSend.setOnClickListener(v -> sendMessage());

        buttonClose.setOnClickListener(v -> {
            String userName = getArguments().getString(ARG_USER_NAME); // Get the user's name
            String goodbyeMessage = "¡A SIDO UN PLACER AYUDARTE " + userName + "! Gracias por usar nuestro servicio.";

            speakOut(goodbyeMessage);

            new Handler().postDelayed(() -> dismiss(), 7000);
        });

        // Initialize TextToSpeech
        textToSpeech = new TextToSpeech(getContext(), new OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    // Configure language if needed
                    int langResult = textToSpeech.setLanguage(java.util.Locale.getDefault());
                    if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(getContext(), "Idioma no soportado para la síntesis de voz", Toast.LENGTH_SHORT).show();
                    } else {
                        // Get the user's name and greet
                        String userName = getArguments().getString(ARG_USER_NAME);
                        speakOut("¡Hola " + userName + "! ¿En qué producto estás pensando?");
                    }
                } else {
                    Toast.makeText(getContext(), "Error al inicializar el TextToSpeech", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set up microphone button listener
        imageButtonMicrophone.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, REQUEST_MICROPHONE_PERMISSION);
            } else {
                startVoiceRecognition();
            }
        });

        return view;
    }

    private void speakOut(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
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

            // Check if the product has a discount
            String descuento = producto.has("descuento") ? producto.get("descuento").getAsString() : null;
            double precioOriginal = Double.parseDouble(precio);
            String precioConDescuento = precio;

            // If the product has a discount, calculate the price with the discount
            if (descuento != null) {
                double descuentoValor = Double.parseDouble(descuento);  // Discount in percentage
                double precioFinal = precioOriginal - (precioOriginal * descuentoValor / 100);
                precioConDescuento = String.format("%.2f", precioFinal); // Format the price with discount
            }

            // Create the product object with associated tags
            List<String> tags = List.of(nombre.toLowerCase(), descripcion.toLowerCase()); // Add relevant tags
            faqMap.put(nombre.toLowerCase(), new ChatMessage("Producto: " + nombre + "\nDescripción: " + descripcion + "\nPrecio: " + precioConDescuento + " soles", false, imagenUrl, tags));
        }
    }

    private void sendMessage() {
        String userMessage = editTextMessage.getText().toString().trim();

        if (!TextUtils.isEmpty(userMessage)) {
            chatAdapter.addMessage(new ChatMessage(userMessage, true, null));

            ChatMessage response = getResponse(userMessage);
            chatAdapter.addMessage(response);

            speakOut(response.getText());

            editTextMessage.setText("");
            recyclerViewChat.scrollToPosition(chatAdapter.getItemCount() - 1);
        }
    }


    private ChatMessage getResponse(String userMessage) {
        ChatMessage bestMatch = null;
        List<String> similarProducts = new ArrayList<>();

        // Normalizar el mensaje del usuario
        String normalizedUserMessage = normalizeString(userMessage);

        // Primero, buscar una coincidencia exacta basada en el nombre del producto o etiquetas
        for (Map.Entry<String, ChatMessage> entry : faqMap.entrySet()) {
            ChatMessage chatMessage = entry.getValue();

            // Verificar coincidencia exacta en las etiquetas o nombre del producto
            if (chatMessage.getTags() != null) {
                for (String tag : chatMessage.getTags()) {
                    if (normalizeString(tag).equals(normalizedUserMessage)) {
                        bestMatch = chatMessage;
                        break;
                    }
                }
            }

            if (bestMatch != null) {
                break;
            }
        }

        // Si no se encuentra una coincidencia exacta, buscar productos similares
        if (bestMatch == null) {
            for (Map.Entry<String, ChatMessage> entry : faqMap.entrySet()) {
                ChatMessage chatMessage = entry.getValue();
                if (chatMessage.getTags() != null) {
                    for (String tag : chatMessage.getTags()) {
                        if (normalizeString(tag).contains(normalizedUserMessage)) {
                            similarProducts.add(chatMessage.getText());
                        }
                    }
                }
            }

            // Si hay productos similares, sugerirlos
            if (!similarProducts.isEmpty()) {
                bestMatch = new ChatMessage("¿Estás buscando alguno de estos productos? " + String.join(", ", similarProducts), false, null, null);
            } else {
                bestMatch = new ChatMessage("Lo siento, no encontré resultados para '" + userMessage + "'. ¿Puedo ayudarte con algo más?", false, null, null);
            }
        }

        return bestMatch;
    }

    // Método para normalizar cadenas
    private String normalizeString(String input) {
        if (input == null) {
            return "";
        }
        // Convertir a minúsculas, eliminar tildes y espacios en blanco
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", ""); // Eliminar tildes
        normalized = normalized.replaceAll("\\s+", ""); // Eliminar espacios en blanco
        return normalized.toLowerCase(); // Convertir a minúsculas
    }


    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Di algo...");

        startActivityForResult(intent, 100);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == getActivity().RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String spokenText = results.get(0);
                editTextMessage.setText(spokenText);
                sendMessage();
            }
        }
    }
}

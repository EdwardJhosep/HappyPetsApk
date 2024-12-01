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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
                        speakOut("¡Hola " + userName + "! ¿En qué puedo ayudarte?");
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

            // Use a helper method to handle possible JsonNull values
            String nombre = getJsonString(producto, "nm_producto");
            String descripcion = getJsonString(producto, "descripcion");
            String precio = getJsonString(producto, "precio");
            String imagenUrl = "https://api.happypetshco.com/ServidorProductos/" + getJsonString(producto, "imagen");

            // Check if the product has a discount (use getAsString() safely)
            String descuento = producto.has("descuento") ? getJsonString(producto, "descuento") : null;

            // Validate and parse the precio field safely
            double precioOriginal = parseDoubleSafe(precio); // Using the safe method to parse precio
            String precioConDescuento = String.valueOf(precioOriginal);

            // If the product has a discount, calculate the price with the discount
            if (descuento != null) {
                double descuentoValor = parseDoubleSafe(descuento);  // Discount in percentage
                double precioFinal = precioOriginal - (precioOriginal * descuentoValor / 100);
                precioConDescuento = String.format("%.2f", precioFinal); // Format the price with discount
            }

            List<String> tags = List.of(nombre.toLowerCase(), descripcion.toLowerCase()); // Add relevant tags
            faqMap.put(nombre.toLowerCase(), new ChatMessage(
                    "¡Tenemos el producto que buscas!\n\n" +
                            "Producto: " + nombre + "\nDescripción: " + descripcion + "\nPrecio: " + precioConDescuento + " soles\n\n" +
                            "Para realizar tu compra, solo tienes que dirigirte al apartado de productos, buscarlo y ¡listo!",
                    false,
                    imagenUrl,
                    tags
            ));
        }

        // Add the "horarios" entry to the FAQ map
// Añadir las entradas de FAQ con un enfoque informativo para la app o web
        faqMap.put("hola", new ChatMessage("¡Hola! Bienvenido a nuestra clínica veterinaria HappyPets. Puedes agendar citas, consultar nuestros servicios y mucho más desde nuestra app o página web. ¿En qué te puedo ayudar?", false, null, null));
        faqMap.put("adios", new ChatMessage("¡Hasta luego! Gracias por confiar en HappyPets. Recuerda que siempre puedes acceder a nuestra app o página web para más información.", false, null, null));
        faqMap.put("gracias", new ChatMessage("¡De nada! Estamos aquí para ayudarte. Recuerda que puedes realizar varias gestiones a través de nuestra app o web.", false, null, null));
        faqMap.put("cita", new ChatMessage("Claro, puedes agendar tu cita directamente desde nuestra app o página web. Solo selecciona el día y la hora que prefieras.", false, null, null));
        faqMap.put("horarios", new ChatMessage("Nuestros horarios de atención son de lunes a sábado, de 8:00 AM a 6:00 PM. Puedes agendar tu cita en cualquier momento a través de nuestra app o web. ¿Cómo puedo ayudarte?", false, null, null));
        faqMap.put("emergencia", new ChatMessage("Para emergencias, por favor llama directamente al número de contacto de emergencia: 987-654-321. También puedes solicitar ayuda urgente a través de nuestra app.", false, null, null));
        faqMap.put("servicios", new ChatMessage("Ofrecemos consultas, vacunación, desparasitación, cirugía, estética y más. Puedes obtener más detalles y agendar tus citas en nuestra app o página web.", false, null, null));
        faqMap.put("ubicacion", new ChatMessage("Estamos ubicados en Jirón Aguilar 649, Huánuco. Puedes consultar nuestra ubicación y agendar tu cita en nuestra app o web.", false, null, null));
        faqMap.put("vacunacion", new ChatMessage("Ofrecemos un esquema completo de vacunación. Puedes agendar una cita para vacunación directamente desde nuestra app o página web. ¿Te interesa?", false, null, null));
        faqMap.put("esterilizacion", new ChatMessage("La esterilización es un procedimiento seguro que ayuda a prevenir enfermedades y controlar la población. Si deseas más información o agendar una cita, puedes hacerlo fácilmente en nuestra app o web.", false, null, null));
        faqMap.put("alimentacion", new ChatMessage("Podemos recomendarte la dieta adecuada para tu mascota según su edad, raza y necesidades. Puedes consultar nuestras recomendaciones a través de la app o página web.", false, null, null));
        faqMap.put("bano", new ChatMessage("Ofrecemos servicios de baño, corte de pelo y estética para tu mascota. Puedes agendar tu turno para estos servicios cómodamente desde nuestra app o página web.", false, null, null));
        faqMap.put("vacaciones", new ChatMessage("Ofrecemos servicio de hospedaje para mascotas durante tus vacaciones. Puedes obtener más información y reservar el servicio a través de nuestra app o web.", false, null, null));
        faqMap.put("adopcion", new ChatMessage("Contamos con un programa de adopción para dar hogar a mascotas que lo necesitan. Si deseas conocer más sobre el proceso o adoptar una mascota, puedes consultar los detalles en nuestra app o página web.", false, null, null));
        faqMap.put("contacto", new ChatMessage("Puedes contactarnos al 123-456-789 o escribirnos por WhatsApp al mismo número. También puedes obtener toda la información que necesites desde nuestra app o página web.", false, null, null));
        faqMap.put("producto", new ChatMessage("Tenemos una amplia variedad de productos para el cuidado de tu mascota, desde alimentos hasta accesorios. Puedes consultar todos nuestros productos solo ingresando o diciendo el nombre", false, null, null));

    }


    // Helper method to get a string value from a JsonObject safely
    private String getJsonString(JsonObject jsonObject, String key) {
        JsonElement element = jsonObject.get(key);
        if (element != null && !element.isJsonNull()) {
            return element.getAsString();
        }
        return ""; // Return a default value if the element is null or doesn't exist
    }

    // Helper method to safely parse a double value, returns 0.0 if parsing fails
    private double parseDoubleSafe(String value) {
        try {
            if (value != null && !value.isEmpty()) {
                return Double.parseDouble(value);
            }
        } catch (NumberFormatException e) {
            // Handle the case where the value is not a valid double (e.g., empty string or non-numeric)
            e.printStackTrace(); // Optionally log the error
        }
        return 0.0; // Return a default value (e.g., 0.0) if the value is invalid
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

        // Inicializar los sinónimos si no se ha hecho ya
        if (synonymsMap.isEmpty()) {
            initializeSynonyms();
        }

        // Normalizar el mensaje del usuario
        String normalizedUserMessage = normalizeString(userMessage);

        // Buscar coincidencia exacta o sinónimos basados en las etiquetas o nombre del producto
        for (Map.Entry<String, ChatMessage> entry : faqMap.entrySet()) {
            ChatMessage chatMessage = entry.getValue();

            // Verificar si el mensaje del usuario coincide exactamente con alguna etiqueta o el texto
            if (chatMessage.getTags() != null) {
                for (String tag : chatMessage.getTags()) {
                    if (normalizeString(tag).equals(normalizedUserMessage)) {
                        bestMatch = chatMessage;
                        break;
                    }
                }
            }

            // Verificar si el mensaje contiene la palabra clave principal o alguno de sus sinónimos
            if (bestMatch == null) {
                // Buscar coincidencias con los sinónimos
                for (String synonym : synonymsMap.getOrDefault(entry.getKey(), Collections.emptyList())) {
                    if (normalizedUserMessage.contains(normalizeString(synonym))) {
                        bestMatch = chatMessage;
                        break;
                    }
                }
            }

            // Si se encuentra una coincidencia, detener la búsqueda
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
                        // Buscar productos cuyo tag contenga la palabra clave
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
    private Map<String, List<String>> synonymsMap = new HashMap<>();

    private void initializeSynonyms() {
        synonymsMap.put("hola", Arrays.asList("saludos", "buenos días", "hey","hola"));
        synonymsMap.put("adios", Arrays.asList("hasta luego", "adiós", "nos vemos","adios"));
        synonymsMap.put("gracias", Arrays.asList("de nada", "muchas gracias", "te lo agradezco","gracias"));
        synonymsMap.put("cita", Arrays.asList("agendar", "programar", "reservar cita","cita"));
        synonymsMap.put("horarios", Arrays.asList("horario de atención", "hora", "horario","horarios"));
        synonymsMap.put("emergencia", Arrays.asList("urgencia", "emergente", "situación urgente","emergencia"));
        synonymsMap.put("servicios", Arrays.asList("servicios disponibles", "atenciones", "tratamientos","servicios"));
        synonymsMap.put("ubicacion", Arrays.asList("dirección", "localización", "donde estamos","ubicacion","ubicado"));
        synonymsMap.put("vacunacion", Arrays.asList("vacuna", "vacunas", "esquema de vacunación","vacunacion"));
        synonymsMap.put("esterilizacion", Arrays.asList("esterilizar", "operación de esterilización", "esterilización","esterilizacion"));
        synonymsMap.put("alimentacion", Arrays.asList("dieta", "comida", "alimento","alimentacion"));
        synonymsMap.put("bano", Arrays.asList("baño", "higiene", "limpieza","bano"));
        synonymsMap.put("vacaciones", Arrays.asList("hospedaje", "vacaciones para mascotas", "alojamiento","vacaciones"));
        synonymsMap.put("adopcion", Arrays.asList("adoptar", "dar en adopción", "programa de adopción","adopcion"));
        synonymsMap.put("contacto", Arrays.asList("información de contacto", "cómo contactarnos", "teléfono","contacto"));
        synonymsMap.put("producto", Arrays.asList("artículo", "bien", "mercancía", "artículo disponible", "producto disponible","producto","venden"));

    }
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

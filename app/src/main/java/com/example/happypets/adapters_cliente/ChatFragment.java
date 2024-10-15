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

import com.example.happypets.R;
import com.example.happypets.models.ChatMessage;

import java.util.HashMap;
import java.util.Map;

public class ChatFragment extends DialogFragment {

    private RecyclerView recyclerViewChat;
    private ChatAdapter chatAdapter;
    private EditText editTextMessage;

    // Mapa de preguntas y respuestas
    private Map<String, String> faqMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerViewChat = view.findViewById(R.id.recycler_view_chat);
        editTextMessage = view.findViewById(R.id.editTextMessage);
        Button buttonSend = view.findViewById(R.id.buttonSend);

        // Inicializa el adaptador y el RecyclerView
        chatAdapter = new ChatAdapter();
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewChat.setAdapter(chatAdapter);

        // Configura el mapa de preguntas y respuestas
        initializeFAQMap();

        // Configura el botón de enviar
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        return view;
    }

    private void initializeFAQMap() {
        faqMap = new HashMap<>();
        faqMap.put("¿Cuáles son los horarios de atención?", "Nuestro horario es de 9 AM a 5 PM de lunes a viernes.");
        faqMap.put("¿Qué servicios ofrecen?", "Ofrecemos consultas, vacunaciones, y tratamientos veterinarios.");
        faqMap.put("¿Cómo puedo hacer una cita?", "Puedes hacer una cita llamando al 123-456-7890 o a través de nuestra página web.");
        faqMap.put("¿Tienen servicios de emergencia?", "Sí, tenemos servicios de emergencia disponibles 24/7.");
        faqMap.put("¿Cuál es la dirección de la clínica?", "Nuestra clínica está ubicada en Av. Salud 123, Ciudad.");
        faqMap.put("¿Qué tipo de mascotas atienden?", "Atendemos perros, gatos y otros animales pequeños.");
        faqMap.put("¿Qué debo hacer si mi mascota tiene una emergencia?", "Llama inmediatamente a la clínica y ven tan pronto como puedas.");
        faqMap.put("¿Cómo puedo saber si mi mascota necesita una consulta?", "Si notas cambios en el comportamiento o salud de tu mascota, es mejor consultar a un veterinario.");
        faqMap.put("¿Realizan cirugías?", "Sí, ofrecemos servicios quirúrgicos para mascotas.");
        faqMap.put("¿Ofrecen servicios de peluquería?", "Sí, contamos con servicios de peluquería para mascotas.");
        faqMap.put("¿Cómo puedo pagar mis servicios?", "Aceptamos efectivo, tarjetas de crédito y débito.");
        faqMap.put("¿Qué vacunas necesitan mis mascotas?", "Recomendamos vacunas para parvovirus, moquillo, hepatitis y rabia.");
        faqMap.put("¿Con qué frecuencia debo llevar a mi mascota al veterinario?", "Se recomienda una revisión anual, o más frecuente si hay problemas de salud.");
        faqMap.put("¿Ofrecen servicios de desparasitación?", "Sí, ofrecemos tratamientos de desparasitación para perros y gatos.");
        faqMap.put("¿Puedo llevar a mi mascota a la consulta?", "Sí, puedes llevar a tu mascota a todas las consultas.");
        faqMap.put("¿Qué alimentos son seguros para mi mascota?", "Recomendamos alimentos balanceados y específicos para cada tipo de mascota.");
        faqMap.put("¿Tienen un programa de fidelidad?", "Sí, ofrecemos descuentos a clientes frecuentes.");
        faqMap.put("¿Ofrecen consejos sobre adiestramiento?", "Sí, podemos ofrecerte recomendaciones para adiestrar a tu mascota.");
        faqMap.put("¿Qué hago si tengo problemas para administrar la medicación a mi mascota?", "Contacta a nuestra clínica para obtener asesoramiento sobre cómo administrarla.");
        faqMap.put("¿Puedo hacer una cita en línea?", "Sí, puedes reservar tu cita a través de nuestra página web.");
        faqMap.put("¿Cuánto tiempo dura una consulta?", "Una consulta típica dura entre 30 y 60 minutos.");
        faqMap.put("¿Ofrecen cuidados paliativos para mascotas?", "Sí, ofrecemos cuidados paliativos para mascotas enfermas.");
        faqMap.put("¿Tienen servicios de cremación?", "Sí, ofrecemos servicios de cremación para mascotas.");
        faqMap.put("¿Ofrecen productos para el cuidado de mascotas?", "Sí, tenemos una selección de productos de cuidado para mascotas en la clínica.");
        faqMap.put("¿Qué debo hacer si pierdo a mi mascota?", "Coloca carteles y publica en redes sociales; también puedes consultar refugios locales.");
        faqMap.put("¿Ofrecen servicios de transporte para mascotas?", "No ofrecemos transporte, pero podemos recomendarte servicios de terceros.");
        faqMap.put("¿Pueden atender a animales exóticos?", "Atendemos algunas especies exóticas, consulta con nuestro veterinario.");
        faqMap.put("¿Qué pruebas de diagnóstico realizan?", "Realizamos análisis de sangre, rayos X y ultrasonido.");
        faqMap.put("¿Qué debo hacer si tengo preguntas sobre la salud de mi mascota?", "Llama a la clínica para hablar con un veterinario.");
        faqMap.put("¿Pueden ayudarme con la selección de alimentos?", "Sí, podemos ayudarte a elegir la mejor alimentación para tu mascota.");
        faqMap.put("¿Hacen pruebas de alergia?", "Sí, realizamos pruebas para identificar alergias en mascotas.");
        faqMap.put("¿Qué cuidados especiales necesitan los animales ancianos?", "Los animales ancianos requieren chequeos más frecuentes y dietas específicas.");
        faqMap.put("¿Ofrecen servicios de rehabilitación?", "Sí, ofrecemos servicios de rehabilitación para mascotas con lesiones.");
        faqMap.put("¿Cómo puedo preparar a mi mascota para una cirugía?", "Consulta con nuestro veterinario sobre los cuidados previos a la cirugía.");
        faqMap.put("¿Realizan análisis de sangre?", "Sí, realizamos análisis de sangre en nuestra clínica.");
        faqMap.put("¿Ofrecen servicios de emergencia fuera del horario?", "Sí, tenemos un servicio de emergencia disponible las 24 horas.");
        faqMap.put("¿Qué debo hacer si mi mascota se intoxica?", "Llama a la clínica de inmediato; te darán instrucciones.");
        faqMap.put("¿Tienen veterinarios especializados?", "Sí, contamos con veterinarios especializados en diferentes áreas.");
        faqMap.put("¿Cómo elijo un buen veterinario?", "Consulta recomendaciones y revisa sus credenciales.");
        faqMap.put("¿Qué vacunas necesita un cachorro?", "Los cachorros requieren varias vacunas en sus primeros meses de vida.");
        faqMap.put("¿Qué hacer si mi mascota tiene pulgas?", "Consulta a nuestro veterinario para un tratamiento adecuado.");
        faqMap.put("¿Ofrecen servicios de esterilización?", "Sí, ofrecemos servicios de esterilización para mascotas.");
        faqMap.put("¿Qué debo llevar a la consulta?", "Trae la documentación médica de tu mascota y cualquier medicamento que esté tomando.");
        faqMap.put("¿Cuáles son los síntomas de enfermedad en mascotas?", "Los síntomas incluyen letargo, pérdida de apetito y vómitos.");
        faqMap.put("¿Tienen un servicio de asesoramiento sobre comportamiento?", "Sí, ofrecemos consultas sobre comportamiento animal.");
        faqMap.put("¿Pueden hacer análisis de orina?", "Sí, realizamos análisis de orina en nuestra clínica.");
        faqMap.put("¿Qué cuidados requieren las mascotas ancianas?", "Las mascotas ancianas necesitan chequeos regulares y atención especial.");
        faqMap.put("¿Realizan pruebas de sangre para detectar enfermedades?", "Sí, realizamos pruebas de sangre para detectar enfermedades comunes.");
        faqMap.put("¿Ofrecen servicios de consejo nutricional?", "Sí, ofrecemos asesoramiento sobre la nutrición adecuada para tu mascota.");
        faqMap.put("¿Puedo visitar la clínica sin cita previa?", "Sí, pero es recomendable pedir cita para evitar largas esperas.");
        faqMap.put("¿Qué cuidados necesitan los cachorros?", "Los cachorros requieren vacunas, desparasitaciones y socialización.");
        faqMap.put("¿Ofrecen tratamientos para la obesidad en mascotas?", "Sí, ofrecemos planes de tratamiento para mascotas con sobrepeso.");
        faqMap.put("¿Qué hacer si mi mascota tiene problemas dentales?", "Consulta a nuestro veterinario para un chequeo dental.");
        faqMap.put("¿Realizan exámenes físicos rutinarios?", "Sí, recomendamos exámenes físicos anuales para todas las mascotas.");
        faqMap.put("¿Qué hacer si tengo un gato que orina fuera de su caja?", "Consulta a nuestro veterinario para descartar problemas de salud.");
        faqMap.put("¿Pueden ayudarme a preparar a mi mascota para el viaje?", "Sí, ofrecemos consejos para viajar con tu mascota.");
        faqMap.put("¿Tienen servicios de cuidados post-quirúrgicos?", "Sí, ofrecemos cuidados especiales para mascotas después de una cirugía.");
        faqMap.put("¿Ofrecen tratamientos para enfermedades crónicas?", "Sí, trabajamos con mascotas con enfermedades crónicas.");
        faqMap.put("¿Qué hacer si mi mascota tiene diarrea?", "Consulta a nuestro veterinario si la diarrea persiste más de 24 horas.");
        faqMap.put("¿Realizan pruebas de diagnóstico por imagen?", "Sí, ofrecemos servicios de rayos X y ultrasonido.");
        faqMap.put("¿Tienen un programa de vacunación?", "Sí, seguimos un programa de vacunación recomendado para mascotas.");
        faqMap.put("¿Cómo trato los problemas de piel en mi mascota?", "Consulta a nuestro veterinario para un diagnóstico y tratamiento adecuado.");
        faqMap.put("¿Tienen un servicio de atención a mascotas adoptadas?", "Sí, ofrecemos servicios a mascotas que han sido adoptadas.");
        faqMap.put("¿Ofrecen consejos sobre la socialización de mascotas?", "Sí, ofrecemos consejos sobre la socialización de cachorros y gatos.");
        faqMap.put("¿Qué hacer si mi perro no quiere comer?", "Consulta a nuestro veterinario si tu perro no come durante más de un día.");
        faqMap.put("¿Ofrecen servicios de atención veterinaria a domicilio?", "Sí, ofrecemos atención veterinaria a domicilio en áreas seleccionadas.");
        faqMap.put("¿Qué cuidados especiales necesitan las mascotas rescatadas?", "Las mascotas rescatadas pueden necesitar atención médica adicional y socialización.");
        faqMap.put("¿Puedo preguntar sobre la salud de mi mascota por teléfono?", "Sí, puedes llamar y hablar con un veterinario para obtener asesoramiento.");
        faqMap.put("¿Tienen un servicio de odontología para mascotas?", "Sí, ofrecemos servicios de odontología para mascotas.");
        faqMap.put("¿Realizan chequeos de rutina para mascotas mayores?", "Sí, recomendamos chequeos regulares para mascotas mayores.");
        faqMap.put("¿Puedo recibir recordatorios para las citas de mi mascota?", "Sí, ofrecemos recordatorios por teléfono y mensaje de texto.");
        faqMap.put("¿Ofrecen pruebas de alergia para mascotas?", "Sí, realizamos pruebas para detectar alergias en mascotas.");
        faqMap.put("¿Qué hacer si tengo un perro agresivo?", "Consulta a nuestro veterinario o un especialista en comportamiento animal.");
        faqMap.put("¿Tienen un servicio de entrega de medicamentos para mascotas?", "No ofrecemos entrega, pero puedes recoger los medicamentos en la clínica.");
        faqMap.put("¿Ofrecen servicios de terapia para mascotas?", "Sí, ofrecemos servicios de terapia para mascotas con necesidades especiales.");
        faqMap.put("¿Qué hacer si mi gato tiene problemas de comportamiento?", "Consulta a nuestro veterinario para un diagnóstico y tratamiento adecuado.");
        faqMap.put("¿Ofrecen servicios de socialización para cachorros?", "Sí, ofrecemos clases de socialización para cachorros.");
        faqMap.put("¿Qué debo hacer si encuentro un animal herido?", "Llama a un refugio local o a nuestra clínica para obtener ayuda.");
    }


    private void sendMessage() {
        String userMessage = editTextMessage.getText().toString().trim();

        if (!userMessage.isEmpty()) {
            // Agrega el mensaje del usuario al adaptador
            chatAdapter.addMessage(new ChatMessage(userMessage, true)); // Mensaje del usuario

            // Busca la respuesta más apropiada
            String response = getResponse(userMessage);
            chatAdapter.addMessage(new ChatMessage(response, false)); // Respuesta del sistema

            editTextMessage.setText(""); // Limpia el campo de entrada
            recyclerViewChat.scrollToPosition(chatAdapter.getItemCount() - 1); // Desplaza la vista hacia el último mensaje
        }
    }

    private String getResponse(String userMessage) {
        String bestMatch = "";
        int bestMatchCount = 0;

        for (String question : faqMap.keySet()) {
            int matchCount = countMatchingWords(userMessage.toLowerCase(), question.toLowerCase());

            // Update best match if this question has more matching words
            if (matchCount > bestMatchCount) {
                bestMatchCount = matchCount;
                bestMatch = faqMap.get(question);
            }
        }

        // If no match found, return a default response
        if (bestMatchCount == 0) {
            return "Lo siento, no tengo una respuesta para eso. ¿Puedes reformular tu pregunta?";
        }
        return bestMatch;
    }

    // Helper method to count matching words
    private int countMatchingWords(String userMessage, String question) {
        String[] userWords = userMessage.split(" ");
        int matchCount = 0;

        for (String word : userWords) {
            if (question.contains(word)) {
                matchCount++;
            }
        }
        return matchCount;
    }
    @Override
    public void onStart() {
        super.onStart();
        // Ajustar el tamaño de la ventana de diálogo
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
            params.gravity = Gravity.BOTTOM; // Aparece desde abajo
            params.y = 300; // Ajusta este valor para que comience desde más arriba
            getDialog().getWindow().setAttributes(params);
        }
    }
}

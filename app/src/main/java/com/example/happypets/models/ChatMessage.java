package com.example.happypets.models;

import java.util.List;

public class ChatMessage {
    private String text;           // El texto del mensaje.
    private boolean isUserMessage; // Determina si el mensaje fue enviado por el usuario.
    private String imageUrl;       // URL de la imagen asociada al mensaje.
    private List<String> tags;     // Lista de etiquetas (puedes usar etiquetas como palabras clave).

    // Constructor para crear un mensaje con texto, tipo de mensaje, URL de la imagen y etiquetas.
    public ChatMessage(String text, boolean isUserMessage, String imageUrl, List<String> tags) {
        this.text = text;
        this.isUserMessage = isUserMessage;
        this.imageUrl = imageUrl;
        this.tags = tags;
    }

    // Constructor sobrecargado para crear un mensaje sin imagen y sin etiquetas.
    public ChatMessage(String text, boolean isUserMessage, Object o) {
        this(text, isUserMessage, null, null); // Llama al constructor principal con imagen y etiquetas como null.
    }

    // Método para obtener el texto del mensaje.
    public String getText() {
        return text;
    }

    // Método para verificar si el mensaje fue enviado por el usuario.
    public boolean isUserMessage() {
        return isUserMessage;
    }

    // Método para obtener la URL de la imagen asociada al mensaje.
    public String getImageUrl() {
        return imageUrl;
    }

    // Método que verifica si el mensaje tiene una imagen.
    public boolean hasImage() {
        return imageUrl != null;
    }

    // Método para obtener las etiquetas asociadas al mensaje.
    public List<String> getTags() {
        return tags;
    }

    // Método que agrega una etiqueta al mensaje.
    public void addTag(String tag) {
        if (tags != null) {
            tags.add(tag);
        }
    }

    // Método que verifica si el mensaje tiene etiquetas.
    public boolean hasTags() {
        return tags != null && !tags.isEmpty();
    }

    // Método que devuelve una representación de cadena del mensaje (útil para debugging o mostrar en logs).
    @Override
    public String toString() {
        return "ChatMessage{" +
                "text='" + text + '\'' +
                ", isUserMessage=" + isUserMessage +
                ", imageUrl='" + imageUrl + '\'' +
                ", tags=" + tags +
                '}';
    }
}

package com.example.happypets.models;

public class ChatMessage {
    private String text;
    private boolean isUserMessage;
    private String imageUrl; // Campo para la URL de la imagen

    // Constructor que acepta texto, tipo de mensaje y URL de imagen
    public ChatMessage(String text, boolean isUserMessage, String imageUrl) {
        this.text = text;
        this.isUserMessage = isUserMessage;
        this.imageUrl = imageUrl;
    }

    // Constructor sin imagen
    public ChatMessage(String text, boolean isUserMessage) {
        this(text, isUserMessage, null);
    }

    public String getText() {
        return text;
    }

    public boolean isUserMessage() {
        return isUserMessage;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean hasImage() {
        return imageUrl != null;
    }
}

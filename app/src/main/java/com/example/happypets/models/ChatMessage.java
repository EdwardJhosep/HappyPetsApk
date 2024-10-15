package com.example.happypets.models;

public class ChatMessage {
    private String text;
    private boolean isUserMessage;

    // Constructor que acepta texto y tipo de mensaje
    public ChatMessage(String text, boolean isUserMessage) {
        this.text = text;
        this.isUserMessage = isUserMessage;
    }

    public String getText() {
        return text;
    }

    public boolean isUserMessage() {
        return isUserMessage;
    }
}

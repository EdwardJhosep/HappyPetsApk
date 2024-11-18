package com.example.happypets.models;

public  class Notification {
    private String message;
    private String status;
    private String observations;
    private String id;


    public Notification(String id,String message, String status, String observations) {
        this.id = id;
        this.message = message;
        this.status = status;
        this.observations = observations;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

    public String getObservations() {
        return observations;
    }

    // Getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
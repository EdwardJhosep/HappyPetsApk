package com.example.happypets.models;

public class User {
    private String dni;
    private String nombres;
    private String telefono;
    private String ubicacion;

    public User(String dni, String nombres, String telefono, String ubicacion) {
        this.dni = dni;
        this.nombres = nombres;
        this.telefono = telefono;
        this.ubicacion = ubicacion;
    }

    // Getters
    public String getDni() {
        return dni;
    }

    public String getNombres() {
        return nombres;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    // Setters (opcional, según sea necesario)
    public void setDni(String dni) {
        this.dni = dni;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }
}

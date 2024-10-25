package com.example.happypets.models;

import java.util.List;

public class User {
    private String id; // Agregar el campo ID
    private String dni;
    private String nombres;
    private String telefono;
    private String ubicacion;
    private List<String> permisos; // Campo para los permisos

    // Modificar el constructor para incluir el ID
    public User(String id, String dni, String nombres, String telefono, String ubicacion, List<String> permisos) {
        this.id = id; // Inicializa el ID
        this.dni = dni;
        this.nombres = nombres;
        this.telefono = telefono;
        this.ubicacion = ubicacion;
        this.permisos = permisos; // Inicializa la lista de permisos
    }

    // Getters
    public String getId() {
        return id; // Agregar el getter para ID
    }

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

    public List<String> getPermisos() {
        return permisos; // Agregado para obtener permisos
    }

    // Setters (opcional)
    public void setId(String id) {
        this.id = id; // Agregar el setter para ID
    }

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

    public void setPermisos(List<String> permisos) {
        this.permisos = permisos; // Agregado para establecer permisos
    }
}

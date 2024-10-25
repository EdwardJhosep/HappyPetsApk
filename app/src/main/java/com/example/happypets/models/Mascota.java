package com.example.happypets.models;

public class Mascota {
    private String id; // Nuevo atributo para el ID
    private String nombre;
    private String edad;
    private String especie;
    private String raza;
    private String sexo;
    private String estado;
    private String imagen; // Atributo para la imagen

    // Constructor modificado para incluir el ID
    public Mascota(String id, String nombre, String edad, String especie, String raza, String sexo, String estado, String imagen) {
        this.id = id; // Inicializar el ID
        this.nombre = nombre;
        this.edad = edad;
        this.especie = especie;
        this.raza = raza;
        this.sexo = sexo;
        this.estado = estado;
        this.imagen = imagen; // Inicializar el atributo de imagen
    }

    // Getters
    public String getId() { // Getter para el nuevo atributo ID
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEdad() {
        return edad;
    }

    public String getEspecie() {
        return especie;
    }

    public String getRaza() {
        return raza;
    }

    public String getSexo() {
        return sexo;
    }

    public String getEstado() {
        return estado;
    }

    public String getImagen() { // Getter para el atributo de imagen
        return imagen;
    }

    @Override
    public String toString() {
        return "ID: " + id + "\n" + // Incluir el ID en la representación en cadena
                "Nombre: " + nombre + "\n" +
                "Edad: " + edad + "\n" +
                "Especie: " + especie + "\n" +
                "Raza: " + raza + "\n" +
                "Sexo: " + sexo + "\n" +
                "Estado: " + estado + "\n" +
                "Imagen: " + imagen; // Incluir la imagen en la representación en cadena
    }
}

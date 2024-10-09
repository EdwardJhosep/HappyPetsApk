package com.example.happypets.models;

public class Mascota {
    private String nombre;
    private String edad;
    private String especie;
    private String raza;
    private String sexo;
    private String estado;

    public Mascota(String nombre, String edad, String especie, String raza, String sexo, String estado) {
        this.nombre = nombre;
        this.edad = edad;
        this.especie = especie;
        this.raza = raza;
        this.sexo = sexo;
        this.estado = estado;
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

    @Override
    public String toString() {
        return "Nombre: " + nombre + "\n" +
                "Edad: " + edad + "\n" +
                "Especie: " + especie + "\n" +
                "Raza: " + raza + "\n" +
                "Sexo: " + sexo + "\n" +
                "Estado: " + estado;
    }
}

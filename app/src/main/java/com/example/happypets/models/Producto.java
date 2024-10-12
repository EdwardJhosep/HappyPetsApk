package com.example.happypets.models;

public class Producto {
    private int id;
    private String nombre;
    private String descripcion;
    private String categoria;
    private String precio;
    private String descuento; // Campo para el descuento
    private String stock;
    private String imagen;

    public Producto(int id, String nombre, String descripcion, String categoria, String precio, String descuento, String stock, String imagen) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.precio = precio;
        this.descuento = descuento; // Inicializa el descuento
        this.stock = stock;
        this.imagen = imagen;
    }

    // Métodos getter y setter
    public int getId() {  // Asegúrate de tener este método
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getPrecio() {
        return precio;
    }

    public void setPrecio(String precio) {
        this.precio = precio;
    }

    public String getDescuento() {
        return descuento; // Método getter para el descuento
    }

    public void setDescuento(String descuento) {
        this.descuento = descuento; // Método setter para el descuento
    }

    public String getStock() {
        return stock;
    }

    public void setStock(String stock) {
        this.stock = stock;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }
}

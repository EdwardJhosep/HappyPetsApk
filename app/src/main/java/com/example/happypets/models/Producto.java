package com.example.happypets.models;

public class Producto {
        private int id;
        private String nombre;
        private String descripcion;
        private String categoria;
        private String subCategoria; // Campo para la subcategoría
        private String subSubCategoria; // Campo para la sub-subcategoría
        private String precio;
        private String descuento;
        private String stock;
        private String imagen;
        private String colores; // Nuevo campo para los colores

        // Constructor actualizado
        public Producto(int id, String nombre, String descripcion, String categoria,
                        String subCategoria, String subSubCategoria,
                        String precio, String descuento, String stock,
                        String imagen, String colores) {
            this.id = id;
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.categoria = categoria;
            this.subCategoria = subCategoria; // Inicializa subcategoría
            this.subSubCategoria = subSubCategoria; // Inicializa sub-subcategoría
            this.precio = precio;
            this.descuento = descuento;
            this.stock = stock;
            this.imagen = imagen;
            this.colores = colores; // Inicializa el campo colores
        }

    // Métodos getter y setter
    public int getId() {
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



    public String getCategoria() {
        return categoria;
    }



    public String getPrecio() {
        return precio;
    }

    public void setPrecio(String precio) {
        this.precio = precio;
    }

    public String getDescuento() {
        return descuento;
    }

    public String getStock() {
        return stock;
    }



    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getColores() { // Método getter para colores
        return colores;
    }


    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getSubCategoria() {
        return subCategoria;
    }

    public void setSubCategoria(String subCategoria) {
        this.subCategoria = subCategoria;
    }

    public String getSubSubCategoria() {
        return subSubCategoria;
    }

    public void setSubSubCategoria(String subSubCategoria) {
        this.subSubCategoria = subSubCategoria;
    }

}
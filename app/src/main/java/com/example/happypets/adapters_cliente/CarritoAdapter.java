package com.example.happypets.adapters_cliente;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CarritoAdapter {
    private Map<String, List<Integer>> carrito; // Mapa que relaciona el ID del usuario con sus productos en el carrito

    public CarritoAdapter() {
        carrito = new HashMap<>();
    }

    // Método para agregar un producto al carrito
    public void agregarProducto(String productoId, String userId) {
        // Verificar si el carrito ya contiene el usuario
        if (!carrito.containsKey(userId)) {
            carrito.put(userId, new ArrayList<>()); // Inicializa la lista de productos si no existe
        }

        // Agregar el producto al carrito del usuario
        List<Integer> productosUsuario = carrito.get(userId);
        if (!productosUsuario.contains(Integer.parseInt(productoId))) {
            productosUsuario.add(Integer.parseInt(productoId));
            Log.d("CarritoAdapter", "Producto ID: " + productoId + " agregado al carrito de User ID: " + userId);
        } else {
            Log.d("CarritoAdapter", "El producto ID: " + productoId + " ya está en el carrito de User ID: " + userId);
        }
    }

    // Método para obtener los productos en el carrito de un usuario
    public List<Integer> obtenerProductos(String userId) {
        return carrito.getOrDefault(userId, new ArrayList<>()); // Devuelve la lista de productos o una lista vacía si no existen
    }

    // Método para vaciar el carrito de un usuario
    public void vaciarCarrito(String userId) {
        carrito.remove(userId);
    }
}

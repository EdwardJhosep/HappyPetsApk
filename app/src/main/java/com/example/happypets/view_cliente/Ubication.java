package com.example.happypets.view_cliente;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.happypets.R;

public class Ubication extends Fragment {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private WebView webView;
    private ProgressBar progressBar;
    private TextView loadingMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflar el layout para este fragmento
        View view = inflater.inflate(R.layout.activity_ubication, container, false);

        // Encontrar el WebView, el ProgressBar y el TextView en el layout
        webView = view.findViewById(R.id.webview);
        progressBar = view.findViewById(R.id.progressBar);
        loadingMessage = view.findViewById(R.id.loadingMessage); // Inicializa el TextView para mostrar el mensaje de carga

        // Configurar ajustes de WebView
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Habilitar JavaScript si es necesario
        webSettings.setDomStorageEnabled(true); // Habilitar almacenamiento DOM si es necesario

        // Establecer un WebViewClient para gestionar las solicitudes de la web
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // Mostrar el ProgressBar y el mensaje cuando la página comienza a cargar
                progressBar.setVisibility(View.VISIBLE);
                loadingMessage.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // Ocultar el ProgressBar y el mensaje cuando la página termina de cargar
                progressBar.setVisibility(View.GONE);
                loadingMessage.setVisibility(View.GONE);
            }
        });

        // Configurar un WebChromeClient para manejar permisos
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                // Verificar si ya se ha concedido el permiso de ubicación
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    // Si el permiso ya ha sido concedido, permitir el acceso
                    callback.invoke(origin, true, false);
                } else {
                    // Si no se ha concedido, mostrar un diálogo explicativo
                    requestLocationPermission(callback, origin);
                }
            }
        });

        // Verificar los permisos cuando el fragmento sea visible
        checkLocationPermission();

        return view;
    }

    // Método para verificar si el permiso de ubicación está concedido
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Si el permiso ya ha sido concedido, cargar el WebView
            loadWebView();
        } else {
            // Si no se ha concedido, solicitar el permiso
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }

    // Método para cargar el WebView después de que se haya concedido el permiso
    private void loadWebView() {
        webView.loadUrl("https://player.onirix.com/projects/c71ba637fa3649a1b17dad4676b9f5b9/webar?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjQ3MjU4LCJwcm9qZWN0SWQiOjk2MjUxLCJyb2xlIjozLCJpYXQiOjE3MzMzNTIxOTV9.1hg-PnTQfTjl8PTQHJZkZhGZ_j-2tR0W_Mr-ogKC5oA&launchpad=true");
    }

    // Método para solicitar permiso de ubicación al usuario si no se ha concedido
    private void requestLocationPermission(GeolocationPermissions.Callback callback, String origin) {
        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Permiso de ubicación necesario")
                .setMessage("Es necesario que permitas al navegador conocer tu ubicación para continuar con la funcionalidad de la página web.")
                .setPositiveButton("Permitir", (dialog, which) -> {
                    // Solicitar permiso de ubicación
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
                })
                .setNegativeButton("Denegar", (dialog, which) -> {
                    // Denegar el acceso a la ubicación
                    callback.invoke(origin, false, false);
                })
                .create()
                .show();
    }

    // Manejo de la respuesta de los permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, mostrar un mensaje y permitir el acceso de geolocalización
                Toast.makeText(getContext(), "Permiso de ubicación concedido", Toast.LENGTH_SHORT).show();
                loadWebView();  // Cargar el WebView después de que se conceda el permiso
            } else {
                // Permiso denegado, mostrar un mensaje al usuario
                Toast.makeText(getContext(), "Permiso de ubicación denegado. Algunas funciones no estarán disponibles.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

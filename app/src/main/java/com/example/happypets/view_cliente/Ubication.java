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
    private TextView loadingMessage; // Mensaje de carga

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflar el layout para este fragmento
        View view = inflater.inflate(R.layout.activity_ubication, container, false);

        // Encontrar el WebView, el ProgressBar y el TextView en el layout
        webView = view.findViewById(R.id.webview);
        progressBar = view.findViewById(R.id.progressBar);
        loadingMessage = view.findViewById(R.id.loadingMessage); // Inicializa el TextView

        // Configurar ajustes de WebView
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Habilitar JavaScript si es necesario
        webSettings.setDomStorageEnabled(true); // Habilitar almacenamiento DOM si es necesario

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
                // Manejar permisos de geolocalización
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    callback.invoke(origin, true, false);
                } else {
                    // Solicitar permiso si no se ha concedido
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
                }
            }
        });

        // Cargar la URL en el WebView
        webView.loadUrl("https://player.onirix.com/projects/855ab066d4c94bc5ac414a5dddc2cd3e/webar?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjQ1NzQ2LCJwcm9qZWN0SWQiOjkzMjAyLCJyb2xlIjozLCJpYXQiOjE3MzA3NDk5MDF9.b6_4CFOFjCN37iP0r71qxHiLFShk3BPJxlTjUZeRuRo&background=%23f59acf&preview=true&hide_controls=true&ar_button=true");

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido
                Toast.makeText(getContext(), "Location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                // Permiso denegado
                Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

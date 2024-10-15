package com.example.happypets.view_admin;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.happypets.R;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class DashboardAdmin extends Fragment {

    private WebView chartWebView;
    private OkHttpClient client;
    private int productCount = 0;
    private int petCount = 0;
    private int userCount = 0;
    private String token; // Variable para almacenar el token

    public static DashboardAdmin newInstance(String token) {
        DashboardAdmin fragment = new DashboardAdmin();
        Bundle args = new Bundle();
        args.putString("TOKEN", token); // Almacena el token en el Bundle
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_dashboard_admin, container, false);
        chartWebView = view.findViewById(R.id.webview);
        WebSettings webSettings = chartWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        chartWebView.setWebViewClient(new WebViewClient());
        client = new OkHttpClient();

        // Obtener el token del Bundle
        if (getArguments() != null) {
            token = getArguments().getString("TOKEN");
        }

        fetchProductCount();
        fetchPetCount();
        fetchUserCount();

        return view;
    }

    private void fetchProductCount() {
        String url = "https://api-happypetshco-com.preview-domain.com/api/ListarProductos";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token) // Agregar el token a la cabecera
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        JSONArray productos = jsonObject.getJSONArray("productos");
                        productCount = productos.length();
                        updateWebView();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void fetchPetCount() {
        String url = "https://api-happypetshco-com.preview-domain.com/api/ListarMascotas";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        JSONArray mascotas = jsonObject.getJSONArray("mascotas");
                        petCount = mascotas.length();
                        updateWebView();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void fetchUserCount() {
        String url = "https://api-happypetshco-com.preview-domain.com/api/Usuarios";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        JSONArray usuarios = jsonObject.getJSONArray("usuarios");
                        userCount = usuarios.length();
                        updateWebView();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void updateWebView() {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                String htmlContent = createHtmlContent(productCount, petCount, userCount);
                chartWebView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
            });
        }
    }

    private String createHtmlContent(int productCount, int petCount , int userCount) {
        return "<html>" +
                "<head>" +
                "<script src='https://cdn.jsdelivr.net/npm/apexcharts'></script>" +
                "<link rel='stylesheet' href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css'>" + // Agregar iconos
                "<style>" +
                "body { " +
                "    font-family: 'Arial', sans-serif; " +
                "    background-color: #f4f4f9; " +
                "    color: #333; " +
                "    margin: 0; " +
                "    padding: 0; " +
                "    display: flex; " +
                "    flex-direction: column; " +
                "    align-items: center; " +
                "} " +
                ".container { " +
                "    max-width: 800px; " +
                "    width: 100%; " +
                "    margin: 20px; " +
                "} " +
                ".header { " +
                "    text-align: center; " +
                "    margin-bottom: 20px; " +
                "} " +
                ".card { " +
                "    background: #ffffff; " +
                "    border-radius: 12px; " +
                "    padding: 20px; " +
                "    margin: 10px; " +
                "    text-align: center; " +
                "    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1); " +
                "    transition: transform 0.3s, box-shadow 0.3s; " +
                "} " +
                ".card:hover { " +
                "    transform: translateY(-5px); " +
                "    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2); " +
                "} " +
                ".card:nth-child(1) { background: #FF6F61; } " +
                ".card:nth-child(2) { background: #88B04B; } " +
                ".card:nth-child(3) { background: #3498db; } " +
                ".card i { margin-bottom: 10px; color: #000; }" +
                "h1 { " +
                "    color: #333; " +
                "    margin: 0; " +
                "} " +
                ".footer { " +
                "    margin-top: 30px; " +
                "    text-align: center; " +
                "} " +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>Dashboard Admin</h1>" +
                "</div>" +
                "<div style='display: flex; justify-content: space-around; flex-wrap: wrap;'>" +
                "<div class='card' style='flex: 1; min-width: 150px;'><i class='fas fa-box fa-2x'></i><br>Cantidad de Productos<br><strong>" + productCount + "</strong></div>" +
                "<div class='card' style='flex: 1; min-width: 150px;'><i class='fas fa-paw fa-2x'></i><br>Cantidad de Mascotas<br><strong>" + petCount + "</strong></div>" +  // Muestra la cantidad de mascotas
                "<div class='card' style='flex: 1; min-width: 150px;'><i class='fas fa-dollar-sign fa-2x'></i><br>Ganancias<br><strong>S/ 10,000</strong></div>" +
                "<div class='card' style='flex: 1; min-width: 150px;'><i class='fas fa-user fa-2x'></i><br>Clientes<br><strong>"+userCount+"</strong></div>" +
                "</div>" +
                // Gráfico de Ventas
                "<h2 style='text-align:center;'>Ventas Semanales</h2>" +
                "<div id='chart1' style='max-width:650px; margin: 35px auto;'></div>" +

                // Gráfico de Tipos de Productos
                "<h2 style='text-align:center;'>Tipos de Productos</h2>" +
                "<div id='chart2' style='max-width:650px; margin: 35px auto;'></div>" +

                // Gráfico de Clientes Nuevos
                "<h2 style='text-align:center;'>Clientes Nuevos por Mes</h2>" +
                "<div id='chart3' style='max-width:650px; margin: 35px auto;'></div>" +

                "<script>" +
                // Gráfico de Ventas Semanales
                "    var options1 = {" +
                "        chart: {" +
                "            type: 'bar'," +
                "            height: 350" +
                "        }," +
                "        series: [{" +
                "            name: 'Ventas'," +
                "            data: [30, 40, 35, 50, 49, 60, 70]" +
                "        }]," +
                "        xaxis: {" +
                "            categories: ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom']" +
                "        }" +
                "    };" +
                "    var chart1 = new ApexCharts(document.querySelector('#chart1'), options1);" +
                "    chart1.render();" +

                // Gráfico de Tipos de Productos
                "    var options2 = {" +
                "        chart: {" +
                "            type: 'pie'," +
                "            height: 350" +
                "        }," +
                "        series: [44, 55, 41, 17, 15]," +
                "        labels: ['Producto A', 'Producto B', 'Producto C', 'Producto D', 'Producto E']" +
                "    };" +
                "    var chart2 = new ApexCharts(document.querySelector('#chart2'), options2);" +
                "    chart2.render();" +

                // Gráfico de Clientes Nuevos por Mes
                "    var options3 = {" +
                "        chart: {" +
                "            type: 'line'," +
                "            height: 350" +
                "        }," +
                "        series: [{" +
                "            name: 'Clientes Nuevos'," +
                "            data: [10, 41, 35, 51, 49, 62, 69, 91, 148]" +
                "        }]," +
                "        xaxis: {" +
                "            categories: ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep']" +
                "        }" +
                "    };" +
                "    var chart3 = new ApexCharts(document.querySelector('#chart3'), options3);" +
                "    chart3.render();" +
                "</script>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

}

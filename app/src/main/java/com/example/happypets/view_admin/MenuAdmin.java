package com.example.happypets.view_admin;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.happypets.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MenuAdmin extends AppCompatActivity {

    private String token; // Declarar variable para almacenar el token

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    // Manejo de selección de fragmentos usando if-else
                    if (item.getItemId() == R.id.navigation_dashboard) {
                        selectedFragment = DashboardAdmin.newInstance(token); // Pasar token
                    } else if (item.getItemId() == R.id.navigation_clients) {
                        selectedFragment = ClientsAdmin.newInstance(token); // Pasar token
                    } else if (item.getItemId() == R.id.navigation_appointments) {
                        selectedFragment = AppointmentsAdmin.newInstance(token); // Pasar token
                    } else if (item.getItemId() == R.id.navigation_administrar) {
                        selectedFragment = ManageAdmin.newInstance(token); // Pasar token
                    }

                    if (selectedFragment != null) {
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.fragment_container, selectedFragment);
                        transaction.commit();
                    }

                    return true;
                }
            };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_admin);

        // Obtener el token del Intent
        token = getIntent().getStringExtra("token");

        // Configura los insets para evitar que la UI se superponga con las barras de sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializa el BottomNavigationView
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        // Cargar el fragmento inicial
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, DashboardAdmin.newInstance(token)).commit();
        }
    }
}

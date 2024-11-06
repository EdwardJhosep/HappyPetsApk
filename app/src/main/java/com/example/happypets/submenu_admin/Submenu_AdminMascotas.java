package com.example.happypets.submenu_admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.happypets.R;
import com.example.happypets.view_admin.MenuAdmin;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Submenu_AdminMascotas extends AppCompatActivity {

    private String token; // Variable para almacenar el token

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submenu_admin_mascotas);

        // Obtener el token del Intent
        Intent intent = getIntent();
        token = intent.getStringExtra("token"); // Asegúrate de usar la misma clave que en ManageAdmin

        // Inicializa el BottomNavigationView
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        // Cargar el fragmento inicial como EditarMascota
        if (savedInstanceState == null) {
            EditarMascota editarMascotaFragment = EditarMascota.newInstance(token); // Pasar el token al fragmento
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, editarMascotaFragment).commit();
        }
    }

    // Listener para manejar la selección de los items del BottomNavigationView
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    if (item.getItemId() == R.id.Editar) {
                        selectedFragment = EditarMascota.newInstance(token); // Pasar el token
                    } else if (item.getItemId() == R.id.Salir) {
                        Intent intent = new Intent(Submenu_AdminMascotas.this, MenuAdmin.class);
                        intent.putExtra("token", token); // Pasar el token al MenuAdmin
                        startActivity(intent);
                        finish();
                        return false;
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
}

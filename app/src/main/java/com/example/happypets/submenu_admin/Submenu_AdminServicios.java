package com.example.happypets.submenu_admin;

import android.annotation.SuppressLint;
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

public class Submenu_AdminServicios extends AppCompatActivity {

    private String token; // Variable para almacenar el token

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submenu_admin_servicios);

        // Obtener el token del Intent
        Intent intent = getIntent();
        token = intent.getStringExtra("token"); // Asegúrate de usar la misma clave que en ManageAdmin

        // Inicializa el BottomNavigationView
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        // Cargar el fragmento inicial para editar servicio
        if (savedInstanceState == null) {
            EditarServicio editarServicioFragment = EditarServicio.newInstance(token); // Pasar el token al fragmento
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, editarServicioFragment).commit();
        }
    }

    // Listener para manejar la selección de los items del BottomNavigationView
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    // Selección del fragmento para editar servicio
                    if (item.getItemId() == R.id.Editar) {
                        selectedFragment = EditarServicio.newInstance(token); // Cargar el fragmento para editar servicio
                    }
                    // Selección para salir y regresar al menú principal de administración
                    else if (item.getItemId() == R.id.Salir) {
                        Intent intent = new Intent(Submenu_AdminServicios.this, MenuAdmin.class);
                        intent.putExtra("token", token); // Pasar el token al MenuAdmin
                        startActivity(intent);
                        finish();
                        return false;
                    }
                    // Selección para agregar un nuevo servicio
                    else if (item.getItemId() == R.id.Agregar) {
                        selectedFragment = AgregarServicio.newInstance(token); // Cargar el fragmento para agregar servicio
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

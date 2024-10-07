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

    // Listener para manejar la selección de los items del BottomNavigationView
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    if (item.getItemId() == R.id.Agregar) {
                        selectedFragment = new AgregarMascota();
                    } else if (item.getItemId() == R.id.Editar) {
                        selectedFragment = new EditarMascota();
                    } else if (item.getItemId() == R.id.Salir) {
                        // Cambiar a otra actividad en lugar de un fragmento
                        Intent intent = new Intent(Submenu_AdminMascotas.this, MenuAdmin.class);
                        startActivity(intent);
                        finish(); // Finaliza la actividad actual
                        return false; // Regresa false para no continuar con el resto del código
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submenu_admin_mascotas);

        // Inicializa el BottomNavigationView
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        // Cargar el fragmento inicial
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AgregarMascota()).commit();
        }
    }
}

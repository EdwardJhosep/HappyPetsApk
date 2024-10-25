package com.example.happypets.submenu_admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.happypets.R;
import com.example.happypets.view_admin.MenuAdmin;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Submenu_AdminPersonal extends AppCompatActivity {

    private String token; // Variable para almacenar el token

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    if (item.getItemId() == R.id.Editar) {
                        selectedFragment = new EditarPersonal();
                        ((EditarPersonal) selectedFragment).setToken(token); // Cambia EditarProducto a EditarPersonal
                    } if (item.getItemId() == R.id.Salir) {
                        // Cambiar a otra actividad en lugar de un fragmento
                        Intent intent = new Intent(Submenu_AdminPersonal.this, MenuAdmin.class);
                        intent.putExtra("token", token); // Pasar el token al MenuAdmin
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
        setContentView(R.layout.activity_submenu_admin_personal);

        // Obtener el token del Intent
        token = getIntent().getStringExtra("token"); // Asegúrate de que la clave "token" coincida con la utilizada en ManageAdmin
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        if (savedInstanceState == null) {
            // Crear una instancia de EditarPersonal y establecer el token
            EditarPersonal editarPersonal = new EditarPersonal();
            editarPersonal.setToken(token); // Enviar el token al fragmento
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, editarPersonal).commit();
        }
    }
}

package com.example.happypets.perfilview;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.happypets.R;

public class AgregarMascotaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_agregar_mascota);

        // Retrieve the token and userId from the Intent
        String token = getIntent().getStringExtra("token");
        String userId = getIntent().getStringExtra("userId");

        // Display the token and userId in a Toast message
        if (token != null && userId != null) {
            Toast.makeText(this, "Token: " + token + "\nUserId: " + userId, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Token or UserId is missing", Toast.LENGTH_SHORT).show();
        }

        // Handle window insets for edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}

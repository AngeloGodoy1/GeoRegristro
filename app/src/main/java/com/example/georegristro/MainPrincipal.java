package com.example.georegristro;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MainPrincipal extends AppCompatActivity {

    private Button btnRegistrarIncidencia, btnVerHistorial, btnVerMapa, btnCerrarSesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_principal);

        btnRegistrarIncidencia = findViewById(R.id.btnRegistrarIncidencia);
        btnVerHistorial = findViewById(R.id.btnVerHistorial);
        btnVerMapa = findViewById(R.id.btnVerMapa);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        btnRegistrarIncidencia.setOnClickListener(v -> {
            Intent intent = new Intent(MainPrincipal.this, RegistroIncidenciaActivity.class);
            startActivity(intent);
        });

        btnVerHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(MainPrincipal.this, HistorialActivity.class);
            startActivity(intent);
        });

        btnVerMapa.setOnClickListener(v -> {
            Intent intent = new Intent(MainPrincipal.this, MapaActivity.class);
            startActivity(intent);
        });

        btnCerrarSesion.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainPrincipal.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}

package com.example.georegristro;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MapaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore db;
    private Button btnVolverMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);

        db = FirebaseFirestore.getInstance();
        btnVolverMenu = findViewById(R.id.btnVolverMenu);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapVer);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnVolverMenu.setOnClickListener(v -> finish());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        cargarIncidenciasEnMapa();
    }

    private void cargarIncidenciasEnMapa() {
        db.collection("incidencias")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(MapaActivity.this, "No hay incidencias guardadas para mostrar en el mapa", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    int marcadoresContador = 0;

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String titulo = doc.getString("titulo");
                        String descripcion = doc.getString("descripcion");
                        Double lat = doc.getDouble("latitud");
                        Double lng = doc.getDouble("longitud");

                        if (lat != null && lng != null) {
                            LatLng pos = new LatLng(lat, lng);
                            mMap.addMarker(new MarkerOptions()
                                    .position(pos)
                                    .title(titulo != null ? titulo : "Incidencia")
                                    .snippet(descripcion != null ? descripcion : ""));
                            builder.include(pos);
                            marcadoresContador++;
                        }
                    }

                    if (marcadoresContador > 0) {
                        LatLngBounds bounds = builder.build();
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(MapaActivity.this,
                        "Error al cargar mapa de incidencias: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

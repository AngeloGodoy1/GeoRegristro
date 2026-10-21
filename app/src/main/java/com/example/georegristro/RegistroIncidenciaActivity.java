package com.example.georegristro;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistroIncidenciaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;

    private TextView tvCoordenadas;
    private TextInputEditText etTitulo, etDescripcion;
    private Button btnObtenerUbicacion, btnGuardarIncidencia, btnVolverMenu;

    private LatLng ubicacionSeleccionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_incidencia);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();

        tvCoordenadas = findViewById(R.id.tvCoordenadasRegistro);
        etTitulo = findViewById(R.id.etTituloIncidencia);
        etDescripcion = findViewById(R.id.etDescripcionIncidencia);
        btnObtenerUbicacion = findViewById(R.id.btnObtenerUbicacionRegistro);
        btnGuardarIncidencia = findViewById(R.id.btnGuardarIncidencia);
        btnVolverMenu = findViewById(R.id.btnVolverMenu);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapRegistro);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnObtenerUbicacion.setOnClickListener(v -> obtenerUbicacionGPS());
        btnGuardarIncidencia.setOnClickListener(v -> guardarIncidencia());
        btnVolverMenu.setOnClickListener(v -> finish());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(latLng -> {
            actualizarUbicacionSeleccionada(latLng, "Ubicación seleccionada en mapa");
        });

        obtenerUbicacionGPS();
    }

    private void obtenerUbicacionGPS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        if (mMap != null) {
            mMap.setMyLocationEnabled(true);
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                LatLng miUbicacion = new LatLng(location.getLatitude(), location.getLongitude());
                actualizarUbicacionSeleccionada(miUbicacion, "Mi ubicación GPS actual");
            } else {
                Toast.makeText(RegistroIncidenciaActivity.this, "No se pudo obtener el GPS actual. Seleccione en el mapa.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void actualizarUbicacionSeleccionada(LatLng latLng, String titulo) {
        ubicacionSeleccionada = latLng;
        tvCoordenadas.setText(String.format("Ubicación GPS: Lat %.5f, Lng %.5f", latLng.latitude, latLng.longitude));

        if (mMap != null) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title(titulo));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
        }
    }

    private void guardarIncidencia() {
        String titulo = etTitulo.getText() != null ? etTitulo.getText().toString().trim() : "";
        String descripcion = etDescripcion.getText() != null ? etDescripcion.getText().toString().trim() : "";

        if (titulo.isEmpty()) {
            etTitulo.setError("Ingrese un título para la incidencia");
            return;
        }

        if (ubicacionSeleccionada == null) {
            Toast.makeText(this, "Por favor seleccione una ubicación en el mapa o use el GPS", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> incidencia = new HashMap<>();
        incidencia.put("titulo", titulo);
        incidencia.put("descripcion", descripcion);
        incidencia.put("latitud", ubicacionSeleccionada.latitude);
        incidencia.put("longitud", ubicacionSeleccionada.longitude);
        incidencia.put("fecha", Timestamp.now());

        db.collection("incidencias")
                .add(incidencia)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(RegistroIncidenciaActivity.this, "¡Incidencia guardada con éxito!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegistroIncidenciaActivity.this, "Error al guardar incidencia: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacionGPS();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado.", Toast.LENGTH_LONG).show();
            }
        }
    }
}

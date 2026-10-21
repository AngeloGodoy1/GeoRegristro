package com.example.georegristro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class HistorialActivity extends AppCompatActivity {

    private RecyclerView rvIncidencias;
    private Button btnVolverMenu;
    private FirebaseFirestore db;
    private IncidenciasAdapter adapter;
    private List<DocumentSnapshot> listaIncidencias = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        db = FirebaseFirestore.getInstance();
        rvIncidencias = findViewById(R.id.rvIncidencias);
        btnVolverMenu = findViewById(R.id.btnVolverMenu);

        rvIncidencias.setLayoutManager(new LinearLayoutManager(this));
        adapter = new IncidenciasAdapter(listaIncidencias);
        rvIncidencias.setAdapter(adapter);

        btnVolverMenu.setOnClickListener(v -> finish());

        cargarIncidencias();
    }

    private void cargarIncidencias() {
        db.collection("incidencias")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaIncidencias.clear();
                    listaIncidencias.addAll(queryDocumentSnapshots.getDocuments());
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(HistorialActivity.this,
                        "Error al cargar historial: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private static class IncidenciasAdapter extends RecyclerView.Adapter<IncidenciasAdapter.ViewHolder> {

        private final List<DocumentSnapshot> items;

        public IncidenciasAdapter(List<DocumentSnapshot> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_incidencia, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DocumentSnapshot doc = items.get(position);
            String titulo = doc.getString("titulo");
            String descripcion = doc.getString("descripcion");
            Double lat = doc.getDouble("latitud");
            Double lng = doc.getDouble("longitud");

            holder.tvTitulo.setText(titulo != null ? titulo : "Sin título");
            holder.tvDescripcion.setText(descripcion != null ? descripcion : "Sin descripción");
            holder.tvUbicacion.setText(String.format("Lat: %.5f, Lng: %.5f", lat != null ? lat : 0.0, lng != null ? lng : 0.0));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitulo, tvDescripcion, tvUbicacion;

            ViewHolder(View itemView) {
                super(itemView);
                tvTitulo = itemView.findViewById(R.id.tvTituloItem);
                tvDescripcion = itemView.findViewById(R.id.tvDescripcionItem);
                tvUbicacion = itemView.findViewById(R.id.tvUbicacionItem);
            }
        }
    }
}

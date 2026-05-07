package com.example.mapapplication;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class GoogleMapActivity extends AppCompatActivity {

    // Déclaration des variables d'instance
    private MapView map;  // Vue de carte OSMDroid
    private RequestQueue requestQueue;  // File d'attente pour les requêtes HTTP
    private String showUrl = "http://10.0.2.2/map_project/getPosition.php";  // URL du script PHP

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configuration d'OSMDroid - nécessaire avant d'utiliser la bibliothèque
        // Cela configure le cache et les préférences utilisateur
        Configuration.getInstance().load(getApplicationContext(), getSharedPreferences("prefs", MODE_PRIVATE));

        // Définition du layout de l'activité
        setContentView(R.layout.activity_google_map);

        // Récupération et configuration de la vue de carte
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);  // Source de tuiles standard OpenStreetMap
        map.setBuiltInZoomControls(true);  // Activer les contrôles de zoom intégrés
        map.setMultiTouchControls(true);   // Activer le pinch-to-zoom et autres gestes multi-touch

        // Définition de la position initiale de la carte
        map.getController().setZoom(15.0);  // Niveau de zoom (0-22, 15 = niveau ville/quartier)
        map.getController().setCenter(new GeoPoint(37.272525, -122.12106));  // Coordonnées par défaut

        // Initialisation de Volley pour les requêtes HTTP
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        // Chargement des marqueurs depuis l'API
        loadPositions();
    }

    /**
     * Convertit un Drawable en Bitmap de manière sécurisée (gère les VectorDrawables)
     */
    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * Charge les positions depuis le serveur et les affiche sur la carte
     * Cette méthode effectue une requête HTTP et traite la réponse JSON
     */
    private void loadPositions() {
        // Création d'une requête JSON avec Volley
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,  // Méthode HTTP
                showUrl,              // URL du script PHP
                null,                 // Pas de corps JSON à envoyer
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Extraction du tableau de positions depuis la réponse JSON
                            JSONArray positions = response.getJSONArray("positions");

                            // Parcours de toutes les positions
                            for (int i = 0; i < positions.length(); i++) {
                                // Extraction des données de chaque position
                                JSONObject position = positions.getJSONObject(i);
                                double lat = position.getDouble("latitude");
                                double lng = position.getDouble("longitude");

                                // Création d'un marqueur pour chaque position
                                Marker marker = new Marker(map);
                                marker.setPosition(new GeoPoint(lat, lng));  // Définition de la position
                                marker.setTitle("Marker " + (i + 1));        // Titre du marqueur

                                // Chargement et redimensionnement de l'icône du marqueur
                                // Utilisation de ContextCompat pour la compatibilité
                                Drawable original = ContextCompat.getDrawable(GoogleMapActivity.this, R.drawable.marker);
                                if (original != null) {
                                    Bitmap bitmap = drawableToBitmap(original);

                                    // Redimensionnement du bitmap pour l'adapter à l'affichage (80x80 px)
                                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 80, 80, false);
                                    marker.setIcon(new BitmapDrawable(getResources(), scaledBitmap));
                                }

                                // Configuration de l'ancrage du marqueur
                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                                // Ajout du marqueur à la carte
                                map.getOverlays().add(marker);

                                // Affichage des coordonnées dans un toast (pour le débogage)
                                Toast.makeText(getApplicationContext(),"Lat : "+ lat+" lng : "+lng,Toast.LENGTH_SHORT).show();
                            }

                            // Rafraîchissement de la carte pour afficher les marqueurs
                            map.invalidate();

                        } catch (JSONException e) {
                            // Gestion des erreurs de parsing JSON
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Gestion des erreurs de requête HTTP
                        error.printStackTrace();
                    }
                }
        );

        // Ajout de la requête à la file d'attente
        requestQueue.add(jsonObjectRequest);
    }
}

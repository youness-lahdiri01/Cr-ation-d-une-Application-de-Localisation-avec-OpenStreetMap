package com.example.mapapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // Déclaration des variables d'instance
    private Button btnMap;
    private double latitude;    // Stocke la latitude actuelle
    private double longitude;   // Stocke la longitude actuelle
    private double altitude;    // Stocke l'altitude actuelle
    private float accuracy;     // Stocke la précision de la localisation
    RequestQueue requestQueue;  // File d'attente pour les requêtes HTTP
    String insertUrl = "http://10.0.2.2/map_project/createPosition.php";  // URL du script PHP
    LocationManager locationManager;  // Gestionnaire de localisation Android

    // Code d'identification pour la demande de permission
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialisation de la file d'attente Volley pour les requêtes HTTP
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        // Obtention du service de localisation du système
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Récupération du bouton depuis le layout
        btnMap = findViewById(R.id.btnMap);

        // Configuration du listener de clic sur le bouton
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Démarrage de l'activité de la carte
                startActivity(new Intent(MainActivity.this, GoogleMapActivity.class));
            }
        });

        // Vérification des permissions avant de demander les mises à jour de localisation
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            // Si les permissions ne sont pas accordées, les demander
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.READ_PHONE_STATE
                    }, PERMISSION_REQUEST_CODE);
        } else {
            // Permissions déjà accordées, démarrer la logique de localisation
            startLocationUpdates();
        }
    }

    /**
     * Démarre les mises à jour de localisation en utilisant le GPS
     * Cette méthode configure le LocationManager pour recevoir des mises à jour périodiques
     */
    private void startLocationUpdates() {
        // Vérification des permissions à nouveau pour satisfaire l'avertissement de l'IDE (SecurityException)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Configuration des mises à jour de localisation
        // Paramètres: fournisseur, intervalle minimum (ms), distance minimum (m), écouteur
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 150, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                // Extraction des données de localisation
                latitude = location.getLatitude();    // Latitude en degrés
                longitude = location.getLongitude();  // Longitude en degrés
                altitude = location.getAltitude();    // Altitude en mètres
                accuracy = location.getAccuracy();    // Précision en mètres

                // Formatage du message avec les nouvelles coordonnées
                String msg = String.format(
                        getResources().getString(R.string.new_location), latitude,
                        longitude, altitude, accuracy);

                // Envoi des coordonnées au serveur
                addPosition(latitude, longitude);

                // Affichage d'un toast avec les nouvelles coordonnées
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }

            // Note: onStatusChanged, onProviderEnabled, onProviderDisabled sont dépréciés dans les API plus récentes
            // mais conservés ici pour la compatibilité si vous en avez besoin.
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // Appelé quand le statut du fournisseur change (GPS disponible, temporairement indisponible, hors service)
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
                // Appelé quand l'utilisateur active le fournisseur (ex: GPS)
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                // Appelé quand l'utilisateur désactive le fournisseur (ex: GPS)
            }
        });
    }

    /**
     * Gère la réponse de l'utilisateur à la boîte de dialogue de permission
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission accordée, démarrer les mises à jour de localisation
                startLocationUpdates();
            } else {
                // Permission refusée, informer l'utilisateur
                Toast.makeText(this, "Permission refusée. L'application ne peut pas fonctionner.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Envoie les coordonnées de position au serveur via une requête HTTP POST
     * @param lat Latitude à envoyer
     * @param lon Longitude à envoyer
     */
    void addPosition(final double lat, final double lon) {
        // Création d'une requête POST avec Volley
        StringRequest request = new StringRequest(Request.Method.POST,
                insertUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Traitement de la réponse du serveur (vide ici, pourrait être amélioré)
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Gestion des erreurs (vide ici, pourrait être amélioré pour afficher un message)
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                // Préparation des paramètres à envoyer
                HashMap<String, String> params = new HashMap<>();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                // Ajout des coordonnées et de la date
                params.put("latitude", lat + "");
                params.put("longitude", lon + "");
                params.put("date", sdf.format(new Date()));

                // --- REMPLACEMENT POUR IMEI/ID DE PÉRIPHÉRIQUE ---
                // Utilisation d'ANDROID_ID qui est unique par installation et ne nécessite PAS de permissions.
                String androidId = Settings.Secure.getString(
                        getContentResolver(),
                        Settings.Secure.ANDROID_ID
                );

                // Note: Si vous appelez toujours le paramètre 'imei' sur votre backend, conservez le nom de la clé.
                params.put("imei", androidId);

                return params;
            }
        };
        // Ajout de la requête à la file d'attente
        requestQueue.add(request);
    }
}
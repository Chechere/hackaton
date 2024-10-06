package com.hackaton2024.wiliwilowilu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;

public class RegionInfoActivity extends AppCompatActivity implements Notifier {
    private double longitude, latitude;
    private UrlFetchJson fetcherJsonEONET, fetchJsonOpenAQ;
    private UrlFetchBinary fetchBinarySAT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_regioninfo);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if(!havePermissions() || !checkInternetAccess()) {
            return;
        }

        fetchBinarySAT = new UrlFetchBinary(this);
        fetcherJsonEONET = new UrlFetchJson(this);
        fetchJsonOpenAQ = new UrlFetchJson(this);

        LocationManager lm = (LocationManager)getSystemService(LOCATION_SERVICE);
        @SuppressLint("MissingPermission") Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        longitude = location.getLongitude();
        latitude = location.getLatitude();

        System.out.println("GEO: " + longitude + " " + latitude);

        String APIEarth = "https://api.nasa.gov/planetary/earth/imagery?lon=" +
                String.format(Locale.ENGLISH,"%.2f", longitude) + "&lat=" +
                String.format(Locale.ENGLISH,"%.2f", latitude) +
                "&dim=0.1" + "&api_key=" + getString(R.string.NASA_API_key);

        String APIEONET = "https://eonet.gsfc.nasa.gov/api/v3/events?bbox=" +
                //"-129.02,50.73,-58.71,12.89";
                String.format(Locale.ENGLISH,"%.2f,", longitude - 1) +
                String.format(Locale.ENGLISH,"%.2f,", latitude + 1) +
                String.format(Locale.ENGLISH,"%.2f,", longitude + 1) +
                String.format(Locale.ENGLISH,"%.2f,", latitude - 1);

        String APIOpenAQ = "https://api.openaq.org/v1/measurements?coordinates=" +
                String.format(Locale.ENGLISH,"%.2f,", latitude) +
                String.format(Locale.ENGLISH,"%.2f", longitude) +
                "&radius=25000";

        fetchBinarySAT.execute(APIEarth, "SAT IMAGE");
        fetcherJsonEONET.execute(APIEONET, "EONET JSON");
        fetchJsonOpenAQ.execute(APIOpenAQ, "OPENAQ JSON", getString(R.string.OpenAQ_API_key));
    }

    private boolean havePermissions() {
        String permisosFaltantes = "";
        boolean tienePermisos = true;

        boolean faltanWIFIPermResto = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED;

        boolean faltanLocationPerm = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;

        if (faltanWIFIPermResto) {
            permisosFaltantes = "Acceso a Internet";
            tienePermisos = false;
        }

        if (faltanLocationPerm) {
            if(!permisosFaltantes.isEmpty()) {
                permisosFaltantes += ", ";
            }

            permisosFaltantes += "Acceso a la ubicación";
            tienePermisos = false;
        }

        if(!tienePermisos) {
            new AlertDialog.Builder(this)
                    .setTitle("No tengo los suficientes permisos:")
                    .setMessage("Esta aplicación necesita los siguientes permisos para funcionar: " +
                            permisosFaltantes + ".\nPor favor, otorga los permisos.")
                    .setPositiveButton("Ir a Configuración", (dialog, which) -> {
                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);

                        intent.setData(uri);
                        startActivity(intent);
                    })
                    .setNegativeButton("OK", (dialog, which) -> dialog.dismiss())
                    .setCancelable(false)
                    .show();
        }

        return tienePermisos;
    }

    private boolean checkInternetAccess() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkCapabilities networkCapabilities =
                    connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());


            return networkCapabilities != null &&
                    (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
        }

        return false;
    }

    void obtainSatelliteImage(byte[] bitImage) {
        ImageView regionImage = findViewById(R.id.RegionImage);

        if(bitImage != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bitImage, 0, bitImage.length);

            if (bitmap != null) {
                runOnUiThread(() -> regionImage.setImageBitmap(bitmap));
            }
        }
    }

    void obtainEONETData(String json) {
        if(json == null) {
            return;
        }

        TextView hazardTv = findViewById(R.id.EONETTextView);

        try {

            JSONObject jsonObject = new JSONObject(json);
            JSONArray eventsArray = jsonObject.getJSONArray("events");

            if(eventsArray.length() != 0) {
                runOnUiThread(() -> hazardTv.setText(""));
            }

            // Loop through the events
            for (int i = 0; i < eventsArray.length() && i < 3; i++) {
                JSONObject eventObject = eventsArray.getJSONObject(i);

                // Get the categories array
                JSONArray categoriesArray = eventObject.getJSONArray("categories");
                JSONObject categoryObject = categoriesArray.getJSONObject(0);
                String category = categoryObject.getString("title");

                // Get the geometry array (coordinates)
                JSONArray geometryArray = eventObject.getJSONArray("geometry");
                JSONObject geometryObject = geometryArray.getJSONObject(0);
                JSONArray coordinatesArray = geometryObject.getJSONArray("coordinates");

                double evLongitude = coordinatesArray.getDouble(0);
                double evLatitude = coordinatesArray.getDouble(1);

                double distance = calculateDistance(latitude, longitude, evLatitude, evLongitude);
                runOnUiThread(() -> {
                    hazardTv.append(category + "; Distance: " +
                            String.format(Locale.ENGLISH,"%.0fkm\n", distance));
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radio de la Tierra en km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Distancia en kilómetros
    }

    private void obtainOpenAQData(String json) {
        if(json != null) {
            HashMap<String, Double> parameters = new HashMap<>();
            HashMap<String, Integer> recount = new HashMap<>();
            HashMap<String, String> unit = new HashMap<>();

            TextView OpenAQTextView = findViewById(R.id.OpenAQTexview);

            try {
                JSONObject jsonObject = new JSONObject(json);
                JSONArray resultsArray = jsonObject.getJSONArray("results");

                for (int i = 0; i < resultsArray.length(); i++) {
                    JSONObject result = resultsArray.getJSONObject(i);

                    String parameter = result.getString("parameter");
                    double value = result.getDouble("value");
                    String valueUnit = result.getString("unit");

                    if(parameters.containsKey(parameter) && recount.containsKey(parameter)) {
                        double pvalue = parameters.get(parameter);
                        int precount = recount.get(parameter);

                        parameters.put(parameter,(pvalue * precount + pvalue) / (precount + 1));
                        recount.put(parameter, precount + 1);
                    } else {
                        parameters.put(parameter,value);
                        recount.put(parameter, 1);
                        unit.put(parameter, valueUnit);
                    }
                }

                if(!parameters.isEmpty()) {
                    runOnUiThread(() -> OpenAQTextView.setText(""));
                }

                parameters.forEach((k, v) -> {
                    runOnUiThread(() -> {
                        OpenAQTextView.append(k.toUpperCase() + ": " +
                                String.format("%.2f ", v) + unit.get(k) + "\n");
                    });
                });
            } catch(Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void notify(String notification) {
        if(notification.equals("SAT IMAGE")) {
            obtainSatelliteImage(fetchBinarySAT.getResult());
        } else if (notification.equals("EONET JSON")) {
            obtainEONETData(fetcherJsonEONET.getResult());
        } else if (notification.equals("OPENAQ JSON")) {
            obtainOpenAQData(fetchJsonOpenAQ.getResult());
        }
    }
}
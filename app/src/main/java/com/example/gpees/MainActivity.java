package com.example.gpees;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MainActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private MapView mapView;
    private GoogleMap googleMap;
    private DatabaseService dbService;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        dbService = new DatabaseService();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        // Set up marker click listener
        googleMap.setOnMarkerClickListener(marker -> {
            if (marker.getTag() != null && marker.getTag().equals("bathroom")) {
                BathroomDialog dialog = new BathroomDialog();
                dialog.show(getSupportFragmentManager(), "BathroomDialog");
                return true;
            }
            return false;
        });

        // Try to get current location and center map
        updateLocationAndFetchBathrooms();
    }

    private void updateLocationAndFetchBathrooms() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // Enable blue dot
        googleMap.setMyLocationEnabled(true);

        // Get FRESH location instead of cached one
        CancellationTokenSource cts = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.getToken())
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                        fetchBathrooms(currentLatLng.latitude, currentLatLng.longitude, 1000.0);
                    } else {
                        // Fallback to Kelowna
                        LatLng kelowna = new LatLng(49.888, -119.496);
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(kelowna, 14));
                        fetchBathrooms(kelowna.latitude, kelowna.longitude, 1000.0);
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateLocationAndFetchBathrooms();
            }
        }
    }

    private void fetchBathrooms(double lat, double lng, double radius) {
        dbService.getBathroomsNearby(lat, lng, radius, new DatabaseService.BathroomsCallback() {
            @Override
            public void onSuccess(List<Bathroom> bathrooms) {
                // Clear old bathroom markers before adding new ones
                googleMap.clear();
                for (Bathroom bathroom : bathrooms) {
                    addBathroomMarker(bathroom);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error fetching bathrooms", e);
            }
        });
    }

    private void addBathroomMarker(Bathroom bathroom) {
        LatLng position = new LatLng(bathroom.getLatitude(), bathroom.getLongitude());
        
        int iconResId = R.drawable.toilet__icon;
        if (bathroom.hasTag("cost")) {
            iconResId = R.drawable.dollar_sign_solid_full;
        } else if (bathroom.hasTag("accessible")) {
            iconResId = R.drawable.wheelchair_solid_full;
        }

        Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(position)
                .title(bathroom.getName())
                .anchor(0.5f, 0.5f)
                .icon(getBitmapDescriptorFromVector(this, iconResId)));

        if (marker != null) {
            marker.setTag("bathroom");
        }
    }

    private BitmapDescriptor getBitmapDescriptorFromVector(Context context, @DrawableRes int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        if (vectorDrawable == null) return null;

        int size = 80;
        vectorDrawable.setBounds(0, 0, size, size);

        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }
    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}

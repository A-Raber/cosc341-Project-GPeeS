package com.example.gpees;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

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
        googleMap.clear();

        // Set up marker click listener
        googleMap.setOnMarkerClickListener(marker -> {

            if (marker.getTag() != null && marker.getTag().equals("bathroom")) {
                BathroomDialog dialog = new BathroomDialog();
                dialog.show(getSupportFragmentManager(), "BathroomDialog");
                return true;
            }
            return false;
        });

        LatLng kelowna = new LatLng(49.888, -119.496);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(kelowna, 14));

        // Add markers with a "bathroom" tag
        addBathroomMarker(new LatLng(49.888, -119.496), "Standard Toilet", R.drawable.toilet__icon);
        addBathroomMarker(new LatLng(49.892, -119.485), "Accessible Toilet", R.drawable.wheelchair_solid_full);
        addBathroomMarker(new LatLng(49.885, -119.505), "Paid Toilet", R.drawable.dollar_sign_solid_full);
    }

    private void addBathroomMarker(LatLng position, String title, @DrawableRes int iconResId) {
        Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(position)
                .title(title)
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
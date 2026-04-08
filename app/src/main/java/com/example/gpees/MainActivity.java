package com.example.gpees;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import androidx.activity.EdgeToEdge;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, FilterDialog.FilterListener, AddBathroomDialog.AddBathroomListener {

    private static final String TAG = "MainActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private MapView mapView;
    private GoogleMap googleMap;
    private DatabaseService dbService;
    private FusedLocationProviderClient fusedLocationClient;

    private LatLng currentLatLng;
    private final List<Bathroom> displayedBathrooms = new ArrayList<>();
    private FilterCriteria currentFilters = new FilterCriteria();
    private String searchQuery = "";
    private AddBathroomDialog pendingAddDialog;
    private boolean pickingLocation = false;

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

        // Initialize Filter Button
        MaterialButton btnFilter = findViewById(R.id.btn_filter);
        btnFilter.setOnClickListener(v -> {
            FilterDialog dialog = new FilterDialog(currentFilters, this);
            dialog.show(getSupportFragmentManager(), "FilterDialog");
        });

        // Initialize Search Bar
        TextInputEditText etSearch = findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim().toLowerCase();
                fetchBathrooms(currentLatLng.latitude, currentLatLng.longitude, currentFilters.getMaxDistance() * 1000.0);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                fetchBathrooms(currentLatLng.latitude, currentLatLng.longitude, currentFilters.getMaxDistance() * 1000.0);
                return true;
            }
            return false;
        });

        // Hamburger menu popup
        ImageButton btnMenu = findViewById(R.id.btn_menu);
        btnMenu.setOnClickListener(view -> {
            PopupMenu popup = new PopupMenu(this, view);
            popup.getMenuInflater().inflate(R.menu.menu_main, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_profile) {
                    startActivity(new Intent(this, ProfileActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.menu_logout) {
                    // Add logout logic here later
                    return true;
                } else if (item.getItemId() == R.id.menu_add_bathroom) {
                    openAddBathroomDialog(null);
                    return true;
                }
                return false;
            });
            popup.show();
        });

        // Initialize MapView
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Set up CLOSEST button logic
        findViewById(R.id.btn_closest).setOnClickListener(v -> navigateToClosestBathroom());
    }

    @Override
    public void onFilterApplied(FilterCriteria criteria) {
        this.currentFilters = criteria;
        updateLocationAndFetchBathrooms();
    }

    private void navigateToClosestBathroom() {
        if (currentLatLng == null) {
            Toast.makeText(this, "Finding your location...", Toast.LENGTH_SHORT).show();
            updateLocationAndFetchBathrooms();
            return;
        }

        if (displayedBathrooms.isEmpty()) {
            Toast.makeText(this, "No bathrooms found with current filters", Toast.LENGTH_SHORT).show();
            return;
        }

        Bathroom closest = null;
        double minDistance = Double.MAX_VALUE;

        for (Bathroom b : displayedBathrooms) {
            double distance = DatabaseService.distanceMeters(
                    currentLatLng.latitude, currentLatLng.longitude,
                    b.getLatitude(), b.getLongitude()
            );
            if (distance < minDistance) {
                minDistance = distance;
                closest = b;
            }
        }

        if (closest != null) {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" +
                    closest.getLatitude() + "," + closest.getLongitude() + "&mode=w");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        try {
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
            if (!success) Log.e(TAG, "Style parsing failed.");
        } catch (Exception e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        googleMap.getUiSettings().setZoomControlsEnabled(true);

        googleMap.setOnMarkerClickListener(marker -> {
            if (marker.getTag() instanceof Bathroom) {
                Bathroom bathroom = (Bathroom) marker.getTag();
                BathroomDialog dialog = BathroomDialog.newInstance(bathroom, currentLatLng);
                dialog.show(getSupportFragmentManager(), "BathroomDialog");
                return true;
            }
            return false;
        });

        googleMap.setOnMapClickListener(latLng -> {
            if (pickingLocation && pendingAddDialog != null) {
                pickingLocation = false;
                new Thread(() -> {
                    Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                    String address = "";
                    try {
                        List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            address = addresses.get(0).getAddressLine(0);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    final String finalAddress = address;
                    runOnUiThread(() -> {
                        pendingAddDialog.setSelectedLocation(latLng, finalAddress);
                        pendingAddDialog.getDialog().show();
                        pendingAddDialog = null;
                    });
                }).start();
            }
        });

        updateLocationAndFetchBathrooms();
    }

    private void updateLocationAndFetchBathrooms() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        googleMap.setMyLocationEnabled(true);

        CancellationTokenSource cts = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.getToken())
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    } else {
                        currentLatLng = new LatLng(49.888, -119.496); // Default Kelowna
                    }
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                    fetchBathrooms(currentLatLng.latitude, currentLatLng.longitude, currentFilters.getMaxDistance() * 1000.0);
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            updateLocationAndFetchBathrooms();
        }
    }

    private void fetchBathrooms(double lat, double lng, double radius) {
        dbService.getBathroomsNearby(lat, lng, radius, new DatabaseService.BathroomsCallback() {
            @Override
            public void onSuccess(List<Bathroom> bathrooms) {
                googleMap.clear();
                displayedBathrooms.clear();

                for (Bathroom bathroom : bathrooms) {
                    // 1. Rating Filter
                    if (bathroom.getRating() < currentFilters.getMinRating()) {
                        continue;
                    }

                    // 2. Tag Filter (Matches ALL selected tags)
                    boolean matchesTags = true;
                    for (String filterTag : currentFilters.getTags()) {
                        if (!bathroom.hasTag(filterTag.toLowerCase())) {
                            matchesTags = false;
                            break;
                        }
                    }
                    if (!matchesTags) continue;

                    // 3. Search Filter (Name, Address, or Tags)
                    boolean matchesSearch = searchQuery.isEmpty()
                            || (bathroom.getName() != null && bathroom.getName().toLowerCase().contains(searchQuery))
                            || (bathroom.getAddress() != null && bathroom.getAddress().toLowerCase().contains(searchQuery))
                            || bathroom.getTags().stream().anyMatch(tag -> tag.toLowerCase().contains(searchQuery));

                    if (matchesSearch) {
                        displayedBathrooms.add(bathroom);
                        addBathroomMarker(bathroom);
                    }
                }

                if (displayedBathrooms.isEmpty()) {
                    Toast.makeText(MainActivity.this, "No bathrooms match your filters", Toast.LENGTH_SHORT).show();
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

        // Priority Icon selection
        int iconResId = R.drawable.toilet__icon;
        if (bathroom.hasTag("accessible")) iconResId = R.drawable.wheelchair_solid_full;
        else if (bathroom.hasTag("cost")) iconResId = R.drawable.dollar_sign_solid_full;

        Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(position)
                .title(bathroom.getName())
                .anchor(0.5f, 0.5f)
                .icon(getBitmapDescriptorFromVector(this, iconResId)));

        if (marker != null) marker.setTag(bathroom);
    }

    private void openAddBathroomDialog(@Nullable LatLng preselectedLatLng) {
        AddBathroomDialog dialog = new AddBathroomDialog();
        dialog.setListener(this);
        if (preselectedLatLng != null) {
            String address = ""; // Geocoding happens inside the dialog for current location;
            // for map pick we pass coords and let dialog geocode via setSelectedLocation
            dialog.setSelectedLocation(preselectedLatLng, address);
        }
        dialog.show(getSupportFragmentManager(), "AddBathroomDialog");
    }

    // User tapped "Select on Map" — store the dialog and wait for a map tap
    @Override
    public void onSelectOnMap(AddBathroomDialog dialog) {
        pendingAddDialog = dialog;
        pickingLocation = true;
        Toast.makeText(this, "Tap the map to select a location", Toast.LENGTH_SHORT).show();
    }

    // Bathroom successfully added — refresh markers
    @Override
    public void onBathroomAdded() {
        fetchBathrooms(currentLatLng.latitude, currentLatLng.longitude,
                currentFilters.getMaxDistance() * 1000.0);
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

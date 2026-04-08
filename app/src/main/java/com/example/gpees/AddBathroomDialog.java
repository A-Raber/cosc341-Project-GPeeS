package com.example.gpees;

import android.app.Dialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddBathroomDialog extends DialogFragment {

    // Callback to MainActivity for when the user wants to pick a location on the map
    public interface AddBathroomListener {
        void onSelectOnMap(AddBathroomDialog dialog);
        void onBathroomAdded();
    }

    private static final String ARG_LAT = "selected_lat";
    private static final String ARG_LNG = "selected_lng";
    private static final String ARG_ADDRESS = "selected_address";

    private AddBathroomListener listener;
    private final DatabaseService dbService = new DatabaseService();
    private double selectedLat = Double.NaN;
    private double selectedLng = Double.NaN;
    private String selectedAddress = "";

    // Called by MainActivity to restore the dialog with a picked location
    public void setSelectedLocation(LatLng latLng, String address) {
        selectedLat = latLng.latitude;
        selectedLng = latLng.longitude;
        selectedAddress = address;

        Bundle args = getArguments() != null ? getArguments() : new Bundle();
        args.putDouble(ARG_LAT, latLng.latitude);
        args.putDouble(ARG_LNG, latLng.longitude);
        args.putString(ARG_ADDRESS, address);
        setArguments(args);

        if (getDialog() != null) {
            TextView tvSelectedLocation = getDialog().findViewById(R.id.tvSelectedLocation);
            if (tvSelectedLocation != null) {
                tvSelectedLocation.setText(address.isEmpty()
                        ? String.format(Locale.getDefault(), "%.5f, %.5f", latLng.latitude, latLng.longitude)
                        : address);
            }
        }
    }

    public void setListener(AddBathroomListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_add_bathroom);

        EditText editName = dialog.findViewById(R.id.editName);
        TextView tvSelectedLocation = dialog.findViewById(R.id.tvSelectedLocation);
        Button btnSelectOnMap = dialog.findViewById(R.id.btnSelectOnMap);
        Button btnCurrentLocation = dialog.findViewById(R.id.btnCurrentLocation);
        CheckBox checkAccessible = dialog.findViewById(R.id.checkAccessible);
        CheckBox checkSafe = dialog.findViewById(R.id.checkSafe);
        CheckBox checkFree = dialog.findViewById(R.id.checkFree);
        CheckBox checkClean = dialog.findViewById(R.id.checkClean);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnCreate = dialog.findViewById(R.id.btnCreate);

        // Restore selected location if returning from map picker
        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_LAT)) {
            selectedLat = args.getDouble(ARG_LAT);
            selectedLng = args.getDouble(ARG_LNG);
            selectedAddress = args.getString(ARG_ADDRESS, "");
            if (tvSelectedLocation != null) {
                tvSelectedLocation.setText(selectedAddress);
            }
        }

        if (btnSelectOnMap != null) {
            btnSelectOnMap.setOnClickListener(v -> {
                if (listener != null) listener.onSelectOnMap(this);
                dialog.hide();
            });
        }

        if (btnCurrentLocation != null) {
            btnCurrentLocation.setOnClickListener(v -> {
                LatLng current = BathroomDialog.currentLatLng;
                if (current == null) {
                    Toast.makeText(requireContext(), "Current location not available.", Toast.LENGTH_SHORT).show();
                    return;
                }
                new Thread(() -> {
                    String address = reverseGeocode(current.latitude, current.longitude);
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> {
                        selectedLat = current.latitude;
                        selectedLng = current.longitude;
                        selectedAddress = address;
                        if (tvSelectedLocation != null) {
                            tvSelectedLocation.setText(address.isEmpty()
                                    ? String.format(Locale.getDefault(), "%.5f, %.5f", selectedLat, selectedLng)
                                    : address);
                        }
                    });
                }).start();
            });
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dismiss());
        }

        if (btnCreate != null) {
            btnCreate.setOnClickListener(v -> {
                String name = editName != null ? editName.getText().toString().trim() : "";

                if (name.isEmpty()) {
                    Toast.makeText(requireContext(), "Please enter a name.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (Double.isNaN(selectedLat) || Double.isNaN(selectedLng)) {
                    Toast.makeText(requireContext(), "Please select a location.", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<String> tags = new ArrayList<>();
                if (checkAccessible != null && checkAccessible.isChecked()) tags.add("accessible");
                if (checkSafe != null && checkSafe.isChecked()) tags.add("safe");
                if (checkFree != null && checkFree.isChecked()) tags.add("free");
                if (checkClean != null && checkClean.isChecked()) tags.add("clean");

                Bathroom bathroom = new Bathroom(name, selectedAddress, selectedLat, selectedLng, tags);

                btnCreate.setEnabled(false);
                dbService.addBathroom(bathroom, new DatabaseService.WriteCallback() {
                    @Override
                    public void onSuccess() {
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Washroom added!", Toast.LENGTH_SHORT).show();
                            if (listener != null) listener.onBathroomAdded();
                            dismiss();
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Failed to add washroom.", Toast.LENGTH_SHORT).show();
                            btnCreate.setEnabled(true);
                        });
                    }
                });
            });
        }

        Window window = dialog.getWindow();
        if (window != null) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int width = (int) (metrics.widthPixels * 0.95);
            int height = (int) (metrics.heightPixels * 0.65);
            window.setLayout(width, height);
        }

        return dialog;
    }

    private String reverseGeocode(double lat, double lng) {
        try {
            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(address.getAddressLine(i));
                }
                return sb.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
package com.example.gpees;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.slider.Slider;

import java.util.List;

public class FilterDialog extends DialogFragment {

    public interface FilterListener {
        void onFilterApplied(FilterCriteria criteria);
    }

    private FilterListener listener;
    private FilterCriteria currentCriteria;

    // UI Components
    private RatingBar ratingBar;
    private TextView tvRatingLabel;
    private Slider distanceSlider;
    private TextView tvDistanceValue;
    private MaterialCheckBox cbAccessible, cbSafe, cbFree, cbClean;

    public FilterDialog(FilterCriteria currentCriteria, FilterListener listener) {
        this.currentCriteria = currentCriteria;
        this.listener = listener;
    }

    // Required empty constructor for FragmentManager
    public FilterDialog() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_filter, container, false);

        // Initialize UI
        ratingBar = view.findViewById(R.id.rating_bar_filter);
        tvRatingLabel = view.findViewById(R.id.tv_rating_label);
        distanceSlider = view.findViewById(R.id.slider_distance);
        tvDistanceValue = view.findViewById(R.id.tv_distance_value);
        cbAccessible = view.findViewById(R.id.cb_accessible);
        cbSafe = view.findViewById(R.id.cb_safe);
        cbFree = view.findViewById(R.id.cb_free);
        cbClean = view.findViewById(R.id.cb_clean);

        MaterialButton btnClearTags = view.findViewById(R.id.btn_clear_tags);
        MaterialButton btnCancel = view.findViewById(R.id.btn_cancel_filter);
        MaterialButton btnApply = view.findViewById(R.id.btn_apply_filter);

        // Set initial values from currentCriteria
        if (currentCriteria != null) {
            ratingBar.setRating(currentCriteria.getMinRating());
            updateRatingLabel(currentCriteria.getMinRating());

            distanceSlider.setValue(currentCriteria.getMaxDistance());
            tvDistanceValue.setText((int) currentCriteria.getMaxDistance() + " km");

            List<String> tags = currentCriteria.getTags();
            cbAccessible.setChecked(tags.contains("accessible"));
            cbSafe.setChecked(tags.contains("safe"));
            cbFree.setChecked(tags.contains("free"));
            cbClean.setChecked(tags.contains("clean"));
        }

        // Listeners for dynamic UI updates
        ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> updateRatingLabel(rating));

        distanceSlider.addOnChangeListener((slider, value, fromUser) -> tvDistanceValue.setText((int) value + " km"));

        btnClearTags.setOnClickListener(v -> {
            cbAccessible.setChecked(false);
            cbSafe.setChecked(false);
            cbFree.setChecked(false);
            cbClean.setChecked(false);
        });

        btnCancel.setOnClickListener(v -> dismiss());

        btnApply.setOnClickListener(v -> {
            FilterCriteria newCriteria = new FilterCriteria();
            newCriteria.setMinRating(ratingBar.getRating());
            newCriteria.setMaxDistance(distanceSlider.getValue());
            
            if (cbAccessible.isChecked()) newCriteria.addTag("accessible");
            if (cbSafe.isChecked()) newCriteria.addTag("safe");
            if (cbFree.isChecked()) newCriteria.addTag("free");
            if (cbClean.isChecked()) newCriteria.addTag("clean");

            if (listener != null) {
                listener.onFilterApplied(newCriteria);
            }
            dismiss();
        });

        return view;
    }

    private void updateRatingLabel(float rating) {
        tvRatingLabel.setText("Minimum " + rating + " stars");
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}

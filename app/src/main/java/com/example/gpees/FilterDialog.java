package com.example.gpees;

import android.os.Bundle;
import android.util.DisplayMetrics;
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

    // Discrete steps: 100m increments to 1km, then larger jumps
    private final float[] distanceSteps = {
            0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f,
            2.0f, 5.0f, 10.0f, 25.0f, 50.0f
    };

    private RatingBar ratingBar;
    private TextView tvRatingLabel;
    private Slider distanceSlider;
    private TextView tvDistanceValue;
    private MaterialCheckBox cbAccessible, cbSafe, cbFree, cbClean;

    public FilterDialog(FilterCriteria currentCriteria, FilterListener listener) {
        this.currentCriteria = currentCriteria;
        this.listener = listener;
    }

    public FilterDialog() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_filter, container, false);

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

        if (currentCriteria != null) {
            ratingBar.setRating(currentCriteria.getMinRating());
            updateRatingLabel(currentCriteria.getMinRating());

            // Set slider to the index of the closest step
            float currentDist = currentCriteria.getMaxDistance();
            distanceSlider.setValue(getNearestStepIndex(currentDist));
            updateDistanceLabel(currentDist);

            List<String> tags = currentCriteria.getTags();
            cbAccessible.setChecked(tags.contains("accessible"));
            cbSafe.setChecked(tags.contains("safe"));
            cbFree.setChecked(tags.contains("free"));
            cbClean.setChecked(tags.contains("clean"));
        }

        ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> updateRatingLabel(rating));

        distanceSlider.addOnChangeListener((slider, value, fromUser) -> {
            float actualDistance = distanceSteps[(int) value];
            updateDistanceLabel(actualDistance);
        });

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
            newCriteria.setMaxDistance(distanceSteps[(int) distanceSlider.getValue()]);
            
            if (cbAccessible.isChecked()) newCriteria.addTag("accessible");
            if (cbSafe.isChecked()) newCriteria.addTag("safe");
            if (cbFree.isChecked()) newCriteria.addTag("free");
            if (cbClean.isChecked()) newCriteria.addTag("clean");

            if (listener != null) listener.onFilterApplied(newCriteria);
            dismiss();
        });

        return view;
    }

    private void updateRatingLabel(float rating) {
        tvRatingLabel.setText("Minimum " + rating + " stars");
    }

    private void updateDistanceLabel(float distance) {
        if (distance < 1.0f) {
            tvDistanceValue.setText((int)(distance * 1000) + " m");
        } else {
            tvDistanceValue.setText((int) distance + " km");
        }
    }

    private int getNearestStepIndex(float distance) {
        int nearest = distanceSteps.length - 1;
        float minDiff = Float.MAX_VALUE;
        for (int i = 0; i < distanceSteps.length; i++) {
            float diff = Math.abs(distanceSteps[i] - distance);
            if (diff < minDiff) {
                minDiff = diff;
                nearest = i;
            }
        }
        return nearest;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int width = (int) (metrics.widthPixels * 0.95);
            getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}

package com.example.gpees;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;



import java.util.Date;

public class AddReviewDialog extends DialogFragment {

    private static final String ARG_BATHROOM = "bathroom_obj";

    private final DatabaseService dbService = new DatabaseService();

    public static AddReviewDialog newInstance(Bathroom bathroom) {
        AddReviewDialog fragment = new AddReviewDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_BATHROOM, bathroom);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_add_review);

        Bathroom bathroom = null;
        if (getArguments() != null) {
            bathroom = (Bathroom) getArguments().getSerializable(ARG_BATHROOM);
        }

        if (bathroom != null) {
            // Populate read-only bathroom info
            TextView tvName = dialog.findViewById(R.id.reviewBathroomName);
            if (tvName != null) tvName.setText(bathroom.getName());

            TextView tvAddress = dialog.findViewById(R.id.reviewBathroomAddress);
            if (tvAddress != null) tvAddress.setText(bathroom.getAddress());

            RatingBar ratingBar = dialog.findViewById(R.id.reviewRatingBar);
            EditText etReview = dialog.findViewById(R.id.reviewEditText);

            Bathroom finalBathroom = bathroom;

            // Submit button
            Button btnSubmit = dialog.findViewById(R.id.btnSubmitReview);
            if (btnSubmit != null) {
                btnSubmit.setOnClickListener(v -> {
                    float rating = ratingBar != null ? ratingBar.getRating() : 0f;
                    String reviewText = etReview != null ? etReview.getText().toString().trim() : "";

                    if (rating == 0f) {
                        Toast.makeText(requireContext(), "Please select a star rating.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Get username from Firebase Auth if available, otherwise Anonymous
                    String username = "Anonymous";

                    Review review = new Review();
                    review.setRating(rating);
                    review.setComment(reviewText);
                    review.setUsername(username);
                    review.setDate(new Date());

                    dbService.addReview(finalBathroom.getId(), review, new DatabaseService.WriteCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(requireContext(), "Review submitted!", Toast.LENGTH_SHORT).show();
                            dismiss();
                        }
                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(requireContext(), "Failed to submit review.", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            }

            // Cancel button — just dismiss, BathroomDialog remains underneath
            Button btnCancel = dialog.findViewById(R.id.btnCancelReview);
            if (btnCancel != null) {
                btnCancel.setOnClickListener(v -> dismiss());
            }
        }

        // Match BathroomDialog sizing
        Window window = dialog.getWindow();
        if (window != null) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int width = (int) (metrics.widthPixels * 0.95);
            int height = (int) (metrics.heightPixels * 0.55);
            window.setLayout(width, height);
        }

        return dialog;
    }
}
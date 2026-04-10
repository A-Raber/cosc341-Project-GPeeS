package com.example.gpees;

import android.app.Dialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ReviewsDialog extends DialogFragment {

    private static final String ARG_BATHROOM = "bathroom_obj";
    private final DatabaseService dbService = new DatabaseService();

    public static ReviewsDialog newInstance(Bathroom bathroom) {
        ReviewsDialog fragment = new ReviewsDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_BATHROOM, bathroom);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_reviews);

        Bathroom bathroom = null;
        if (getArguments() != null) {
            bathroom = (Bathroom) getArguments().getSerializable(ARG_BATHROOM);
        }

        if (bathroom != null) {
            TextView tvTitle = dialog.findViewById(R.id.tvTitle);
            TextView tvSubtitle = dialog.findViewById(R.id.tvSubtitle);

            if (tvTitle != null) tvTitle.setText(bathroom.getName());
            if (tvSubtitle != null) tvSubtitle.setText(bathroom.getAddress());

            if (bathroom.getId() != null && !bathroom.getId().isEmpty()) {
                loadReviews(dialog, bathroom.getId());
            }
        }

        Button btnClose = dialog.findViewById(R.id.btnClose);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dismiss());
        }

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        return dialog;
    }

    private void loadReviews(Dialog dialog, String bathroomId) {
        dbService.getReviews(bathroomId, new DatabaseService.ReviewsCallback() {
            @Override
            public void onSuccess(List<Review> reviews) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> bindReviews(dialog, reviews));
            }

            @Override
            public void onFailure(Exception e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> showError(dialog));
            }
        });
    }

    private void bindReviews(Dialog dialog, List<Review> reviews) {
        TextView tvReviewCount = dialog.findViewById(R.id.tvReviewCount);
        TextView tvAverageScore = dialog.findViewById(R.id.tvAverageScore);
        RatingBar ratingBarAverage = dialog.findViewById(R.id.ratingBarAverage);
        RecyclerView recyclerReviews = dialog.findViewById(R.id.recyclerReviews);
        TextView tvEmpty = dialog.findViewById(R.id.tvEmpty);

        if (reviews == null || reviews.isEmpty()) {
            if (tvReviewCount != null) tvReviewCount.setText("No reviews yet");
            if (tvAverageScore != null) tvAverageScore.setText("—");
            if (ratingBarAverage != null) ratingBarAverage.setRating(0f);
            if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
            if (recyclerReviews != null) recyclerReviews.setVisibility(View.GONE);
            return;
        }

        // Sort newest first
        Collections.sort(reviews, Comparator.comparing(
                Review::getDate, Comparator.nullsLast(Comparator.reverseOrder())
        ));

        // Compute average
        float total = 0f;
        for (Review r : reviews) total += r.getRating();
        float average = total / reviews.size();

        if (tvReviewCount != null)
            tvReviewCount.setText(String.format(Locale.getDefault(), "%d review%s", reviews.size(), reviews.size() == 1 ? "" : "s"));
        if (tvAverageScore != null)
            tvAverageScore.setText(String.format(Locale.getDefault(), "%.1f", average));
        if (ratingBarAverage != null)
            ratingBarAverage.setRating(average);

        if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
        if (recyclerReviews != null) {
            recyclerReviews.setVisibility(View.VISIBLE);
            recyclerReviews.setLayoutManager(new LinearLayoutManager(requireContext()));
            recyclerReviews.addItemDecoration(
                    new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
            );
            recyclerReviews.setAdapter(new ReviewAdapter(reviews));
        }
    }

    private void showError(Dialog dialog) {
        TextView tvEmpty = dialog.findViewById(R.id.tvEmpty);
        if (tvEmpty != null) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Unable to load reviews.");
        }
    }
}
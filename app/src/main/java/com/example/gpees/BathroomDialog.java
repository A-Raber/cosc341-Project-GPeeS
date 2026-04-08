package com.example.gpees;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class BathroomDialog extends DialogFragment {

    private static final String ARG_BATHROOM = "bathroom_obj";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("M/d/yy", Locale.getDefault());
    static LatLng currentLatLng;

    private final DatabaseService dbService = new DatabaseService();

    public static BathroomDialog newInstance(Bathroom bathroom, LatLng current) {
        BathroomDialog fragment = new BathroomDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_BATHROOM, bathroom);
        currentLatLng = current;
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_bathroom);

        Bathroom bathroom = null;
        if (getArguments() != null) {
            bathroom = (Bathroom) getArguments().getSerializable(ARG_BATHROOM);
        }

        if (bathroom != null) {
            TextView nameTextView = dialog.findViewById(R.id.bathroomNameText);
            if (nameTextView != null) {
                nameTextView.setText(bathroom.getName());
            }

            TextView addressTextView = dialog.findViewById(R.id.bathroomAddressText);
            if (addressTextView != null) {
                addressTextView.setText("");
                addressTextView.append("Address: ");
                addressTextView.append(bathroom.getAddress());
            }

            TextView bathroomDistance = dialog.findViewById(R.id.bathroomDistance);
            if(bathroomDistance != null && currentLatLng != null){
                bathroomDistance.setText("");
                double distance = DatabaseService.distanceMeters(currentLatLng.latitude, currentLatLng.longitude, bathroom.getLatitude(), bathroom.getLongitude());
                if (distance >= 1000) {
                    bathroomDistance.append(String.format(Locale.getDefault(), "%.1f km from you", distance / 1000.0));
                } else {
                    bathroomDistance.append(String.format(Locale.getDefault(), "%.0f m from you", distance));
                }
            }

            // Set up GO button for directions
            Button btnGo = dialog.findViewById(R.id.btnGo);
            if (btnGo != null) {
                Bathroom finalBathroom = bathroom;
                btnGo.setOnClickListener(v -> {
                    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + 
                            finalBathroom.getLatitude() + "," + finalBathroom.getLongitude() + "&mode=w");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                });
            }

            Bathroom finalBathroom = bathroom;
            Button btnShowReviews = dialog.findViewById(R.id.seeReviewsButton);
            if (btnShowReviews != null) {
                btnShowReviews.setOnClickListener(v -> showReviews(finalBathroom));
            }

            LinearLayout tagsContainer = dialog.findViewById(R.id.tagsContainer);
            if(tagsContainer != null){
                tagsContainer.removeAllViews();

                for(String tag : bathroom.getTags()){
                    TextView tagView = new TextView(requireContext());

                    tag = tag.substring(0,1).toUpperCase() + tag.substring(1);
                    tagView.setText(tag);
                    tagView.setTextSize(14);
                    tagView.setPadding(20, 10, 20, 10);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(0, 5, 15, 5);
                    tagView.setLayoutParams(params);

                    tagsContainer.addView(tagView);
                }
            }

            if (bathroom.getId() != null && !bathroom.getId().isEmpty()) {
                loadReviews(dialog, bathroom.getId());
                loadComments(dialog, bathroom.getId());
            }

        }

        Button btnClose = dialog.findViewById(R.id.btnClose);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dismiss());
        }

        Window window = dialog.getWindow();
        if (window != null) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int width = (int) (metrics.widthPixels * 0.95);
            int height = (int) (metrics.heightPixels * 0.85);
            window.setLayout(width, height);
        }

        return dialog;
    }

    private void loadReviews(Dialog dialog, String bathroomId) {
        dbService.getReviews(bathroomId, new DatabaseService.ReviewsCallback() {
            @Override
            public void onSuccess(List<Review> reviews) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> setReviews(dialog, reviews));
            }
            @Override
            public void onFailure(Exception e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> setReviewError(dialog));
            }
        });
    }

    private void loadComments(Dialog dialog, String bathroomId) {
        dbService.getComments(bathroomId, new DatabaseService.CommentsCallback() {
            @Override
            public void onSuccess(List<Comment> comments) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> setComments(dialog, comments));
            }
            @Override
            public void onFailure(Exception e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> setCommentError(dialog));
            }
        });
    }

    private void setReviews(Dialog dialog, List<Review> reviews) {
        RatingBar topRatingBar = dialog.findViewById(R.id.ratingBar);
        RatingBar previewRatingBar = dialog.findViewById(R.id.previewRatingBar);
        TextView previewReviewText = dialog.findViewById(R.id.previewReviewText);
        RatingBar bottomRatingBar = dialog.findViewById(R.id.bottomRatingBar);
        TextView bottomRatingText = dialog.findViewById(R.id.bottomRatingText);

        ProgressBar barOne = dialog.findViewById(R.id.barOne);
        ProgressBar barTwo = dialog.findViewById(R.id.barTwo);
        ProgressBar barThree = dialog.findViewById(R.id.barThree);
        ProgressBar barFour = dialog.findViewById(R.id.barFour);
        ProgressBar barFive = dialog.findViewById(R.id.barFive);

        if (reviews == null || reviews.isEmpty()) {
            if (topRatingBar != null)
                topRatingBar.setRating(0f);
            if (bottomRatingBar != null)
                bottomRatingBar.setRating(0f);
            if (bottomRatingText != null)
                bottomRatingText.setText("0 out of 5");
            if (previewRatingBar != null)
                previewRatingBar.setRating(0f);
            if (previewReviewText != null)
                previewReviewText.setText("No reviews yet.");

            return;
        }

        Collections.sort(reviews, Comparator.comparing(Review::getDate, Comparator.nullsLast(Comparator.reverseOrder())));

        float totalRating = 0f;
        int one = 0, two = 0, three = 0, four = 0, five = 0;
        for (Review review : reviews) {
            totalRating += review.getRating();
            int roundedDownRating = (int) Math.floor(review.getRating());
            if (roundedDownRating == 1)
                one++;
            else if (roundedDownRating == 2)
                two++;
            else if (roundedDownRating == 3)
                three++;
            else if (roundedDownRating == 4)
                four++;
            else if (roundedDownRating == 5)
                five++;
        }

        int totalReviews = reviews.size();
        if (barOne != null)
            barOne.setProgress(Math.round((one * 100f) / totalReviews));
        if (barTwo != null)
            barTwo.setProgress(Math.round((two * 100f) / totalReviews));
        if (barThree != null)
            barThree.setProgress(Math.round((three * 100f) / totalReviews));
        if (barFour != null)
            barFour.setProgress(Math.round((four * 100f) / totalReviews));
        if (barFive != null)
            barFive.setProgress(Math.round((five * 100f) / totalReviews));

        float averageRating = totalRating / reviews.size();
        Review latestReview = reviews.get(0);

        if (topRatingBar != null)
            topRatingBar.setRating(averageRating);
        if (bottomRatingBar != null)
            bottomRatingBar.setRating(averageRating);
        if (bottomRatingText != null)
            bottomRatingText.setText(String.format(Locale.getDefault(), "%.1f out of 5", averageRating));
        if (previewRatingBar != null)
            previewRatingBar.setRating(latestReview.getRating());

        if (previewReviewText != null) {
            String reviewText = latestReview.getComment();
            if (reviewText == null || reviewText.trim().isEmpty())
                reviewText = "No written review.";

            previewReviewText.setText(String.format(Locale.getDefault(), "%s said \"%s\"", safeUsername(latestReview.getUsername()), reviewText));
        }
    }

    private void setComments(Dialog dialog, List<Comment> comments) {
        LinearLayout commentsContainer = dialog.findViewById(R.id.commentsContainer);
        if (commentsContainer == null)
            return;

        commentsContainer.removeAllViews();

        if (comments == null || comments.isEmpty()) {
            commentsContainer.addView(buildCommentTextView("No comments yet."));
            return;
        }

        Collections.sort(comments, Comparator.comparing(Comment::getDate, Comparator.nullsLast(Comparator.reverseOrder())));

        for (int index = 0; index < comments.size(); index++) {
            Comment comment = comments.get(index);
            String dateText = comment.getDate() != null ? DATE_FORMAT.format(comment.getDate()) : "Unknown date";
            String commentText = comment.getComment();
            if (commentText == null || commentText.trim().isEmpty())
                commentText = "";

            commentsContainer.addView(buildCommentTextView(String.format(Locale.getDefault(), "%s  %s said '%s'", dateText, safeUsername(comment.getUsername()), commentText)));
            if (index < comments.size() - 1)
                commentsContainer.addView(buildDivider());
        }
    }

    private void setReviewError(Dialog dialog) {
        TextView previewReviewText = dialog.findViewById(R.id.previewReviewText);
        if (previewReviewText != null)
            previewReviewText.setText("Unable to load reviews.");
    }

    private void setCommentError(Dialog dialog) {
        LinearLayout commentsContainer = dialog.findViewById(R.id.commentsContainer);
        if (commentsContainer == null)
            return;

        commentsContainer.removeAllViews();
        commentsContainer.addView(buildCommentTextView("Unable to load comments."));
    }

    private TextView buildCommentTextView(String text) {
        TextView textView = new TextView(requireContext());
        textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        textView.setPadding(8, 8, 8, 8);
        textView.setTextSize(13);
        textView.setText(text);
        return textView;
    }

    private View buildDivider() {
        View divider = new View(requireContext());
        divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(Color.parseColor("#CCCCCC"));
        return divider;
    }

    private String safeUsername(String username) {
        return (username == null || username.trim().isEmpty()) ? "Anonymous" : username;
    }

    private void showReviews(Bathroom bathroom) {
        ReviewsDialog reviewsDialog = ReviewsDialog.newInstance(bathroom);
        reviewsDialog.show(getParentFragmentManager(), "ReviewsDialog");
    }
}

package com.example.gpees;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    private final List<Review> reviews;

    public ReviewAdapter(List<Review> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review r = reviews.get(position);

        String username = (r.getUsername() == null || r.getUsername().trim().isEmpty())
                ? "Anonymous" : r.getUsername();
        holder.tvUsername.setText(username);

        holder.ratingBar.setRating(r.getRating());

        holder.tvDate.setText(r.getDate() != null ? DATE_FORMAT.format(r.getDate()) : "");

        String comment = r.getComment();
        if (comment == null || comment.trim().isEmpty()) {
            holder.tvReviewText.setVisibility(View.GONE);
        } else {
            holder.tvReviewText.setVisibility(View.VISIBLE);
            holder.tvReviewText.setText(comment);
        }
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvDate, tvReviewText;
        RatingBar ratingBar;

        ViewHolder(View v) {
            super(v);
            tvUsername = v.findViewById(R.id.tvUsername);
            tvDate = v.findViewById(R.id.tvDate);
            tvReviewText = v.findViewById(R.id.tvReviewText);
            ratingBar = v.findViewById(R.id.ratingBar);
        }
    }
}
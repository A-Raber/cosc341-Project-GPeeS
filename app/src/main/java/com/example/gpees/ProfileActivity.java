package com.example.gpees;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profile_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // All reviews data
        String[][] allReviews = {
                {"Wendy's 123 Main St", "★★★★☆", "It works."},
                {"Tim Hortons, Bernard Ave", "★★★☆☆", "Clean enough, a bit crowded."},
                {"McDonald's, Harvey Ave", "★★☆☆☆", "Out of paper towels, again."},
                {"Starbucks, Bernard Ave", "★★★★★", "Surprisingly spotless. Would use again."}
        };

        // All comments data
        String[][] allComments = {
                {"On: Wendy's 123 Main St  ·  8/21/25", "Dont use last stall."},
                {"On: Tim Hortons, Bernard Ave  ·  9/02/25", "Hand dryer is broken FYI."},
                {"On: McDonald's, Harvey Ave  ·  10/14/25", "They locked the bathroom, you have to ask for the code."},
                {"On: Starbucks, Bernard Ave  ·  11/03/25", "Best smelling bathroom on Bernard, no contest."}
        };

        findViewById(R.id.btn_see_more_reviews).setOnClickListener(v ->
                showPopup("All Reviews", allReviews, true));

        findViewById(R.id.btn_see_more_comments).setOnClickListener(v ->
                showPopup("All Comments", allComments, false));
    }

    private void showPopup(String title, String[][] items, boolean isReview) {
        // Build a scrollable layout dynamically
        ScrollView scrollView = new ScrollView(this);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(32, 24, 32, 24);

        for (String[] item : items) {
            if (isReview) {
                // Title
                TextView tvTitle = new TextView(this);
                tvTitle.setText(item[0]);
                tvTitle.setTextSize(15);
                tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                tvTitle.setPadding(0, 16, 0, 4);
                container.addView(tvTitle);

                // Stars
                TextView tvStars = new TextView(this);
                tvStars.setText(item[1]);
                tvStars.setTextColor(0xFFFFC107);
                tvStars.setTextSize(14);
                container.addView(tvStars);

                // Body
                TextView tvBody = new TextView(this);
                tvBody.setText(item[2]);
                tvBody.setTextSize(14);
                tvBody.setTextColor(0xFF444444);
                tvBody.setPadding(0, 0, 0, 16);
                container.addView(tvBody);

            } else {
                // Comment header
                TextView tvHeader = new TextView(this);
                tvHeader.setText(item[0]);
                tvHeader.setTextSize(13);
                tvHeader.setTypeface(null, android.graphics.Typeface.BOLD);
                tvHeader.setTextColor(0xFF2C6E49);
                tvHeader.setPadding(0, 16, 0, 4);
                container.addView(tvHeader);

                // Comment body
                TextView tvBody = new TextView(this);
                tvBody.setText(item[1]);
                tvBody.setTextSize(14);
                tvBody.setTextColor(0xFF444444);
                tvBody.setPadding(0, 0, 0, 16);
                container.addView(tvBody);
            }

            // Divider
            android.view.View divider = new android.view.View(this);
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(0xFFDDDDDD);
            container.addView(divider);
        }

        scrollView.addView(container);

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(scrollView)
                .setPositiveButton("Close", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
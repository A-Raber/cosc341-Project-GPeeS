package com.example.gpees;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.List;

// this class is used for writing and reading to the database
public class DatabaseService {

    private final FirebaseFirestore db;

    public DatabaseService() {
        db = FirebaseFirestore.getInstance();
    }

    // Callbacks

    public interface BathroomsCallback {
        void onSuccess(List<Bathroom> bathrooms);
        void onFailure(Exception e);
    }

    public interface ReviewsCallback {
        void onSuccess(List<Review> reviews);
        void onFailure(Exception e);
    }

    public interface CommentsCallback {
        void onSuccess(List<Comment> comments);
        void onFailure(Exception e);
    }

    public interface WriteCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    // Add a new bathroom (with empty reviews)
    public void addBathroom(Bathroom bathroom, WriteCallback callback) {
        DocumentReference ref = db.collection("bathrooms").document();
        bathroom.setId(ref.getId());  // id gets assigned automatically
        ref.set(bathroom)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    // Read bathrooms within a lat and lng
    // TODO: Refactor to use metres instead of long / lat
    public void getBathroomsNearby(double lat, double lng, double radiusMeters, BathroomsCallback callback) {
        double[] bounds = getBoundingBox(lat, lng, radiusMeters);

        double minLat = bounds[0];
        double maxLat = bounds[1];

        db.collection("bathrooms")
                .whereGreaterThan("latitude", minLat)
                .whereLessThan("latitude", maxLat)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Bathroom> bathrooms = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Bathroom bathroom = doc.toObject(Bathroom.class);
                        if (bathroom != null) {
                            bathroom.setId(doc.getId());

                            if (distanceMeters(lat, lng, bathroom.getLatitude(), bathroom.getLongitude()) <= radiusMeters) {
                                bathrooms.add(bathroom);
                            }
                        }
                    }

                    callback.onSuccess(bathrooms);
                })
                .addOnFailureListener(callback::onFailure);
    }

    // Add a review and update the bathroom's average rating automatically
    public void addReview(String bathroomId, Review review, WriteCallback callback) {
        DocumentReference bathroomRef = db.collection("bathrooms").document(bathroomId);
        DocumentReference reviewRef = bathroomRef.collection("reviews").document();
        
        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot snapshot = transaction.get(bathroomRef);
            Bathroom bathroom = snapshot.toObject(Bathroom.class);
            
            if (bathroom != null) {
                // Calculate new average rating
                int newCount = bathroom.getReviewCount() + 1;
                float newRating = ((bathroom.getRating() * bathroom.getReviewCount()) + review.getRating()) / newCount;
                
                // Update bathroom object
                bathroom.setRating(newRating);
                bathroom.setReviewCount(newCount);
                
                // Set review ID and save
                review.setId(reviewRef.getId());
                transaction.set(reviewRef, review);
                transaction.update(bathroomRef, "rating", newRating, "reviewCount", newCount);
            }
            return null;
        }).addOnSuccessListener(aVoid -> callback.onSuccess())
          .addOnFailureListener(callback::onFailure);
    }

    // Read reviews for a bathroom
    public void getReviews(String bathroomId, ReviewsCallback callback) {
        db.collection("bathrooms")
                .document(bathroomId)
                .collection("reviews")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Review> reviews = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Review review = doc.toObject(Review.class);
                        if (review != null) {
                            review.setId(doc.getId());
                            reviews.add(review);
                        }
                    }
                    callback.onSuccess(reviews);
                }).addOnFailureListener(callback::onFailure);
    }

    // Read comments for a bathroom
    public void getComments(String bathroomId, CommentsCallback callback) {
        db.collection("bathrooms")
                .document(bathroomId)
                .collection("comments")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Comment> comments = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Comment comment = doc.toObject(Comment.class);
                        if (comment != null) {
                            comment.setId(doc.getId()); // Id gets assigned automatically
                            comments.add(comment);
                        }
                    }
                    callback.onSuccess(comments);
                })
                .addOnFailureListener(callback::onFailure);
    }

    // Add a comment to a bathroom
    public void addComment(String bathroomId, Comment comment, WriteCallback callback) {
        DocumentReference ref = db.collection("bathrooms")
                .document(bathroomId)
                .collection("comments")
                .document();
        comment.setId(ref.getId());  // id gets assigned automatically
        ref.set(comment)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    // Helper: Bounding Box for query
    public double[] getBoundingBox(double lat, double lng, double radiusMeters) {
        double latDelta = radiusMeters / 111320.0;
        double minLat = lat - latDelta;
        double maxLat = lat + latDelta;
        return new double[]{minLat, maxLat};
    }

    // Haversine Formula (Prevents getting bathrooms in a square)
    public static double distanceMeters(double lat1, double lng1, double lat2, double lng2) {
        double R = 6371000; // Earth radius in meters

        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}



// TODO: Add deleteComment() and deleteReview() for admins and users
package com.example.gpees;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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
        db.collection("bathrooms")
                .add(bathroom)
                .addOnSuccessListener(documentReference -> {
                    bathroom.setId(documentReference.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    // Read all bathrooms

    public void getBathrooms(BathroomsCallback callback) {
        db.collection("bathrooms")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Bathroom> bathrooms = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Bathroom bathroom = doc.toObject(Bathroom.class);
                        if (bathroom != null) {
                            bathroom.setId(doc.getId());
                            bathrooms.add(bathroom);
                        }
                    }
                    callback.onSuccess(bathrooms);
                })
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    // Read bathrooms within a lat and lng

    public void getBathroomsInRange(double minLat, double maxLat, double minLng, double maxLng, BathroomsCallback callback) {
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

                            // Filter longitude manually
                            if (bathroom.getLongitude() >= minLng && bathroom.getLongitude() <= maxLng) {
                                bathrooms.add(bathroom);
                            }
                        }
                    }
                    callback.onSuccess(bathrooms);
                })
                .addOnFailureListener(e -> callback.onFailure(e));
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
                            reviews.add(review);
                        }
                    }
                    callback.onSuccess(reviews);
                })
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    // Add a review to a bathroom

    public void addReview(String bathroomId, Review review, WriteCallback callback) {
        db.collection("bathrooms")
                .document(bathroomId)
                .collection("reviews")
                .add(review)
                .addOnSuccessListener(ref -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e));
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
                            comments.add(comment);
                        }
                    }
                    callback.onSuccess(comments);
                })
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    // Add a comment to a bathroom

    public void addComment(String bathroomId, Comment comment, WriteCallback callback) {
        db.collection("bathrooms")
                .document(bathroomId)
                .collection("comments")
                .add(comment)
                .addOnSuccessListener(ref -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e));
    }
}

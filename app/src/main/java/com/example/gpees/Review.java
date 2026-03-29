package com.example.gpees;

// Java object used for each review in each bathroom to be saved in fire base
public class Review {
    private String username;
    private float rating;
    private String comment;
    private String date;

    public Review() {}

    public Review(String username, float rating, String date) {
        this(username, rating, "", date);
    }
    public Review(String username, float rating, String comment, String date) {
        this.username = username;
        this.rating = rating;
        this.comment = comment;
        this.date = date;
    }

    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}

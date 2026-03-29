package com.example.gpees;

// Java object used for each comment in each bathroom to be saved in fire base
public class Comment {
    private String username;
    private String comment;
    private String date;

    public Comment() {}
    public Comment(String username, String comment, String date) {
        this.username = username;
        this.comment = comment;
        this.date = date;
    }

    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}

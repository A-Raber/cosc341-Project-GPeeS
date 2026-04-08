package com.example.gpees;

import java.util.Date;

// Java object used for each comment in each bathroom to be saved in fire base
public class Comment {
    private String id;
    private String username;
    private String comment;
    private Date date;

    public Comment() {}
    public Comment(String username, String comment, Date date) {
        this.username = username;
        this.comment = comment;
        this.date = date;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
}

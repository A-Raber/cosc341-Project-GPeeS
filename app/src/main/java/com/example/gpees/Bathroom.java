package com.example.gpees;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Java object used for each bathroom to be saved in fire base
public class Bathroom implements Serializable {
    private String id;
    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private List<String> tags;
    private float rating;
    private int reviewCount;

    public Bathroom() {
        this.tags = new ArrayList<>();
    }

    public Bathroom(String name, String address, double latitude, double longitude, List<String> tags) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.rating = 0.0f;
        this.reviewCount = 0;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    
    // Tag Functions
    public void addTag(String tag) {
        if (!hasTag(tag)) {
            tags.add(tag);
        }
    }
    public void removeTag(String tag) { tags.remove(tag); }
    public boolean hasTag(String tag) { return tags.contains(tag); }
    public List<String> getTags() { return tags; }
}

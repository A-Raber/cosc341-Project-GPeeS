package com.example.gpees;

import java.util.List;

// Java object used for each bathroom to be saved in fire base
public class Bathroom {
    private String id;
    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private List<String> tags;

    public Bathroom() {}

    public Bathroom(String name, String address, double latitude, double longitude, List<String> tags) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.tags = tags;
    }

    // Getters and setters for every field
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
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}

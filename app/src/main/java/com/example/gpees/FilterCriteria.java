package com.example.gpees;

import java.util.ArrayList;
import java.util.List;

/**
 * Data model to store user-selected filter settings.
 * Default values: 0 stars, 50km distance, no specific tags.
 */
public class FilterCriteria {
    private float minRating;
    private float maxDistance;
    private List<String> tags;

    public FilterCriteria() {
        this.minRating = 0.0f;
        this.maxDistance = 50.0f;
        this.tags = new ArrayList<>();
    }

    // Getters and Setters
    public float getMinRating() {
        return minRating;
    }

    public void setMinRating(float minRating) {
        this.minRating = minRating;
    }

    public float getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(float maxDistance) {
        this.maxDistance = maxDistance;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    public void addTag(String tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    public void removeTag(String tag) {
        tags.remove(tag);
    }

    public void clearTags() {
        tags.clear();
    }
}

package com.flc.memberbooking.model;

/**
 * DTO describing a lesson/exercise type returned to the UI.
 */
public class LessonTypeDto {
    private String key;
    private String displayName;
    private double price;

    public LessonTypeDto() {}

    public LessonTypeDto(String key, String displayName, double price) {
        this.key = key;
        this.displayName = displayName;
        this.price = price;
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}

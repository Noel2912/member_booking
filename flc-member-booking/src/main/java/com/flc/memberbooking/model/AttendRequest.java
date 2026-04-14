package com.flc.memberbooking.model;

public class AttendRequest {
    private String review;
    private Integer rating;

    public AttendRequest() {}

    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
}

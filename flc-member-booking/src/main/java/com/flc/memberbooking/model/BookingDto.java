package com.flc.memberbooking.model;

public class BookingDto {
    private int id;
    private MemberDto member;
    private String lessonId;
    private BookingStatus status;
    private String review;
    private Integer rating;

    public BookingDto() {}

    public BookingDto(int id, MemberDto member, String lessonId, BookingStatus status, String review, Integer rating) {
        this.id = id;
        this.member = member;
        this.lessonId = lessonId;
        this.status = status;
        this.review = review;
        this.rating = rating;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public MemberDto getMember() { return member; }
    public void setMember(MemberDto member) { this.member = member; }
    public String getLessonId() { return lessonId; }
    public void setLessonId(String lessonId) { this.lessonId = lessonId; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
}

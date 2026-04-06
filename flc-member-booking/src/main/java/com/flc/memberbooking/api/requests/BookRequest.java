package com.flc.memberbooking.api.requests;

public class BookRequest {
    private int memberId;
    private String lessonId;

    public BookRequest() {}

    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }
    public String getLessonId() { return lessonId; }
    public void setLessonId(String lessonId) { this.lessonId = lessonId; }
}

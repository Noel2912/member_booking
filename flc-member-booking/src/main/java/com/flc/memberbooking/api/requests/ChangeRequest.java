package com.flc.memberbooking.api.requests;

public class ChangeRequest {
    private String newLessonId;

    public ChangeRequest() {}

    public String getNewLessonId() { return newLessonId; }
    public void setNewLessonId(String newLessonId) { this.newLessonId = newLessonId; }
}

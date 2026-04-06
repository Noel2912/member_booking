package com.flc.memberbooking.model;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a booking of a member into a lesson.
 */
public class Booking {
    private static final AtomicInteger ID_SEQ = new AtomicInteger(1000);

    private final int id;
    private final Member member;
    private Lesson lesson;
    private BookingStatus status;
    private String review;
    private Integer rating; // 1..5

    public Booking(Member member, Lesson lesson) {
        this.id = ID_SEQ.getAndIncrement();
        this.member = member;
        this.lesson = lesson;
        this.status = BookingStatus.BOOKED;
    }

    public int getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public void setLesson(Lesson lesson) {
        this.lesson = lesson;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public Optional<String> getReview() {
        return Optional.ofNullable(review);
    }

    public void setReview(String review) {
        this.review = review;
    }

    public Optional<Integer> getRating() {
        return Optional.ofNullable(rating);
    }

    public void setRating(Integer rating) {
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "Booking{" + id + ", member=" + member.getName() + ", lesson=" + lesson.getDisplayName() + ", status=" + status + '}';
    }
}

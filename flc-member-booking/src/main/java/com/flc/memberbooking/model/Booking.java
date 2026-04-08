package com.flc.memberbooking.model;

import jakarta.persistence.*;
import java.util.Optional;

/**
 * Represents a booking of a member into a lesson.
 */
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    private Member member;

    @ManyToOne
    private Lesson lesson;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private String review;

    private Integer rating; // 1..5

    protected Booking() {}

    public Booking(Member member, Lesson lesson) {
        this.member = member;
        this.lesson = lesson;
        this.status = BookingStatus.BOOKED;
    }

    public Integer getId() {
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
        return "Booking{" + id + ", member=" + member.getName() + ", lesson=" + (lesson != null ? lesson.getDisplayName() : "null") + ", status=" + status + '}';
    }
}

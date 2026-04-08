package com.flc.memberbooking.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

/**
 * Scheduled lesson instance.
 */
@Entity
@Table(name = "lessons")
public class Lesson {
    @Id
    private String id; // human readable id

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LessonType type;

    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private TimeSlot timeSlot;

    private int capacity = 4;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();

    protected Lesson() {}

    public Lesson(String id, LessonType type, LocalDate date, TimeSlot timeSlot) {
        this.id = id;
        this.type = type;
        this.date = date;
        this.timeSlot = timeSlot;
    }

    public String getId() {
        return id;
    }

    public LessonType getType() {
        return type;
    }

    public LocalDate getDate() {
        return date;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public int availableSeats() {
        return capacity - (int) bookings.stream().filter(b -> b.getStatus() == BookingStatus.BOOKED || b.getStatus() == BookingStatus.CHANGED).count();
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean addBooking(Booking booking) {
        if (availableSeats() <= 0) return false;
        bookings.add(booking);
        booking.setLesson(this);
        return true;
    }

    public boolean removeBooking(Booking booking) {
        boolean removed = bookings.remove(booking);
        if (removed) booking.setLesson(null);
        return removed;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public String getDisplayName() {
        return date + " " + timeSlot + " - " + type.getDisplayName();
    }

    public OptionalDouble averageRating() {
        return bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.ATTENDED)
                .map(Booking::getRating)
                .filter(java.util.Optional::isPresent)
                .mapToDouble(r -> r.get().doubleValue())
                .average();
    }

    public double totalIncome() {
        long count = bookings.stream().filter(b -> b.getStatus() != BookingStatus.CANCELLED).count();
        return type.getPrice().doubleValue() * count;
    }

    @Override
    public String toString() {
        return "Lesson{" + id + ", " + getDisplayName() + ", seats=" + availableSeats() + ", price=" + type.getPrice() + '}';
    }
}

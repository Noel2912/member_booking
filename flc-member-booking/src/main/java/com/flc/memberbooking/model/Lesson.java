package com.flc.memberbooking.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

/**
 * Scheduled lesson instance.
 */
public class Lesson {
    private final String id; // human readable id
    private final LessonType type;
    private final LocalDate date;
    private final TimeSlot timeSlot;
    private final int capacity = 4;
    private final List<Booking> bookings = new ArrayList<>();

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

    public boolean addBooking(Booking booking) {
        if (availableSeats() <= 0) return false;
        bookings.add(booking);
        return true;
    }

    public boolean removeBooking(Booking booking) {
        return bookings.remove(booking);
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
        // Count all attended or booked? For revenue they count all bookings (assume booked means paid)
        long count = bookings.stream().filter(b -> b.getStatus() != BookingStatus.CANCELLED).count();
        return type.getPrice().doubleValue() * count;
    }

    @Override
    public String toString() {
        return "Lesson{" + id + ", " + getDisplayName() + ", seats=" + availableSeats() + ", price=" + type.getPrice() + '}';
    }
}

package com.flc.memberbooking.api;

import java.time.LocalDate;
import com.flc.memberbooking.model.TimeSlot;

public class LessonDto {
    private String id;
    private String type;
    private LocalDate date;
    private TimeSlot timeSlot;
    private int availableSeats;
    private double price;

    public LessonDto() {}

    public LessonDto(String id, String type, LocalDate date, TimeSlot timeSlot, int availableSeats, double price) {
        this.id = id;
        this.type = type;
        this.date = date;
        this.timeSlot = timeSlot;
        this.availableSeats = availableSeats;
        this.price = price;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public TimeSlot getTimeSlot() { return timeSlot; }
    public void setTimeSlot(TimeSlot timeSlot) { this.timeSlot = timeSlot; }
    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}

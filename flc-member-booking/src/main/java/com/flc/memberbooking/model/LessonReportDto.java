package com.flc.memberbooking.model;

import java.time.LocalDate;

public class LessonReportDto {
    private String id;
    private String type;
    private LocalDate date;
    private TimeSlot timeSlot;
    private int attendedCount;
    private Double averageRating; // nullable
    private double income;
    private double price;

    public LessonReportDto() {}

    public LessonReportDto(String id, String type, LocalDate date, TimeSlot timeSlot, int attendedCount, Double averageRating, double income, double price) {
        this.id = id;
        this.type = type;
        this.date = date;
        this.timeSlot = timeSlot;
        this.attendedCount = attendedCount;
        this.averageRating = averageRating;
        this.income = income;
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
    public int getAttendedCount() { return attendedCount; }
    public void setAttendedCount(int attendedCount) { this.attendedCount = attendedCount; }
    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    public double getIncome() { return income; }
    public void setIncome(double income) { this.income = income; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}

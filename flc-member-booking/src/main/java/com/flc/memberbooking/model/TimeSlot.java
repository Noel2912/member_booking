package com.flc.memberbooking.model;

public enum TimeSlot {
    MORNING,
    AFTERNOON,
    EVENING;

    @Override
    public String toString() {
        switch (this) {
            case MORNING: return "Morning";
            case AFTERNOON: return "Afternoon";
            case EVENING: return "Evening";
            default: return super.toString();
        }
    }
}

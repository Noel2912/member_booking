package com.flc.memberbooking.model;

import java.math.BigDecimal;

/**
 * Types of lessons with fixed price per type.
 */
public enum LessonType {
    YOGA("Yoga", new BigDecimal("6.50")),
    ZUMBA("Zumba", new BigDecimal("7.00")),
    AQUACISE("Aquacise", new BigDecimal("8.00")),
    BOX_FIT("Box Fit", new BigDecimal("6.00")),
    BODY_BLITZ("Body Blitz", new BigDecimal("5.50"));

    private final String displayName;
    private final BigDecimal price;

    LessonType(String displayName, BigDecimal price) {
        this.displayName = displayName;
        this.price = price;
    }

    public String getDisplayName() {
        return displayName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return displayName + " (" + price + ")";
    }
}

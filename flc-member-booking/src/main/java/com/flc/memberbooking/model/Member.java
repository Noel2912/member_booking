package com.flc.memberbooking.model;

import java.util.concurrent.atomic.AtomicInteger;

public class Member {
    private static final AtomicInteger ID_SEQ = new AtomicInteger(1);

    private final int id;
    private final String name;

    public Member(String name) {
        this.id = ID_SEQ.getAndIncrement();
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Member{" + id + ": '" + name + "'}";
    }
}

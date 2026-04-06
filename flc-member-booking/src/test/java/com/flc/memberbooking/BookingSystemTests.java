package com.flc.memberbooking;

import com.flc.memberbooking.model.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BookingSystemTests {
    private BookingSystem system;

    @BeforeEach
    public void setup() {
        system = new BookingSystem();
    }

    @Test
    public void testBookingSuccess() {
        Member m = system.getMembers().iterator().next();
        List<Lesson> lessons = system.getAllLessons();
        Lesson l = lessons.get(0);
        int before = l.availableSeats();
        var maybe = system.bookLesson(m.getId(), l.getId());
        assertTrue(maybe.isPresent());
        assertEquals(before - 1, l.availableSeats());
    }

    @Test
    public void testBookingCapacityEnforced() {
        List<Member> mems = system.getMembers().stream().toList();
        Lesson l = system.getAllLessons().get(1);
        // ensure at least 4 distinct members exist; if not, create
        while (mems.size() < 5) {
            // not adding dynamic members in this test; rely on seeded members
            break;
        }
        int added = 0;
        for (Member m : mems) {
            if (added >= 4) break;
            var b = system.bookLesson(m.getId(), l.getId());
            assertTrue(b.isPresent());
            added++;
        }
        // now capacity reached
        Member extra = mems.get(0);
        var fail = system.bookLesson(extra.getId(), l.getId());
        assertTrue(fail.isEmpty());
    }

    @Test
    public void testChangeBooking() {
        Member m = system.getMembers().iterator().next();
        Lesson a = system.getAllLessons().get(2);
        Lesson b = system.getAllLessons().get(3);
        var book = system.bookLesson(m.getId(), a.getId());
        assertTrue(book.isPresent());
        int bid = book.get().getId();
        boolean ok = system.changeBooking(bid, b.getId());
        assertTrue(ok);
        var maybe = system.findBooking(bid);
        assertTrue(maybe.isPresent());
        assertEquals(BookingStatus.CHANGED, maybe.get().getStatus());
        assertEquals(b.getId(), maybe.get().getLesson().getId());
    }

    @Test
    public void testAttendAndReview() {
        Member m = system.getMembers().iterator().next();
        Lesson l = system.getAllLessons().get(4);
        var book = system.bookLesson(m.getId(), l.getId());
        assertTrue(book.isPresent());
        int bid = book.get().getId();
        boolean ok = system.attendBooking(bid, "Great class", 5);
        assertTrue(ok);
        var maybe = system.findBooking(bid);
        assertTrue(maybe.isPresent());
        assertEquals(BookingStatus.ATTENDED, maybe.get().getStatus());
        assertEquals(5, maybe.get().getRating().get().intValue());
    }

    @Test
    public void testIncomeByType() {
        Member m = system.getMembers().iterator().next();
        // book two lessons of the same type in same month
        Lesson l1 = system.getAllLessons().get(0);
        Lesson l2 = system.getAllLessons().stream().filter(x -> x.getType()==l1.getType()).findFirst().get();
        system.bookLesson(m.getId(), l1.getId());
        system.bookLesson(m.getId(), l2.getId());
        int month = l1.getDate().getMonthValue();
        var map = system.incomeByType(month);
        assertTrue(map.containsKey(l1.getType()));
        double income = map.get(l1.getType());
        // should be at least price * 2
        assertTrue(income >= l1.getType().getPrice().doubleValue() * 2 - 0.0001);
    }
}

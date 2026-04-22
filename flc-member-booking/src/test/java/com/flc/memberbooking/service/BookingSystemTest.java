package com.flc.memberbooking.service;

import com.flc.memberbooking.model.*;
import com.flc.memberbooking.repository.BookingRepository;
import com.flc.memberbooking.repository.LessonRepository;
import com.flc.memberbooking.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingSystemTest {

    @Mock
    MemberRepository memberRepository;

    @Mock
    LessonRepository lessonRepository;

    @Mock
    BookingRepository bookingRepository;

    @Captor
    ArgumentCaptor<Booking> bookingCaptor;

    @Test
    public void seedsWhenEmpty() {
        when(memberRepository.count()).thenReturn(0L);
        when(lessonRepository.count()).thenReturn(0L);

        // constructing should call seed methods
        new BookingSystem(memberRepository, lessonRepository, bookingRepository);

        // expect at least 5 members saved
        verify(memberRepository, atLeast(5)).save(any(Member.class));
        // expect lessons saved
        verify(lessonRepository, atLeastOnce()).save(any(Lesson.class));
    }

    @Test
    public void viewByDayAndExercise() {
        Lesson l1 = new Lesson("S1", LessonType.YOGA, LocalDate.of(2026,4,4), TimeSlot.MORNING); // saturday
        Lesson l2 = new Lesson("S2", LessonType.ZUMBA, LocalDate.of(2026,4,5), TimeSlot.AFTERNOON); // sunday
        when(lessonRepository.findAll()).thenReturn(List.of(l1, l2));
        when(memberRepository.count()).thenReturn(1L);
        when(lessonRepository.count()).thenReturn(1L);

        BookingSystem bs = new BookingSystem(memberRepository, lessonRepository, bookingRepository);

        var byDay = bs.viewByDay("saturday");
        assertEquals(1, byDay.size());
        assertEquals("S1", byDay.get(0).getId());

        var byEx = bs.viewByExercise(LessonType.ZUMBA.getDisplayName());
        assertEquals(1, byEx.size());
        assertEquals("S2", byEx.get(0).getId());
    }

    @Test
    public void bookingFailureReasonsAndBookLesson() {
        Member m = new Member("Alice");
        Lesson lesson = new Lesson("L1", LessonType.YOGA, LocalDate.of(2026,4,4), TimeSlot.MORNING);

        when(memberRepository.findById(1)).thenReturn(Optional.empty());
        when(lessonRepository.findById("L1")).thenReturn(Optional.of(lesson));
        when(memberRepository.count()).thenReturn(1L);
        when(lessonRepository.count()).thenReturn(1L);
        BookingSystem bs = new BookingSystem(memberRepository, lessonRepository, bookingRepository);

        assertEquals("invalid", bs.bookingFailureReason(1, "L1"));

        when(memberRepository.findById(1)).thenReturn(Optional.of(m));
        when(bookingRepository.existsByMemberAndLessonAndStatusNot(any(), any(), any())).thenReturn(true);
        assertEquals("duplicate", bs.bookingFailureReason(1, "L1"));

        when(bookingRepository.existsByMemberAndLessonAndStatusNot(any(), any(), any())).thenReturn(false);
        when(bookingRepository.countByLessonAndStatusNot(eq(lesson), any())).thenReturn((long)lesson.getCapacity());
        assertEquals("full", bs.bookingFailureReason(1, "L1"));

        when(bookingRepository.countByLessonAndStatusNot(eq(lesson), any())).thenReturn(0L);
        assertNull(bs.bookingFailureReason(1, "L1"));

        // bookLesson should return empty when duplicate/full/invalid
        when(memberRepository.findById(1)).thenReturn(Optional.of(m));
        when(lessonRepository.findById("L1")).thenReturn(Optional.of(lesson));
        when(bookingRepository.existsByMemberAndLessonAndStatusNot(any(), any(), any())).thenReturn(false);
        when(bookingRepository.countByLessonAndStatusNot(eq(lesson), any())).thenReturn(0L);
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var maybe = bs.bookLesson(1, "L1");
        assertTrue(maybe.isPresent());
        assertEquals(m, maybe.get().getMember());

        // if member missing
        when(memberRepository.findById(2)).thenReturn(Optional.empty());
        assertTrue(bs.bookLesson(2, "L1").isEmpty());
    }

    @Test
    public void changeCancelAttendFindAndList() {
        Member m = new Member("Bob");
        Lesson a = new Lesson("A", LessonType.YOGA, LocalDate.of(2026,4,4), TimeSlot.MORNING);
        Lesson b = new Lesson("B", LessonType.ZUMBA, LocalDate.of(2026,4,5), TimeSlot.AFTERNOON);
        Booking booking = new Booking(m, a);

        when(bookingRepository.findById(10)).thenReturn(Optional.empty());
        when(lessonRepository.findById("B")).thenReturn(Optional.of(b));
        when(bookingRepository.findById(11)).thenReturn(Optional.of(booking));
        when(lessonRepository.findById("B")).thenReturn(Optional.of(b));
        when(bookingRepository.countByLessonAndStatusNot(eq(b), any())).thenReturn(0L);
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(bookingRepository.findAll()).thenReturn(List.of(booking));
        when(bookingRepository.findById(11)).thenReturn(Optional.of(booking));

        BookingSystem bs = new BookingSystem(memberRepository, lessonRepository, bookingRepository);

        // changeBooking: booking id not found -> false
        assertFalse(bs.changeBooking(10, "B"));

        // changeBooking success
        assertTrue(bs.changeBooking(11, "B"));
        assertEquals(BookingStatus.CHANGED, booking.getStatus());
        assertEquals(b, booking.getLesson());

        // cancel
        when(bookingRepository.findById(20)).thenReturn(Optional.of(booking));
        assertTrue(bs.cancelBooking(20));
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());

        // attend
        when(bookingRepository.findById(30)).thenReturn(Optional.of(booking));
        assertTrue(bs.attendBooking(30, "Great", 5));
        assertEquals(BookingStatus.ATTENDED, booking.getStatus());
        assertTrue(booking.getReview().isPresent());
        assertTrue(booking.getRating().isPresent());

        // findBooking and getAllBookings
        when(bookingRepository.findById(99)).thenReturn(Optional.of(booking));
        assertTrue(bs.findBooking(99).isPresent());
        assertEquals(1, bs.getAllBookings().size());
    }

    @Test
    public void monthlyStatsAndIncome() {
        Lesson l1 = new Lesson("L1", LessonType.YOGA, LocalDate.of(2026,4,4), TimeSlot.MORNING);
        Lesson l2 = new Lesson("L2", LessonType.YOGA, LocalDate.of(2026,4,4), TimeSlot.AFTERNOON);
        Booking b1 = new Booking(new Member("X"), l1);
        Booking b2 = new Booking(new Member("Y"), l1);
        l1.addBooking(b1);
        l1.addBooking(b2);
        when(lessonRepository.findAll()).thenReturn(List.of(l1, l2));
        when(memberRepository.count()).thenReturn(1L);
        when(lessonRepository.count()).thenReturn(1L);

        BookingSystem bs = new BookingSystem(memberRepository, lessonRepository, bookingRepository);

        var map = bs.monthlyLessonStats(4);
        assertTrue(map.containsKey(LocalDate.of(2026,4,4)));

        var income = bs.incomeByType(4);
        assertTrue(income.containsKey(LessonType.YOGA));
        double expected = LessonType.YOGA.getPrice().doubleValue() * 2;
        assertEquals(expected, income.get(LessonType.YOGA), 0.0001);
    }
}

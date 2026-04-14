package com.flc.memberbooking.service;

import com.flc.memberbooking.model.*;
import com.flc.memberbooking.repository.BookingRepository;
import com.flc.memberbooking.repository.LessonRepository;
import com.flc.memberbooking.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Booking system backed by JPA repositories (H2 database).
 */
@Service
@Transactional
public class BookingSystem {
    private final MemberRepository memberRepository;
    private final LessonRepository lessonRepository;
    private final BookingRepository bookingRepository;

    public BookingSystem(MemberRepository memberRepository, LessonRepository lessonRepository, BookingRepository bookingRepository) {
        this.memberRepository = memberRepository;
        this.lessonRepository = lessonRepository;
        this.bookingRepository = bookingRepository;

        // seed initial data if empty
        if (memberRepository.count() == 0) seedMembers();
        if (lessonRepository.count() == 0) seedLessons();
    }

    private void seedMembers() {
        memberRepository.save(new Member("Alice"));
        memberRepository.save(new Member("Bob"));
        memberRepository.save(new Member("Carol"));
        memberRepository.save(new Member("David"));
        memberRepository.save(new Member("Eve"));
    }

    public Collection<Member> getMembers() { return memberRepository.findAll(); }

    private void seedLessons() {
        LocalDate start = LocalDate.of(2026,4,4); // Saturday
        int weekendCount = 8;
        LessonType[] types = LessonType.values();
        for (int w = 0; w < weekendCount; w++) {
            LocalDate saturday = start.plusWeeks(w);
            LocalDate sunday = saturday.plusDays(1);
            createLessonsForDay(saturday, w+1, types);
            createLessonsForDay(sunday, w+1, types);
        }
    }

    private void createLessonsForDay(LocalDate date, int weekendNumber, LessonType[] types) {
        TimeSlot[] slots = TimeSlot.values();
        for (int i = 0; i < slots.length; i++) {
            LessonType type = types[(weekendNumber + i) % types.length];
            String id = String.format("W%02d-%s-%s", weekendNumber, date.getDayOfWeek().toString().substring(0,3), slots[i].toString().substring(0,1));
            Lesson lesson = new Lesson(id, type, date, slots[i]);
            lessonRepository.save(lesson);
        }
    }

    public List<Lesson> viewByDay(String dayName) {
        DayOfWeek dow = dayName.equalsIgnoreCase("saturday") ? DayOfWeek.SATURDAY : DayOfWeek.SUNDAY;
        return lessonRepository.findAll().stream().filter(l -> l.getDate().getDayOfWeek() == dow).collect(Collectors.toList());
    }

    public List<Lesson> viewByExercise(String exerciseName) {
        return lessonRepository.findAll().stream().filter(l -> l.getType().getDisplayName().equalsIgnoreCase(exerciseName)).collect(Collectors.toList());
    }

    public Optional<Booking> bookLesson(int memberId, String lessonId) {
        Optional<Member> m = memberRepository.findById(memberId);
        Optional<Lesson> lessonOpt = lessonRepository.findById(lessonId);
        if (m.isEmpty() || lessonOpt.isEmpty()) return Optional.empty();
        Member member = m.get();
        Lesson lesson = lessonOpt.get();
        // prevent duplicate booking (non-cancelled)
        boolean dup = bookingRepository.existsByMemberAndLessonAndStatusNot(member, lesson, BookingStatus.CANCELLED);
        if (dup) return Optional.empty();
        // check capacity
        long current = bookingRepository.countByLessonAndStatusNot(lesson, BookingStatus.CANCELLED);
        if (lesson.getCapacity() - current <= 0) return Optional.empty();
        Booking b = new Booking(member, lesson);
        bookingRepository.save(b);
        return Optional.of(b);
    }

    /**
     * Return a short failure reason for attempting to book ("invalid","duplicate","full")
     * or null when booking should proceed.
     */
    public String bookingFailureReason(int memberId, String lessonId) {
        Optional<Member> m = memberRepository.findById(memberId);
        Optional<Lesson> lessonOpt = lessonRepository.findById(lessonId);
        if (m.isEmpty() || lessonOpt.isEmpty()) return "invalid";
        Member member = m.get();
        Lesson lesson = lessonOpt.get();
        boolean dup = bookingRepository.existsByMemberAndLessonAndStatusNot(member, lesson, BookingStatus.CANCELLED);
        if (dup) return "duplicate";
        long current = bookingRepository.countByLessonAndStatusNot(lesson, BookingStatus.CANCELLED);
        if (lesson.getCapacity() - current <= 0) return "full";
        return null;
    }

    public Optional<Booking> findBooking(int bookingId) {
        return bookingRepository.findById(bookingId);
    }

    public List<Booking> getAllBookings() { return bookingRepository.findAll(); }

    public boolean changeBooking(int bookingId, String newLessonId) {
        Optional<Booking> ob = bookingRepository.findById(bookingId);
        Optional<Lesson> nl = lessonRepository.findById(newLessonId);
        if (ob.isEmpty() || nl.isEmpty()) return false;
        Booking b = ob.get();
        Lesson newLesson = nl.get();
        long current = bookingRepository.countByLessonAndStatusNot(newLesson, BookingStatus.CANCELLED);
        if (newLesson.getCapacity() - current <= 0) return false;
        b.setLesson(newLesson);
        b.setStatus(BookingStatus.CHANGED);
        bookingRepository.save(b);
        return true;
    }

    public boolean cancelBooking(int bookingId) {
        Optional<Booking> ob = bookingRepository.findById(bookingId);
        if (ob.isEmpty()) return false;
        Booking b = ob.get();
        b.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(b);
        return true;
    }

    public boolean attendBooking(int bookingId, String review, int rating) {
        Optional<Booking> ob = bookingRepository.findById(bookingId);
        if (ob.isEmpty()) return false;
        Booking b = ob.get();
        b.setStatus(BookingStatus.ATTENDED);
        b.setReview(review);
        b.setRating(rating);
        bookingRepository.save(b);
        return true;
    }

    public List<Lesson> getAllLessons() { return lessonRepository.findAll(); }

    public Map<LocalDate, List<Lesson>> monthlyLessonStats(int month) {
        return lessonRepository.findAll().stream()
                .filter(l -> l.getDate().getMonthValue() == month)
                .collect(Collectors.groupingBy(Lesson::getDate, TreeMap::new, Collectors.toList()));
    }

    public Map<LessonType, Double> incomeByType(int month) {
        Map<LessonType, Double> map = new EnumMap<>(LessonType.class);
        for (Lesson l : lessonRepository.findAll()) {
            if (l.getDate().getMonthValue() != month) continue;
            map.merge(l.getType(), l.totalIncome(), Double::sum);
        }
        return map;
    }

}

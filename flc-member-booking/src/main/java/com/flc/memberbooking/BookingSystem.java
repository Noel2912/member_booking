package com.flc.memberbooking;

import org.springframework.stereotype.Service;
import com.flc.memberbooking.model.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory booking system.
 */
@Service
public class BookingSystem {
    private final Map<Integer, Member> members = new HashMap<>();
    private final Map<String, Lesson> lessons = new LinkedHashMap<>();
    private final Map<Integer, Booking> bookings = new HashMap<>();

    public BookingSystem() {
        seedMembers();
        seedLessons();
    }

    private void seedMembers() {
        addMember(new Member("Alice"));
        addMember(new Member("Bob"));
        addMember(new Member("Carol"));
        addMember(new Member("David"));
        addMember(new Member("Eve"));
    }

    private void addMember(Member m) {
        members.put(m.getId(), m);
    }

    public Collection<Member> getMembers() { return members.values(); }

    private void seedLessons() {
        // create 8 weekends starting from a chosen Saturday
        LocalDate start = LocalDate.of(2026,4,4); // Saturday
        int weekendCount = 8;
        LessonType[] types = LessonType.values();
        for (int w = 0; w < weekendCount; w++) {
            LocalDate saturday = start.plusWeeks(w);
            LocalDate sunday = saturday.plusDays(1);
            createLessonsForDay(saturday, w+1, true, types);
            createLessonsForDay(sunday, w+1, false, types);
        }
    }

    private void createLessonsForDay(LocalDate date, int weekendNumber, boolean isSaturday, LessonType[] types) {
        TimeSlot[] slots = TimeSlot.values();
        for (int i = 0; i < slots.length; i++) {
            LessonType type = types[(weekendNumber + i) % types.length];
            String id = String.format("W%02d-%s-%s", weekendNumber, date.getDayOfWeek().toString().substring(0,3), slots[i].toString().substring(0,1));
            Lesson lesson = new Lesson(id, type, date, slots[i]);
            lessons.put(id, lesson);
        }
    }

    public List<Lesson> viewByDay(String dayName) {
        DayOfWeek dow = dayName.equalsIgnoreCase("saturday") ? DayOfWeek.SATURDAY : DayOfWeek.SUNDAY;
        return lessons.values().stream().filter(l -> l.getDate().getDayOfWeek() == dow).collect(Collectors.toList());
    }

    public List<Lesson> viewByExercise(String exerciseName) {
        return lessons.values().stream().filter(l -> l.getType().getDisplayName().equalsIgnoreCase(exerciseName)).collect(Collectors.toList());
    }

    public Optional<Booking> bookLesson(int memberId, String lessonId) {
        Member m = members.get(memberId);
        Lesson lesson = lessons.get(lessonId);
        if (m == null || lesson == null) return Optional.empty();
        // prevent duplicate booking
        boolean dup = bookings.values().stream().anyMatch(b -> b.getMember().getId() == memberId && b.getLesson().getId().equals(lessonId) && b.getStatus() != BookingStatus.CANCELLED);
        if (dup) return Optional.empty();
        if (lesson.availableSeats() <= 0) return Optional.empty();
        Booking b = new Booking(m, lesson);
        lesson.addBooking(b);
        bookings.put(b.getId(), b);
        return Optional.of(b);
    }

    public Optional<Booking> findBooking(int bookingId) {
        return Optional.ofNullable(bookings.get(bookingId));
    }

    public boolean changeBooking(int bookingId, String newLessonId) {
        Booking b = bookings.get(bookingId);
        Lesson newLesson = lessons.get(newLessonId);
        if (b == null || newLesson == null) return false;
        if (newLesson.availableSeats() <= 0) return false;
        // release seat on old lesson
        Lesson old = b.getLesson();
        old.removeBooking(b);
        b.setLesson(newLesson);
        b.setStatus(BookingStatus.CHANGED);
        newLesson.addBooking(b);
        return true;
    }

    public boolean cancelBooking(int bookingId) {
        Booking b = bookings.get(bookingId);
        if (b == null) return false;
        b.setStatus(BookingStatus.CANCELLED);
        b.getLesson().removeBooking(b);
        return true;
    }

    public boolean attendBooking(int bookingId, String review, int rating) {
        Booking b = bookings.get(bookingId);
        if (b == null) return false;
        b.setStatus(BookingStatus.ATTENDED);
        b.setReview(review);
        b.setRating(rating);
        return true;
    }

    public List<Lesson> getAllLessons() { return new ArrayList<>(lessons.values()); }

    public Map<LocalDate, List<Lesson>> monthlyLessonStats(int month) {
        // returns lessons grouped by date for the 4 weekends in that month
        return lessons.values().stream()
                .filter(l -> l.getDate().getMonthValue() == month)
                .collect(Collectors.groupingBy(Lesson::getDate, TreeMap::new, Collectors.toList()));
    }

    public Map<LessonType, Double> incomeByType(int month) {
        Map<LessonType, Double> map = new EnumMap<>(LessonType.class);
        for (Lesson l : lessons.values()) {
            if (l.getDate().getMonthValue() != month) continue;
            map.merge(l.getType(), l.totalIncome(), Double::sum);
        }
        return map;
    }

}

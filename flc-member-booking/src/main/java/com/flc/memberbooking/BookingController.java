package com.flc.memberbooking;

import com.flc.memberbooking.api.*;
import com.flc.memberbooking.api.requests.*;
import com.flc.memberbooking.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class BookingController {

    private final BookingSystem system;

    public BookingController(BookingSystem system) {
        this.system = system;
    }

    @GetMapping("/members")
    public List<MemberDto> members() {
        return system.getMembers().stream().map(m -> new MemberDto(m.getId(), m.getName())).collect(Collectors.toList());
    }

    @GetMapping("/lessons")
    public List<LessonDto> lessons() {
        return system.getAllLessons().stream().map(this::toLessonDto).collect(Collectors.toList());
    }

    @GetMapping("/lessons/day/{day}")
    public List<LessonDto> lessonsByDay(@PathVariable String day) {
        return system.viewByDay(day).stream().map(this::toLessonDto).collect(Collectors.toList());
    }

    @GetMapping("/lessons/exercise/{exercise}")
    public List<LessonDto> lessonsByExercise(@PathVariable String exercise) {
        return system.viewByExercise(exercise).stream().map(this::toLessonDto).collect(Collectors.toList());
    }

    @PostMapping("/bookings")
    public ResponseEntity<?> book(@RequestBody BookRequest req) {
        var maybe = system.bookLesson(req.getMemberId(), req.getLessonId());
        if (maybe.isPresent()) return ResponseEntity.status(HttpStatus.CREATED).body(toBookingDto(maybe.get()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Booking failed (invalid/duplicate/full)");
    }

    @GetMapping("/bookings/{id}")
    public ResponseEntity<?> getBooking(@PathVariable int id) {
        var b = system.findBooking(id);
        if (b.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found");
        }
        return ResponseEntity.ok(toBookingDto(b.get()));
    }

    @PutMapping("/bookings/{id}/change")
    public ResponseEntity<?> changeBooking(@PathVariable int id, @RequestBody ChangeRequest req) {
        boolean ok = system.changeBooking(id, req.getNewLessonId());
        if (ok) return ResponseEntity.ok(Map.of("changed", true));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Change failed (invalid or full)");
    }

    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<?> cancelBooking(@PathVariable int id) {
        boolean ok = system.cancelBooking(id);
        if (ok) return ResponseEntity.ok(Map.of("cancelled", true));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cancel failed");
    }

    @PostMapping("/bookings/{id}/attend")
    public ResponseEntity<?> attend(@PathVariable int id, @RequestBody AttendRequest req) {
        boolean ok = system.attendBooking(id, req.getReview(), req.getRating() == null ? 0 : req.getRating());
        if (ok) return ResponseEntity.ok(Map.of("attended", true));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Attend failed");
    }

    @GetMapping("/reports/month/{month}/lessons")
    public ResponseEntity<?> monthlyLessonReport(@PathVariable int month) {
        var map = system.monthlyLessonStats(month);
        var out = map.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().stream().map(this::toLessonDto).collect(Collectors.toList())));
        return ResponseEntity.ok(out);
    }

    @GetMapping("/reports/month/{month}/income")
    public ResponseEntity<?> monthlyIncomeReport(@PathVariable int month) {
        Map<LessonType, Double> map = system.incomeByType(month);
        return ResponseEntity.ok(map);
    }

    private LessonDto toLessonDto(Lesson l) {
        return new LessonDto(l.getId(), l.getType().getDisplayName(), l.getDate(), l.getTimeSlot(), l.availableSeats(), l.getType().getPrice().doubleValue());
    }

    private BookingDto toBookingDto(Booking b) {
        Member m = b.getMember();
        MemberDto md = new MemberDto(m.getId(), m.getName());
        return new BookingDto(b.getId(), md, b.getLesson().getId(), b.getStatus(), b.getReview().orElse(null), b.getRating().orElse(null));
    }
}

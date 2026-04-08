package com.flc.memberbooking.repository;

import com.flc.memberbooking.model.Booking;
import com.flc.memberbooking.model.BookingStatus;
import com.flc.memberbooking.model.Lesson;
import com.flc.memberbooking.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    long countByLessonAndStatusNot(Lesson lesson, BookingStatus status);
    boolean existsByMemberAndLessonAndStatusNot(Member member, Lesson lesson, BookingStatus status);
}

package com.flc.memberbooking.repository;

import com.flc.memberbooking.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepository extends JpaRepository<Lesson, String> {
}

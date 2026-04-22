package com.flc.memberbooking.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.flc.memberbooking.model.Lesson;
import com.flc.memberbooking.model.LessonType;
import com.flc.memberbooking.model.Member;
import com.flc.memberbooking.model.TimeSlot;
import com.flc.memberbooking.service.BookingSystem;

@ExtendWith(MockitoExtension.class)
public class BookingControllerTests {

    private MockMvc mvc;

    @Mock
    private BookingSystem system;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders.standaloneSetup(new BookingController(system)).build();
    }

    @Test
    public void testGetMembers() throws Exception {
        Member m = new Member("Alice");
        when(system.getMembers()).thenReturn(List.of(m));

        mvc.perform(get("/api/members").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alice"));
    }

    @Test
    public void testGetLessons() throws Exception {
        Lesson l = new Lesson("L1", LessonType.YOGA, LocalDate.of(2026,4,4), TimeSlot.MORNING);
        when(system.getAllLessons()).thenReturn(List.of(l));

        mvc.perform(get("/api/lessons").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("L1"))
                .andExpect(jsonPath("$[0].exercise").value(LessonType.YOGA.getDisplayName()));
    }

    @Test
    public void testGetLessonsByDay() throws Exception {
        Lesson l = new Lesson("L2", LessonType.YOGA, LocalDate.of(2026,4,4), TimeSlot.MORNING);
        when(system.viewByDay("saturday")).thenReturn(List.of(l));

        mvc.perform(get("/api/lessons/day/saturday").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("L2"));
    }

    @Test
    public void testPostBookingsBadRequestWhenInvalid() throws Exception {
        // if bookingFailureReason returns "invalid" controller should return 400
        when(system.bookingFailureReason(1, "Lx")).thenReturn("invalid");

        String body = "{\"memberId\":1,\"lessonId\":\"Lx\"}";
        mvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }
}

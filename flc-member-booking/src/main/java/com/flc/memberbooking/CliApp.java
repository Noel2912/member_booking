package com.flc.memberbooking;

import com.flc.memberbooking.model.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class CliApp {
    private final BookingSystem system = new BookingSystem();
    private final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        new CliApp().run();
    }

    private void run() {
        System.out.println("Welcome to FLC Member Booking CLI");
        boolean exit = false;
        while (!exit) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": bookLesson(); break;
                case "2": changeOrCancel(); break;
                case "3": attendLesson(); break;
                case "4": monthlyLessonReport(); break;
                case "5": monthlyChampionReport(); break;
                case "6": showMembers(); break;
                case "0": exit = true; break;
                default: System.out.println("Invalid choice");
            }
        }
        System.out.println("Goodbye");
    }

    private void printMenu() {
        System.out.println("\nMain Menu:\n1. Book a lesson\n2. Change/Cancel a booking\n3. Attend a lesson (write review)\n4. Monthly lesson report\n5. Monthly champion report\n6. List members\n0. Exit\nEnter choice:");
    }

    private void showMembers() {
        System.out.println("Members:");
        for (Member m : system.getMembers()) System.out.println(m.getId()+": "+m.getName());
    }

    private void bookLesson() {
        System.out.println("Booking: Enter member id:");
        int id = Integer.parseInt(scanner.nextLine().trim());
        System.out.println("View timetable by: 1) day 2) exercise");
        String opt = scanner.nextLine().trim();
        List<Lesson> list;
        if (opt.equals("1")) {
            System.out.println("Enter day (Saturday or Sunday):");
            String day = scanner.nextLine().trim();
            list = system.viewByDay(day);
        } else {
            System.out.println("Enter exercise name (e.g., Yoga):");
            String ex = scanner.nextLine().trim();
            list = system.viewByExercise(ex);
        }
        DateTimeFormatter df = DateTimeFormatter.ISO_DATE;
        System.out.println("Available lessons:");
        for (Lesson l : list) System.out.println(l.getId()+" - "+l.getDisplayName()+" - seats:"+l.availableSeats()+" - price:"+l.getType().getPrice());
        System.out.println("Enter lesson id to book:");
        String lid = scanner.nextLine().trim();
        var maybe = system.bookLesson(id, lid);
        if (maybe.isPresent()) System.out.println("Booked: " + maybe.get().getId());
        else System.out.println("Booking failed (duplicate or full or invalid)");
    }

    private void changeOrCancel() {
        System.out.println("Enter booking id:");
        int bid = Integer.parseInt(scanner.nextLine().trim());
        var b = system.findBooking(bid);
        if (b.isEmpty()) { System.out.println("Booking not found"); return; }
        System.out.println("1) Change 2) Cancel");
        String c = scanner.nextLine().trim();
        if (c.equals("1")) {
            System.out.println("Enter new lesson id:");
            String nid = scanner.nextLine().trim();
            boolean ok = system.changeBooking(bid, nid);
            System.out.println(ok?"Changed":"Change failed (full or invalid)");
        } else {
            boolean ok = system.cancelBooking(bid);
            System.out.println(ok?"Cancelled":"Cancel failed");
        }
    }

    private void attendLesson() {
        System.out.println("Enter booking id to attend:");
        int bid = Integer.parseInt(scanner.nextLine().trim());
        System.out.println("Enter review text:");
        String review = scanner.nextLine();
        System.out.println("Enter rating 1-5:");
        int r = Integer.parseInt(scanner.nextLine().trim());
        boolean ok = system.attendBooking(bid, review, r);
        System.out.println(ok?"Marked as attended":"Attend failed");
    }

    private void monthlyLessonReport() {
        System.out.println("Enter month number (e.g., 4 for April):");
        int m = Integer.parseInt(scanner.nextLine().trim());
        Map<java.time.LocalDate, List<Lesson>> map = system.monthlyLessonStats(m);
        System.out.println("Monthly lesson stats for month: "+m);
        for (var entry : map.entrySet()) {
            System.out.println("Date: " + entry.getKey());
            for (Lesson l : entry.getValue()) {
                long attended = l.getBookings().stream().filter(b -> b.getStatus() == BookingStatus.ATTENDED).count();
                double avg = l.averageRating().orElse(Double.NaN);
                System.out.println("  " + l.getId() + " - " + l.getType().getDisplayName() + " - attended=" + attended + " - avgRating=" + (Double.isNaN(avg)?"N/A":String.format("%.2f",avg)));
            }
        }
    }

    private void monthlyChampionReport() {
        System.out.println("Enter month number (e.g., 4 for April):");
        int m = Integer.parseInt(scanner.nextLine().trim());
        var map = system.incomeByType(m);
        System.out.println("Income by lesson type for month " + m + ":");
        map.entrySet().stream().sorted((a,b)->Double.compare(b.getValue(), a.getValue())).forEach(e -> System.out.println(e.getKey()+" = "+e.getValue()));
    }
}

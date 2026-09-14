package com.nazlicanguner.campuspuzzle.model;

import java.util.List;

public class ProblemData {

    private final List<ClassInfo> classes;
    private final List<Room> rooms;
    private final List<StudentGroup> studentGroups;
    private final List<TimeSlot> timeSlots;

    public ProblemData(
            List<ClassInfo> classes,
            List<Room> rooms,
            List<StudentGroup> studentGroups,
            List<TimeSlot> timeSlots
    ) {
        this.classes = List.copyOf(classes);
        this.rooms = List.copyOf(rooms);
        this.studentGroups = List.copyOf(studentGroups);
        this.timeSlots = List.copyOf(timeSlots);
    }

    public List<ClassInfo> getClasses() {
        return classes;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public List<StudentGroup> getStudentGroups() {
        return studentGroups;
    }

    public List<TimeSlot> getTimeSlots() {
        return timeSlots;
    }
}
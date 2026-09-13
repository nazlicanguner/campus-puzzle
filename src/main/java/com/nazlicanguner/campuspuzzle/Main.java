package com.nazlicanguner.campuspuzzle;

import com.nazlicanguner.campuspuzzle.model.ClassInfo;
import com.nazlicanguner.campuspuzzle.model.Room;
import com.nazlicanguner.campuspuzzle.model.ScheduleEntry;
import com.nazlicanguner.campuspuzzle.model.StudentGroup;
import com.nazlicanguner.campuspuzzle.model.TimeSlot;

import java.util.Set;

public class Main {

    public static void main(String[] args) {
        ClassInfo classInfo = new ClassInfo("CS101", 60, "P01");
        Room room = new Room("R101", 80);
        TimeSlot timeSlot = new TimeSlot(
                "MON_09", "Monday 09:00-10:00"
        );
        StudentGroup group = new StudentGroup(
                "G1", Set.of(classInfo.getId())
        );

        ScheduleEntry entry = new ScheduleEntry(
                classInfo, room, timeSlot
        );

        System.out.println("Campus Puzzle");
        System.out.println("Class: " + entry.getClassInfo().getId());
        System.out.println("Professor: " + classInfo.getProfessorId());
        System.out.println("Group: " + group.getId());
        System.out.println("Room: " + entry.getRoom().getId());
        System.out.println("Time: " + entry.getTimeSlot().getLabel());
        System.out.println("Wasted seats: " + entry.getWastedSeats());
    }
}
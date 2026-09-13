package com.nazlicanguner.campuspuzzle.model;

import java.util.Objects;

public class ScheduleEntry {

    private final ClassInfo classInfo;
    private final Room room;
    private final TimeSlot timeSlot;

    public ScheduleEntry(ClassInfo classInfo, Room room, TimeSlot timeSlot) {
        this.classInfo = Objects.requireNonNull(
                classInfo, "Class must not be null."
        );
        this.room = Objects.requireNonNull(
                room, "Room must not be null."
        );
        this.timeSlot = Objects.requireNonNull(
                timeSlot, "Time slot must not be null."
        );

        if (classInfo.getEnrollment() > room.getCapacity()) {
            throw new IllegalArgumentException(
                    "Room capacity is insufficient for this class."
            );
        }
    }

    public ClassInfo getClassInfo() {
        return classInfo;
    }

    public Room getRoom() {
        return room;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public int getWastedSeats() {
        return room.getCapacity() - classInfo.getEnrollment();
    }
}
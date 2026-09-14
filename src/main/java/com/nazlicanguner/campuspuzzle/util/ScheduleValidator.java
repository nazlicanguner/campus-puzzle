package com.nazlicanguner.campuspuzzle.util;

import com.nazlicanguner.campuspuzzle.model.ClassInfo;
import com.nazlicanguner.campuspuzzle.model.ProblemData;
import com.nazlicanguner.campuspuzzle.model.Room;
import com.nazlicanguner.campuspuzzle.model.ScheduleEntry;
import com.nazlicanguner.campuspuzzle.model.ScheduleResult;
import com.nazlicanguner.campuspuzzle.model.StudentGroup;
import com.nazlicanguner.campuspuzzle.model.TimeSlot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ScheduleValidator {

    public List<String> validate(ProblemData data, ScheduleResult result) {
        List<String> errors = new ArrayList<>();

        Map<String, ClassInfo> classesById = new HashMap<>();
        Map<String, Room> roomsById = new HashMap<>();
        Map<String, TimeSlot> slotsById = new HashMap<>();

        for (ClassInfo classInfo : data.getClasses()) {
            classesById.put(classInfo.getId(), classInfo);
        }

        for (Room room : data.getRooms()) {
            roomsById.put(room.getId(), room);
        }

        for (TimeSlot slot : data.getTimeSlots()) {
            slotsById.put(slot.getId(), slot);
        }

        Set<String> accountedFor = new HashSet<>();

        for (ScheduleEntry entry : result.getEntries()) {
            String classId = entry.getClassInfo().getId();
            String roomId = entry.getRoom().getId();
            String slotId = entry.getTimeSlot().getId();

            if (!accountedFor.add(classId)) {
                errors.add("Class scheduled more than once: " + classId);
            }

            ClassInfo originalClass = classesById.get(classId);
            Room originalRoom = roomsById.get(roomId);
            TimeSlot originalSlot = slotsById.get(slotId);

            if (originalClass == null) {
                errors.add("Unknown scheduled class: " + classId);
            } else {
                if (entry.getClassInfo().getEnrollment()
                        != originalClass.getEnrollment()
                        || !entry.getClassInfo().getProfessorId()
                        .equals(originalClass.getProfessorId())) {
                    errors.add("Class data differs from input: " + classId);
                }
            }

            if (originalRoom == null) {
                errors.add("Unknown room: " + roomId);
            } else if (entry.getRoom().getCapacity()
                    != originalRoom.getCapacity()) {
                errors.add("Room capacity differs from input: " + roomId);
            }

            if (originalSlot == null) {
                errors.add("Unknown time slot: " + slotId);
            } else if (!entry.getTimeSlot().getLabel()
                    .equals(originalSlot.getLabel())) {
                errors.add("Time slot label differs from input: " + slotId);
            }

            if (originalClass != null && originalRoom != null
                    && originalClass.getEnrollment()
                    > originalRoom.getCapacity()) {
                errors.add(
                        "Insufficient capacity: " + classId + " in " + roomId
                );
            }
        }

        for (Map.Entry<String, String> item
                : result.getUnscheduledReasons().entrySet()) {
            String classId = item.getKey();

            if (!classesById.containsKey(classId)) {
                errors.add("Unknown unscheduled class: " + classId);
            }

            if (!accountedFor.add(classId)) {
                errors.add(
                        "Class is both scheduled and unscheduled: " + classId
                );
            }

            if (item.getValue().isBlank()) {
                errors.add("Missing unscheduled reason: " + classId);
            }
        }

        for (String classId : classesById.keySet()) {
            if (!accountedFor.contains(classId)) {
                errors.add("Class missing from result: " + classId);
            }
        }

        List<ScheduleEntry> entries = result.getEntries();

        for (int i = 0; i < entries.size(); i++) {
            for (int j = i + 1; j < entries.size(); j++) {
                ScheduleEntry first = entries.get(i);
                ScheduleEntry second = entries.get(j);

                if (!first.getTimeSlot().getId()
                        .equals(second.getTimeSlot().getId())) {
                    continue;
                }

                String firstId = first.getClassInfo().getId();
                String secondId = second.getClassInfo().getId();
                String pair = firstId + " and " + secondId
                        + " at " + first.getTimeSlot().getId();

                if (first.getRoom().getId().equals(second.getRoom().getId())) {
                    errors.add("Room double-booking: " + pair);
                }

                ClassInfo firstClass = classesById.get(firstId);
                ClassInfo secondClass = classesById.get(secondId);

                if (firstClass == null || secondClass == null) {
                    continue;
                }

                if (firstClass.getProfessorId()
                        .equals(secondClass.getProfessorId())) {
                    errors.add("Professor conflict: " + pair);
                }

                for (StudentGroup group : data.getStudentGroups()) {
                    if (group.getClassIds().contains(firstId)
                            && group.getClassIds().contains(secondId)) {
                        errors.add(
                                "Student group conflict (" + group.getId()
                                        + "): " + pair
                        );
                    }
                }
            }
        }

        return List.copyOf(errors);
    }

    public void validateOrThrow(ProblemData data, ScheduleResult result) {
        List<String> errors = validate(data, result);

        if (!errors.isEmpty()) {
            throw new IllegalStateException(
                    "Invalid schedule:\n" + String.join("\n", errors)
            );
        }
    }
}
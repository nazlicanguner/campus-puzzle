package com.nazlicanguner.campuspuzzle.greedy;

import com.nazlicanguner.campuspuzzle.model.ClassInfo;
import com.nazlicanguner.campuspuzzle.model.ProblemData;
import com.nazlicanguner.campuspuzzle.model.Room;
import com.nazlicanguner.campuspuzzle.model.ScheduleEntry;
import com.nazlicanguner.campuspuzzle.model.ScheduleResult;
import com.nazlicanguner.campuspuzzle.model.StudentGroup;
import com.nazlicanguner.campuspuzzle.model.TimeSlot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GreedyScheduler {

    public ScheduleResult schedule(ProblemData data) {
        List<ClassInfo> sortedClasses = new ArrayList<>(data.getClasses());

        sortedClasses.sort(
                Comparator.comparingInt(ClassInfo::getEnrollment)
                        .reversed()
                        .thenComparing(ClassInfo::getId)
        );

        List<ScheduleEntry> entries = new ArrayList<>();
        Map<String, String> unscheduledReasons = new LinkedHashMap<>();

        for (ClassInfo classInfo : sortedClasses) {
            ScheduleEntry assignment = findAssignment(
                    classInfo, data, entries
            );

            if (assignment != null) {
                entries.add(assignment);
            } else {
                unscheduledReasons.put(
                        classInfo.getId(),
                        explainUnscheduled(classInfo, data)
                );
            }
        }

        return new ScheduleResult(entries, unscheduledReasons);
    }

    private ScheduleEntry findAssignment(
            ClassInfo classInfo,
            ProblemData data,
            List<ScheduleEntry> entries
    ) {
        for (TimeSlot timeSlot : data.getTimeSlots()) {
            for (Room room : data.getRooms()) {
                if (canAssign(
                        classInfo, room, timeSlot,
                        entries, data.getStudentGroups()
                )) {
                    return new ScheduleEntry(classInfo, room, timeSlot);
                }
            }
        }

        return null;
    }

    private boolean canAssign(
            ClassInfo classInfo,
            Room room,
            TimeSlot timeSlot,
            List<ScheduleEntry> entries,
            List<StudentGroup> studentGroups
    ) {
        if (classInfo.getEnrollment() > room.getCapacity()) {
            return false;
        }

        for (ScheduleEntry entry : entries) {
            if (!entry.getTimeSlot().getId().equals(timeSlot.getId())) {
                continue;
            }

            if (entry.getRoom().getId().equals(room.getId())) {
                return false;
            }

            ClassInfo otherClass = entry.getClassInfo();

            if (otherClass.getProfessorId().equals(classInfo.getProfessorId())) {
                return false;
            }

            for (StudentGroup group : studentGroups) {
                if (group.getClassIds().contains(classInfo.getId())
                        && group.getClassIds().contains(otherClass.getId())) {
                    return false;
                }
            }
        }

        return true;
    }

    private String explainUnscheduled(
            ClassInfo classInfo,
            ProblemData data
    ) {
        if (data.getTimeSlots().isEmpty()) {
            return "No time slots are available.";
        }

        boolean hasSuitableRoom = data.getRooms().stream()
                .anyMatch(room ->
                        room.getCapacity() >= classInfo.getEnrollment()
                );

        if (!hasSuitableRoom) {
            return "No room has sufficient capacity.";
        }

        return "No feasible room and time slot remain "
                + "under the current greedy assignments.";
    }
}
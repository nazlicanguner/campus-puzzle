package com.nazlicanguner.campuspuzzle.dp;

import com.nazlicanguner.campuspuzzle.model.ClassInfo;
import com.nazlicanguner.campuspuzzle.model.Room;
import com.nazlicanguner.campuspuzzle.model.ScheduleEntry;
import com.nazlicanguner.campuspuzzle.model.ScheduleResult;
import com.nazlicanguner.campuspuzzle.model.TimeSlot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RoomOptimizer {

    private static final byte SKIP_CLASS = 1;
    private static final byte SKIP_ROOM = 2;
    private static final byte ASSIGN = 3;

    public ScheduleResult optimizeSlot(
            List<ClassInfo> classes,
            List<Room> rooms,
            TimeSlot timeSlot
    ) {
        List<ClassInfo> sortedClasses = new ArrayList<>(classes);
        List<Room> sortedRooms = new ArrayList<>(rooms);

        sortedClasses.sort(
                Comparator.comparingInt(ClassInfo::getEnrollment)
                        .thenComparing(ClassInfo::getId)
        );

        sortedRooms.sort(
                Comparator.comparingInt(Room::getCapacity)
                        .thenComparing(Room::getId)
        );

        int classCount = sortedClasses.size();
        int roomCount = sortedRooms.size();

        int[][] scheduled = new int[classCount + 1][roomCount + 1];
        long[][] waste = new long[classCount + 1][roomCount + 1];
        byte[][] decision = new byte[classCount + 1][roomCount + 1];

        for (int i = 1; i <= classCount; i++) {
            decision[i][0] = SKIP_CLASS;
        }

        for (int j = 1; j <= roomCount; j++) {
            decision[0][j] = SKIP_ROOM;
        }

        for (int i = 1; i <= classCount; i++) {
            ClassInfo classInfo = sortedClasses.get(i - 1);

            for (int j = 1; j <= roomCount; j++) {
                Room room = sortedRooms.get(j - 1);

                // Option 1: leave this class unassigned.
                scheduled[i][j] = scheduled[i - 1][j];
                waste[i][j] = waste[i - 1][j];
                decision[i][j] = SKIP_CLASS;

                // Option 2: leave this room unused.
                if (isBetter(
                        scheduled[i][j - 1], waste[i][j - 1],
                        scheduled[i][j], waste[i][j]
                )) {
                    scheduled[i][j] = scheduled[i][j - 1];
                    waste[i][j] = waste[i][j - 1];
                    decision[i][j] = SKIP_ROOM;
                }

                // Option 3: assign this class to this room.
                if (classInfo.getEnrollment() <= room.getCapacity()) {
                    int candidateCount = scheduled[i - 1][j - 1] + 1;
                    long candidateWaste = waste[i - 1][j - 1]
                            + room.getCapacity()
                            - classInfo.getEnrollment();

                    if (isBetter(
                            candidateCount, candidateWaste,
                            scheduled[i][j], waste[i][j]
                    )) {
                        scheduled[i][j] = candidateCount;
                        waste[i][j] = candidateWaste;
                        decision[i][j] = ASSIGN;
                    }
                }
            }
        }

        List<ScheduleEntry> entries = new ArrayList<>();
        Set<String> assignedIds = new HashSet<>();

        int i = classCount;
        int j = roomCount;

        while (i > 0) {
            byte action = decision[i][j];

            if (action == ASSIGN) {
                ClassInfo classInfo = sortedClasses.get(i - 1);
                Room room = sortedRooms.get(j - 1);

                entries.add(new ScheduleEntry(classInfo, room, timeSlot));
                assignedIds.add(classInfo.getId());

                i--;
                j--;
            } else if (action == SKIP_ROOM) {
                j--;
            } else {
                i--;
            }
        }

        entries.sort(
                Comparator.comparing(entry -> entry.getClassInfo().getId())
        );

        Map<String, String> unscheduledReasons = new LinkedHashMap<>();

        for (ClassInfo classInfo : sortedClasses) {
            if (!assignedIds.contains(classInfo.getId())) {
                boolean hasSuitableRoom = sortedRooms.stream()
                        .anyMatch(room ->
                                room.getCapacity() >= classInfo.getEnrollment()
                        );

                String reason = hasSuitableRoom
                        ? "Not selected in the optimal room allocation "
                          + "for fixed time slot " + timeSlot.getId() + "."
                        : "No room has sufficient capacity.";

                unscheduledReasons.put(classInfo.getId(), reason);
            }
        }

        return new ScheduleResult(entries, unscheduledReasons);
    }

    private boolean isBetter(
            int candidateCount,
            long candidateWaste,
            int currentCount,
            long currentWaste
    ) {
        return candidateCount > currentCount
                || (candidateCount == currentCount
                && candidateWaste < currentWaste);
    }
}
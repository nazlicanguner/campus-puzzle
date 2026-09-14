package com.nazlicanguner.campuspuzzle.backtracking;

import com.nazlicanguner.campuspuzzle.graph.ConflictGraph;
import com.nazlicanguner.campuspuzzle.greedy.GreedyScheduler;
import com.nazlicanguner.campuspuzzle.model.BacktrackingResult;
import com.nazlicanguner.campuspuzzle.model.ClassInfo;
import com.nazlicanguner.campuspuzzle.model.ProblemData;
import com.nazlicanguner.campuspuzzle.model.Room;
import com.nazlicanguner.campuspuzzle.model.ScheduleEntry;
import com.nazlicanguner.campuspuzzle.model.ScheduleResult;
import com.nazlicanguner.campuspuzzle.model.TimeSlot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class BacktrackingScheduler {

    public BacktrackingResult schedule(ProblemData data, long maxNodes) {
        Objects.requireNonNull(data, "Problem data must not be null.");

        if (maxNodes <= 0) {
            throw new IllegalArgumentException(
                    "Search node limit must be positive."
            );
        }

        return new Search(data, maxNodes).run();
    }

    private static class Search {

        private final ProblemData data;
        private final long maxNodes;
        private final ConflictGraph graph;
        private final List<ClassInfo> orderedClasses;
        private final List<Room> orderedRooms;
        private final Map<String, String> impossibleReasons;

        private List<ScheduleEntry> bestEntries;
        private long bestWaste;
        private long visitedNodes;
        private boolean limitReached;

        private Search(ProblemData data, long maxNodes) {
            this.data = data;
            this.maxNodes = maxNodes;
            this.graph = new ConflictGraph(data);
            this.orderedClasses = new ArrayList<>();
            this.orderedRooms = new ArrayList<>(data.getRooms());
            this.impossibleReasons = new LinkedHashMap<>();

            orderedRooms.sort(
                    Comparator.comparingInt(Room::getCapacity)
                            .thenComparing(Room::getId)
            );

            for (ClassInfo classInfo : data.getClasses()) {
                boolean hasSuitableRoom = orderedRooms.stream()
                        .anyMatch(room ->
                                room.getCapacity() >= classInfo.getEnrollment()
                        );

                if (data.getTimeSlots().isEmpty()) {
                    impossibleReasons.put(
                            classInfo.getId(),
                            "No time slots are available."
                    );
                } else if (!hasSuitableRoom) {
                    impossibleReasons.put(
                            classInfo.getId(),
                            "No room has sufficient capacity."
                    );
                } else {
                    orderedClasses.add(classInfo);
                }
            }

            orderedClasses.sort(
                    Comparator.<ClassInfo>comparingInt(
                                    classInfo -> graph.getDegree(classInfo.getId())
                            ).reversed()
                            .thenComparing(
                                    Comparator.comparingInt(
                                            ClassInfo::getEnrollment
                                    ).reversed()
                            )
                            .thenComparing(ClassInfo::getId)
            );

            ScheduleResult baseline = new GreedyScheduler().schedule(data);
            bestEntries = new ArrayList<>(baseline.getEntries());
            bestWaste = baseline.getTotalWastedSeats();
        }

        private BacktrackingResult run() {
            search(0, new ArrayList<>(), 0L);

            Set<String> scheduledIds = bestEntries.stream()
                    .map(entry -> entry.getClassInfo().getId())
                    .collect(Collectors.toSet());

            Map<String, String> reasons = new LinkedHashMap<>();

            for (ClassInfo classInfo : data.getClasses()) {
                if (scheduledIds.contains(classInfo.getId())) {
                    continue;
                }

                String reason = impossibleReasons.get(classInfo.getId());

                if (reason == null) {
                    reason = limitReached
                            ? "Unscheduled in the best solution found before "
                              + "the search limit; feasibility is not proven."
                            : "Unscheduled in an optimal partial schedule; "
                              + "the constraints prevent scheduling all classes.";
                }

                reasons.put(classInfo.getId(), reason);
            }

            ScheduleResult result = new ScheduleResult(bestEntries, reasons);

            return new BacktrackingResult(
                    result, !limitReached, visitedNodes
            );
        }

        private void search(
                int index,
                List<ScheduleEntry> currentEntries,
                long currentWaste
        ) {
            if (visitedNodes >= maxNodes) {
                limitReached = true;
                return;
            }

            visitedNodes++;

            if (isBetter(currentEntries.size(), currentWaste)) {
                bestEntries = new ArrayList<>(currentEntries);
                bestWaste = currentWaste;
            }

            if (index == orderedClasses.size()) {
                return;
            }

            int remainingClasses = orderedClasses.size() - index;
            int maximumPossibleCount =
                    currentEntries.size() + remainingClasses;

            // Even assigning every remaining class cannot beat the best count.
            if (maximumPossibleCount < bestEntries.size()) {
                return;
            }

            // Only a tie in count is possible, and waste cannot decrease.
            if (maximumPossibleCount == bestEntries.size()
                    && currentWaste >= bestWaste) {
                return;
            }

            ClassInfo classInfo = orderedClasses.get(index);

            for (TimeSlot timeSlot : data.getTimeSlots()) {
                for (Room room : orderedRooms) {
                    if (!canAssign(
                            classInfo, room, timeSlot, currentEntries
                    )) {
                        continue;
                    }

                    ScheduleEntry assignment = new ScheduleEntry(
                            classInfo, room, timeSlot
                    );

                    currentEntries.add(assignment);

                    search(
                            index + 1,
                            currentEntries,
                            currentWaste + assignment.getWastedSeats()
                    );

                    // Undo the assignment before trying another option.
                    currentEntries.remove(currentEntries.size() - 1);

                    if (limitReached) {
                        return;
                    }
                }
            }

            // Also explore leaving this class unscheduled.
            search(index + 1, currentEntries, currentWaste);
        }

        private boolean canAssign(
                ClassInfo classInfo,
                Room room,
                TimeSlot timeSlot,
                List<ScheduleEntry> currentEntries
        ) {
            if (classInfo.getEnrollment() > room.getCapacity()) {
                return false;
            }

            for (ScheduleEntry entry : currentEntries) {
                if (!entry.getTimeSlot().getId().equals(timeSlot.getId())) {
                    continue;
                }

                if (entry.getRoom().getId().equals(room.getId())) {
                    return false;
                }

                if (graph.hasConflict(
                        classInfo.getId(), entry.getClassInfo().getId()
                )) {
                    return false;
                }
            }

            return true;
        }

        private boolean isBetter(int scheduledCount, long waste) {
            return scheduledCount > bestEntries.size()
                    || (scheduledCount == bestEntries.size()
                    && waste < bestWaste);
        }
    }
}
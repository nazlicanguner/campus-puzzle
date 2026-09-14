package com.nazlicanguner.campuspuzzle;

import com.nazlicanguner.campuspuzzle.dp.RoomOptimizer;
import com.nazlicanguner.campuspuzzle.graph.ConflictGraph;
import com.nazlicanguner.campuspuzzle.graph.WelshPowellColoring;
import com.nazlicanguner.campuspuzzle.greedy.GreedyScheduler;
import com.nazlicanguner.campuspuzzle.model.ClassInfo;
import com.nazlicanguner.campuspuzzle.model.ProblemData;
import com.nazlicanguner.campuspuzzle.model.ScheduleEntry;
import com.nazlicanguner.campuspuzzle.model.ScheduleResult;
import com.nazlicanguner.campuspuzzle.util.JsonLoader;
import com.nazlicanguner.campuspuzzle.backtracking.BacktrackingScheduler;
import com.nazlicanguner.campuspuzzle.model.BacktrackingResult;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws IOException {
        JsonLoader loader = new JsonLoader();
        Path inputPath = Path.of(
                args.length > 0 ? args[0] : "data/constraints.json"
        );

        ProblemData data = loader.load(inputPath);

        System.out.println("Input: " + inputPath);
        System.out.println();

        ConflictGraph graph = new ConflictGraph(data);

        System.out.println("Conflict Graph");

        for (String classId : graph.getClassIds()) {
            System.out.printf(
                    "%s | Conflicts: %s | Degree: %d%n",
                    classId,
                    graph.getNeighbors(classId),
                    graph.getDegree(classId)
            );
        }

        System.out.println("Edges: " + graph.getEdgeCount());
        System.out.println();

        WelshPowellColoring coloring = new WelshPowellColoring();
        Map<String, Integer> colors = coloring.color(graph);

        System.out.println("Welsh-Powell Coloring");

        for (String classId : graph.getClassIds()) {
            System.out.printf(
                    "%s | Color: %d%n",
                    classId,
                    colors.get(classId)
            );
        }

        long colorCount = colors.values().stream().distinct().count();
        System.out.println("Colors used: " + colorCount);
        System.out.println();

        GreedyScheduler greedyScheduler = new GreedyScheduler();
        ScheduleResult greedyResult = greedyScheduler.schedule(data);

        printSchedule("Greedy Baseline", greedyResult);

        ScheduleResult optimizedResult = optimizeColoredSchedule(
                data, colors
        );

        printSchedule("Welsh-Powell + DP", optimizedResult);

        BacktrackingScheduler backtrackingScheduler = new BacktrackingScheduler();

        BacktrackingResult backtrackingResult = backtrackingScheduler.schedule(
                data, 200_000L
        );

        printSchedule(
                "Backtracking",
                backtrackingResult.getScheduleResult()
        );

        System.out.println(
                "Search complete: " + backtrackingResult.isSearchComplete()
        );
        System.out.println(
                "Visited nodes: " + backtrackingResult.getVisitedNodes()
        );

        if (backtrackingResult.isSearchComplete()) {
            System.out.println(
                    "Optimality: proven for scheduled count, then wasted seats."
            );
        } else {
            System.out.println(
                    "Search limit reached. Best solution found; optimality not proven."
            );
        }
    }

    private static ScheduleResult optimizeColoredSchedule(
            ProblemData data,
            Map<String, Integer> colors
    ) {
        Map<Integer, List<ClassInfo>> classesBySlot = new LinkedHashMap<>();
        List<ScheduleEntry> entries = new ArrayList<>();
        Map<String, String> unscheduledReasons = new LinkedHashMap<>();

        for (ClassInfo classInfo : data.getClasses()) {
            Integer color = colors.get(classInfo.getId());

            if (color == null || color < 0) {
                throw new IllegalArgumentException(
                        "Missing or invalid color for class "
                                + classInfo.getId()
                );
            }

            if (color >= data.getTimeSlots().size()) {
                unscheduledReasons.put(
                        classInfo.getId(),
                        "Assigned color has no available time slot. "
                                + "Recoloring or additional slots may be needed."
                );
                continue;
            }

            classesBySlot.computeIfAbsent(
                    color, key -> new ArrayList<>()
            ).add(classInfo);
        }

        RoomOptimizer optimizer = new RoomOptimizer();

        for (int slotIndex = 0;
             slotIndex < data.getTimeSlots().size();
             slotIndex++) {

            List<ClassInfo> slotClasses = classesBySlot.getOrDefault(
                    slotIndex, List.of()
            );

            ScheduleResult slotResult = optimizer.optimizeSlot(
                    slotClasses,
                    data.getRooms(),
                    data.getTimeSlots().get(slotIndex)
            );

            entries.addAll(slotResult.getEntries());
            unscheduledReasons.putAll(slotResult.getUnscheduledReasons());
        }

        return new ScheduleResult(entries, unscheduledReasons);
    }

    private static void printSchedule(
            String title,
            ScheduleResult result
    ) {
        System.out.println("Campus Puzzle - " + title);
        System.out.println();

        for (ScheduleEntry entry : result.getEntries()) {
            String capacityInfo = entry.getWastedSeats() == 0
                    ? "Perfect Fit"
                    : "Wasted " + entry.getWastedSeats() + " seats";

            System.out.printf(
                    "Scheduled | %s | %s | %s | %s%n",
                    entry.getClassInfo().getId(),
                    entry.getTimeSlot().getLabel(),
                    entry.getRoom().getId(),
                    capacityInfo
            );
        }

        result.getUnscheduledReasons().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> System.out.printf(
                        "Unscheduled | %s | N/A | N/A | %s%n",
                        entry.getKey(),
                        entry.getValue()
                ));

        System.out.println();
        System.out.println("Scheduled: " + result.getScheduledCount());
        System.out.println("Unscheduled: " + result.getUnscheduledCount());
        System.out.println(
                "Total wasted seats: " + result.getTotalWastedSeats()
        );
        System.out.println();
    }
}
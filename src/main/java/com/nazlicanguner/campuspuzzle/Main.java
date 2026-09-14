package com.nazlicanguner.campuspuzzle;

import com.nazlicanguner.campuspuzzle.greedy.GreedyScheduler;
import com.nazlicanguner.campuspuzzle.model.ProblemData;
import com.nazlicanguner.campuspuzzle.model.ScheduleEntry;
import com.nazlicanguner.campuspuzzle.model.ScheduleResult;
import com.nazlicanguner.campuspuzzle.util.JsonLoader;

import java.io.IOException;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws IOException {
        JsonLoader loader = new JsonLoader();
        ProblemData data = loader.load(
                Path.of("data", "constraints.json")
        );

        GreedyScheduler scheduler = new GreedyScheduler();
        ScheduleResult result = scheduler.schedule(data);

        System.out.println("Campus Puzzle - Greedy Baseline");
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
                .sorted(java.util.Map.Entry.comparingByKey())
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
    }
}
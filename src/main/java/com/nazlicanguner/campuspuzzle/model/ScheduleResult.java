package com.nazlicanguner.campuspuzzle.model;

import java.util.List;
import java.util.Map;

public class ScheduleResult {

    private final List<ScheduleEntry> entries;
    private final Map<String, String> unscheduledReasons;

    public ScheduleResult(
            List<ScheduleEntry> entries,
            Map<String, String> unscheduledReasons
    ) {
        this.entries = List.copyOf(entries);
        this.unscheduledReasons = Map.copyOf(unscheduledReasons);
    }

    public List<ScheduleEntry> getEntries() {
        return entries;
    }

    public Map<String, String> getUnscheduledReasons() {
        return unscheduledReasons;
    }

    public int getScheduledCount() {
        return entries.size();
    }

    public int getUnscheduledCount() {
        return unscheduledReasons.size();
    }

    public long getTotalWastedSeats() {
        long total = 0;

        for (ScheduleEntry entry : entries) {
            total += entry.getWastedSeats();
        }

        return total;
    }
}
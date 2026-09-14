package com.nazlicanguner.campuspuzzle.model;

import java.util.Objects;

public class BacktrackingResult {

    private final ScheduleResult scheduleResult;
    private final boolean searchComplete;
    private final long visitedNodes;

    public BacktrackingResult(
            ScheduleResult scheduleResult,
            boolean searchComplete,
            long visitedNodes
    ) {
        this.scheduleResult = Objects.requireNonNull(
                scheduleResult, "Schedule result must not be null."
        );

        if (visitedNodes < 0) {
            throw new IllegalArgumentException(
                    "Visited node count must not be negative."
            );
        }

        this.searchComplete = searchComplete;
        this.visitedNodes = visitedNodes;
    }

    public ScheduleResult getScheduleResult() {
        return scheduleResult;
    }

    public boolean isSearchComplete() {
        return searchComplete;
    }

    public long getVisitedNodes() {
        return visitedNodes;
    }
}
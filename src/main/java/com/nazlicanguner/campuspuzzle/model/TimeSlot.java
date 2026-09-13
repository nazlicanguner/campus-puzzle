package com.nazlicanguner.campuspuzzle.model;

public class TimeSlot {

    private final String id;
    private final String label;

    public TimeSlot(String id, String label) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Time slot ID must not be blank."
            );
        }
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException(
                    "Time slot label must not be blank."
            );
        }

        this.id = id;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }
}
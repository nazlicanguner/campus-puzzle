package com.nazlicanguner.campuspuzzle.model;

public class Room {

    private final String id;
    private final int capacity;

    public Room(String id, int capacity) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Room ID must not be blank.");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Room capacity must be positive.");
        }

        this.id = id;
        this.capacity = capacity;
    }

    public String getId() {
        return id;
    }

    public int getCapacity() {
        return capacity;
    }
}
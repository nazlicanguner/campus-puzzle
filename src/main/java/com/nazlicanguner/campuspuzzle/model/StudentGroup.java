package com.nazlicanguner.campuspuzzle.model;

import java.util.Set;

public class StudentGroup {

    private final String id;
    private final Set<String> classIds;

    public StudentGroup(String id, Set<String> classIds) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Student group ID must not be blank."
            );
        }
        if (classIds == null || classIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "Student group must attend at least one class."
            );
        }
        for (String classId : classIds) {
            if (classId == null || classId.isBlank()) {
                throw new IllegalArgumentException(
                        "Class IDs must not be blank."
                );
            }
        }

        this.id = id;
        this.classIds = Set.copyOf(classIds);
    }

    public String getId() {
        return id;
    }

    public Set<String> getClassIds() {
        return classIds;
    }
}
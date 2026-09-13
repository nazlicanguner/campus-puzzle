package com.nazlicanguner.campuspuzzle.model;

public class ClassInfo {

    private final String id;
    private final int enrollment;
    private final String professorId;

    public ClassInfo(String id, int enrollment, String professorId) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Class ID must not be blank.");
        }
        if (enrollment <= 0) {
            throw new IllegalArgumentException("Enrollment must be positive.");
        }
        if (professorId == null || professorId.isBlank()) {
            throw new IllegalArgumentException("Professor ID must not be blank.");
        }

        this.id = id;
        this.enrollment = enrollment;
        this.professorId = professorId;
    }

    public String getId() {
        return id;
    }

    public int getEnrollment() {
        return enrollment;
    }

    public String getProfessorId() {
        return professorId;
    }
}
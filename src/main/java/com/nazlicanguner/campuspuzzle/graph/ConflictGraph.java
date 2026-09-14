package com.nazlicanguner.campuspuzzle.graph;

import com.nazlicanguner.campuspuzzle.model.ClassInfo;
import com.nazlicanguner.campuspuzzle.model.ProblemData;
import com.nazlicanguner.campuspuzzle.model.StudentGroup;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConflictGraph {

    private final Map<String, Set<String>> adjacency;

    public ConflictGraph(ProblemData data) {
        adjacency = new LinkedHashMap<>();

        List<ClassInfo> classes = data.getClasses();

        for (ClassInfo classInfo : classes) {
            adjacency.put(classInfo.getId(), new LinkedHashSet<>());
        }

        for (int i = 0; i < classes.size(); i++) {
            for (int j = i + 1; j < classes.size(); j++) {
                ClassInfo first = classes.get(i);
                ClassInfo second = classes.get(j);

                boolean sameProfessor = first.getProfessorId()
                        .equals(second.getProfessorId());

                if (sameProfessor || shareStudentGroup(
                        first, second, data.getStudentGroups()
                )) {
                    addEdge(first.getId(), second.getId());
                }
            }
        }
    }

    private boolean shareStudentGroup(
            ClassInfo first,
            ClassInfo second,
            List<StudentGroup> groups
    ) {
        for (StudentGroup group : groups) {
            if (group.getClassIds().contains(first.getId())
                    && group.getClassIds().contains(second.getId())) {
                return true;
            }
        }

        return false;
    }

    private void addEdge(String firstId, String secondId) {
        adjacency.get(firstId).add(secondId);
        adjacency.get(secondId).add(firstId);
    }

    public Set<String> getClassIds() {
        return Collections.unmodifiableSet(adjacency.keySet());
    }

    public Set<String> getNeighbors(String classId) {
        Set<String> neighbors = adjacency.get(classId);

        if (neighbors == null) {
            throw new IllegalArgumentException(
                    "Unknown class ID: " + classId
            );
        }

        return Collections.unmodifiableSet(neighbors);
    }

    public int getDegree(String classId) {
        return getNeighbors(classId).size();
    }

    public boolean hasConflict(String firstId, String secondId) {
        if (!adjacency.containsKey(secondId)) {
            throw new IllegalArgumentException(
                    "Unknown class ID: " + secondId
            );
        }

        return getNeighbors(firstId).contains(secondId);
    }

    public int getEdgeCount() {
        int total = 0;

        for (Set<String> neighbors : adjacency.values()) {
            total += neighbors.size();
        }

        return total / 2;
    }
}
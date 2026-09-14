package com.nazlicanguner.campuspuzzle;

import com.nazlicanguner.campuspuzzle.model.ClassInfo;
import com.nazlicanguner.campuspuzzle.model.ProblemData;
import com.nazlicanguner.campuspuzzle.util.JsonLoader;

import java.io.IOException;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws IOException {
        Path inputPath = Path.of("data", "constraints.json");

        JsonLoader loader = new JsonLoader();
        ProblemData data = loader.load(inputPath);

        System.out.println("Campus Puzzle");
        System.out.println("Classes: " + data.getClasses().size());
        System.out.println("Rooms: " + data.getRooms().size());
        System.out.println("Student groups: " + data.getStudentGroups().size());
        System.out.println("Time slots: " + data.getTimeSlots().size());

        System.out.println();

        for (ClassInfo classInfo : data.getClasses()) {
            System.out.printf(
                    "%s | Students: %d | Professor: %s%n",
                    classInfo.getId(),
                    classInfo.getEnrollment(),
                    classInfo.getProfessorId()
            );
        }
    }
}
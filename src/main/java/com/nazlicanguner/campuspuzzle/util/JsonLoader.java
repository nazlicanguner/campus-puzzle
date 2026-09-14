package com.nazlicanguner.campuspuzzle.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nazlicanguner.campuspuzzle.model.ClassInfo;
import com.nazlicanguner.campuspuzzle.model.ProblemData;
import com.nazlicanguner.campuspuzzle.model.Room;
import com.nazlicanguner.campuspuzzle.model.StudentGroup;
import com.nazlicanguner.campuspuzzle.model.TimeSlot;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class JsonLoader {

    private final ObjectMapper mapper;

    public JsonLoader() {
        mapper = new ObjectMapper();
        mapper.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        mapper.enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    }

    public ProblemData load(Path path) throws IOException {
        JsonNode root = mapper.readTree(path.toFile());

        if (root == null || !root.isObject()) {
            throw new IllegalArgumentException(
                    "Input must be a JSON object."
            );
        }

        List<ClassInfo> classes = new ArrayList<>();
        List<Room> rooms = new ArrayList<>();
        List<StudentGroup> studentGroups = new ArrayList<>();
        List<TimeSlot> timeSlots = new ArrayList<>();

        Set<String> classIds = new HashSet<>();
        Set<String> roomIds = new HashSet<>();
        Set<String> groupIds = new HashSet<>();
        Set<String> slotIds = new HashSet<>();

        for (JsonNode node : requireArray(root, "classes")) {
            String id = requireText(node, "id");
            addUnique(classIds, id, "class");

            classes.add(new ClassInfo(
                    id,
                    requireInt(node, "enrollment"),
                    requireText(node, "professorId")
            ));
        }

        for (JsonNode node : requireArray(root, "rooms")) {
            String id = requireText(node, "id");
            addUnique(roomIds, id, "room");

            rooms.add(new Room(
                    id,
                    requireInt(node, "capacity")
            ));
        }

        for (JsonNode node : requireArray(root, "studentGroups")) {
            String id = requireText(node, "id");
            addUnique(groupIds, id, "student group");

            Set<String> attendedClasses = new LinkedHashSet<>();

            for (JsonNode classNode : requireArray(node, "classIds")) {
                if (!classNode.isTextual()
                        || classNode.textValue().isBlank()) {
                    throw new IllegalArgumentException(
                            "Class IDs in group " + id
                                    + " must be non-blank strings."
                    );
                }

                String classId = classNode.textValue();

                if (!classIds.contains(classId)) {
                    throw new IllegalArgumentException(
                            "Unknown class " + classId
                                    + " referenced by group " + id + "."
                    );
                }

                addUnique(
                        attendedClasses,
                        classId,
                        "class reference in group " + id
                );
            }

            studentGroups.add(new StudentGroup(id, attendedClasses));
        }

        for (JsonNode node : requireArray(root, "timeSlots")) {
            String id = requireText(node, "id");
            addUnique(slotIds, id, "time slot");

            timeSlots.add(new TimeSlot(
                    id,
                    requireText(node, "label")
            ));
        }

        return new ProblemData(classes, rooms, studentGroups, timeSlots);
    }

    private JsonNode requireArray(JsonNode node, String field) {
        JsonNode value = node.get(field);

        if (value == null || !value.isArray()) {
            throw new IllegalArgumentException(
                    "Field '" + field + "' must be an array."
            );
        }

        return value;
    }

    private String requireText(JsonNode node, String field) {
        JsonNode value = node.get(field);

        if (value == null
                || !value.isTextual()
                || value.textValue().isBlank()) {
            throw new IllegalArgumentException(
                    "Field '" + field + "' must be a non-blank string."
            );
        }

        return value.textValue();
    }

    private int requireInt(JsonNode node, String field) {
        JsonNode value = node.get(field);

        if (value == null
                || !value.isIntegralNumber()
                || !value.canConvertToInt()) {
            throw new IllegalArgumentException(
                    "Field '" + field + "' must be a valid integer."
            );
        }

        return value.intValue();
    }

    private void addUnique(Set<String> ids, String id, String type) {
        if (!ids.add(id)) {
            throw new IllegalArgumentException(
                    "Duplicate " + type + " ID: " + id
            );
        }
    }
}
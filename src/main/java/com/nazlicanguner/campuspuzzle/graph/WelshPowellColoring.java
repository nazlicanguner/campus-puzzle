package com.nazlicanguner.campuspuzzle.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WelshPowellColoring {

    public Map<String, Integer> color(ConflictGraph graph) {
        List<String> orderedClasses = new ArrayList<>(
                graph.getClassIds()
        );

        orderedClasses.sort(
                Comparator.<String>comparingInt(graph::getDegree)
                        .reversed()
                        .thenComparing(Comparator.naturalOrder())
        );

        Map<String, Integer> colors = new LinkedHashMap<>();
        int currentColor = 0;

        while (colors.size() < orderedClasses.size()) {
            for (String classId : orderedClasses) {
                if (colors.containsKey(classId)) {
                    continue;
                }

                if (canUseColor(classId, currentColor, graph, colors)) {
                    colors.put(classId, currentColor);
                }
            }

            currentColor++;
        }

        return Collections.unmodifiableMap(colors);
    }

    private boolean canUseColor(
            String classId,
            int color,
            ConflictGraph graph,
            Map<String, Integer> colors
    ) {
        for (String neighbor : graph.getNeighbors(classId)) {
            Integer neighborColor = colors.get(neighbor);

            if (neighborColor != null && neighborColor == color) {
                return false;
            }
        }

        return true;
    }
}
package com.historicconquest.historicconquest.map;

import javafx.geometry.Bounds;
import java.util.*;
import java.util.Map;

public class ZonePathfinder {
    private final static double ADJACENCY_TOLERANCE = 1.0; // Distance < 1px
    private final static int MAX_PATH_LENGTH = 5;
    private final static int MAX_SEARCH_DEPTH = 5;

    private final Map<Zone, Set<Zone>> adjacencyMap;


    public ZonePathfinder(List<Zone> zones) {
        this.adjacencyMap = buildAdjacencyMap(zones);
    }

    private Map<Zone, Set<Zone>> buildAdjacencyMap(List<Zone> zones) {
        Map<Zone, Set<Zone>> map = new HashMap<>();

        for (Zone zone : zones) {
            map.put(zone, new HashSet<>());
        }

        for (int i = 0; i < zones.size(); i++) {
            for (int j = i + 1; j < zones.size(); j++) {
                Zone z1 = zones.get(i);
                Zone z2 = zones.get(j);

                if (areAdjacent(z1, z2)) {
                    map.get(z1).add(z2);
                    map.get(z2).add(z1);
                }
            }
        }

        return map;
    }

    private boolean areAdjacent(Zone z1, Zone z2) {
        Bounds b1 = z1.getSVGBounds();
        Bounds b2 = z2.getSVGBounds();

        if (b1 == null || b2 == null) return false;
        if (b1.intersects(b2)) return true;


        // Horizontal distance between boxes
        double dx = 0;
        if (b1.getMaxX() < b2.getMinX())       dx = b2.getMinX() - b1.getMaxX();
        else if (b2.getMaxX() < b1.getMinX())  dx = b1.getMinX() - b2.getMaxX();

        // Vertical distance between boxes
        double dy = 0;
        if (b1.getMaxY() < b2.getMinY())      dy = b2.getMinY() - b1.getMaxY();
        else if (b2.getMaxY() < b1.getMinY()) dy = b1.getMinY() - b2.getMaxY();


        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance < ADJACENCY_TOLERANCE;
    }

    public PathResult findPath(Zone start, Zone end) {
        if (start == end) {
            return new PathResult(List.of(start), PathType.SAME_ZONE);
        }

        Queue<Zone> queue = new LinkedList<>();
        java.util.Map<Zone, Zone> parent = new HashMap<>();
        java.util.Map<Zone, Integer> depth = new HashMap<>();
        Set<Zone> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);
        depth.put(start, 0);

        while (!queue.isEmpty()) {
            Zone current = queue.poll();
            int currentDepth = depth.get(current);

            if (current == end) {
                List<Zone> path = reconstructPath(parent, end);

                if (path.size() <= MAX_PATH_LENGTH) return new PathResult(path, PathType.DIRECT);
                else                                return new PathResult(path, PathType.IMPOSSIBLE);  // Too far
            }

            if (currentDepth >= MAX_SEARCH_DEPTH) continue;

            for (Zone neighbor : adjacencyMap.getOrDefault(current, new HashSet<>())) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parent.put(neighbor, current);
                    depth.put(neighbor, currentDepth + 1);
                    queue.add(neighbor);
                }
            }
        }

        return new PathResult(null, PathType.IMPOSSIBLE);
    }

    private List<Zone> reconstructPath(Map<Zone, Zone> parent, Zone end) {
        List<Zone> path = new ArrayList<>();
        Zone current = end;

        while (current != null) {
            path.addFirst(current);
            current = parent.get(current);
        }

        return path;
    }


    public enum PathType {
        SAME_ZONE,      // Case 0
        DIRECT,         // Cases 1-4 (1 to 4 zones)
        IMPOSSIBLE      // Case 5 (water or > 4 zones)
    }

    public record PathResult(List<Zone> zones, PathType type) {}
}
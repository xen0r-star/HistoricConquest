package com.historicconquest.historicconquest.model.map;

import java.util.*;

public class ZonePathfinder {
    private final static int MAX_PATH_LENGTH = 5;
    private final static int MAX_SEARCH_DEPTH = 10;

    public static PathResult findPath(Zone start, Zone end) {
        if (start == null || end == null) {
            return new PathResult(null, PathType.IMPOSSIBLE);
        }

        if (start == end) {
            return new PathResult(List.of(start), PathType.SAME_ZONE);
        }

        Queue<SearchNode> queue = new LinkedList<>();
        Map<Zone, Zone> parent = new HashMap<>();
        Map<Zone, Integer> depth = new HashMap<>();
        Map<Zone, Boolean> usesBoatPath = new HashMap<>();
        Set<Zone> visited = new HashSet<>();

        queue.add(new SearchNode(start, false));
        visited.add(start);
        depth.put(start, 0);
        usesBoatPath.put(start, false);

        while (!queue.isEmpty()) {
            SearchNode currentNode = queue.poll();
            Zone current = currentNode.zone();
            int currentDepth = depth.get(current);

            if (current == end) {
                List<Zone> path = reconstructPath(parent, end);
                PathType pathType = usesBoatPath.getOrDefault(end, false) ? PathType.BOAT : PathType.DIRECT;

                if (path.size() <= MAX_PATH_LENGTH) return new PathResult(path, pathType);
                else                                return new PathResult(path, PathType.IMPOSSIBLE);  // Too far
            }

            if (currentDepth >= MAX_SEARCH_DEPTH) continue;

            enqueueNeighbors(queue, parent, depth, usesBoatPath, visited, current, currentNode.usesBoat(), current.getAdjacentZones(), false, currentDepth);
            enqueueNeighbors(queue, parent, depth, usesBoatPath, visited, current, currentNode.usesBoat(), current.getAdjacentBoatZones(), true, currentDepth);
        }

        return new PathResult(null, PathType.IMPOSSIBLE);
    }

    private static List<Zone> reconstructPath(Map<Zone, Zone> parent, Zone end) {
        List<Zone> path = new LinkedList<>();
        Zone current = end;

        while (current != null) {
            path.addFirst(current);
            current = parent.get(current);
        }

        return path;
    }

    private static void enqueueNeighbors(
        Queue<SearchNode> queue,
        Map<Zone, Zone> parent,
        Map<Zone, Integer> depth,
        Map<Zone, Boolean> usesBoatPath,
        Set<Zone> visited,
        Zone current,
        boolean currentUsesBoat,
        List<Zone> neighbors,
        boolean edgeIsBoat,
        int currentDepth
    ) {
        if (neighbors == null) return;

        for (Zone neighbor : neighbors) {
            if (visited.contains(neighbor)) continue;

            visited.add(neighbor);
            parent.put(neighbor, current);
            depth.put(neighbor, currentDepth + 1);
            boolean pathUsesBoat = currentUsesBoat || edgeIsBoat;
            usesBoatPath.put(neighbor, pathUsesBoat);
            queue.add(new SearchNode(neighbor, pathUsesBoat));
        }
    }

    private record SearchNode(Zone zone, boolean usesBoat) {}


    public enum PathType {
        SAME_ZONE,
        DIRECT,
        BOAT,
        IMPOSSIBLE
    }

    public record PathResult(List<Zone> zones, PathType type) {}
}
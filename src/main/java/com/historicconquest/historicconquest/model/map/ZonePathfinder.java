package com.historicconquest.historicconquest.model.map;

import com.historicconquest.historicconquest.controller.game.GameController;
import com.historicconquest.historicconquest.controller.overlay.Notification;
import com.historicconquest.historicconquest.controller.overlay.NotificationController;

import java.util.*;

public class ZonePathfinder {
    private static int MAX_PATH_LENGTH = 5;
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

                if (path.size() <= MAX_PATH_LENGTH) {
                    return new PathResult(path, pathType);

                } else {
                    NotificationController.show(
                        "Movement Impossible",
                        "Target is too far (" + (path.size() - 1) + " zones away). Check your movement range!",
                        Notification.Type.ERROR,
                        5000
                    );
                    return new PathResult(path, PathType.IMPOSSIBLE);
                }
            }

            if (currentDepth >= MAX_SEARCH_DEPTH) continue;

            enqueueNeighbors(queue, parent, depth, usesBoatPath, visited, current, currentNode.usesBoat(), current.getAdjacentZones(), false, currentDepth);
            enqueueNeighbors(queue, parent, depth, usesBoatPath, visited, current, currentNode.usesBoat(), current.getAdjacentBoatZones(), true, currentDepth);
        }

        GameController.getInstance().nextPlayer();
        return new PathResult(null, PathType.IMPOSSIBLE);
    }

    public static Set<Zone> getZonesWithinRange(Zone start, int maxDistance) {
        if (start == null || maxDistance < 0) {
            return Collections.emptySet();
        }

        Set<Zone> visited = new HashSet<>();
        Queue<Zone> queue = new LinkedList<>();
        Map<Zone, Integer> distanceMap = new HashMap<>();

        visited.add(start);
        queue.add(start);
        distanceMap.put(start, 0);

        while (!queue.isEmpty()) {
            Zone current = queue.poll();
            int currentDist = distanceMap.get(current);

            if (current.getAdjacentZones() != null) {
                for (Zone neighbor : current.getAdjacentZones()) {
                    int newDist = currentDist + 1;
                    if (newDist <= maxDistance && (!distanceMap.containsKey(neighbor) || newDist < distanceMap.get(neighbor))) {
                        visited.add(neighbor);
                        distanceMap.put(neighbor, newDist);
                        queue.add(neighbor);
                    }
                }
            }

            if (current.getAdjacentBoatZones() != null) {
                for (Zone neighbor : current.getAdjacentBoatZones()) {

                    if (neighbor.getOceanName() != null && neighbor.getOceanName().equals(start.getOceanName())) {
                        int newDist = 4;

                        if (newDist <= maxDistance && (!distanceMap.containsKey(neighbor) || newDist < distanceMap.get(neighbor))) {
                            visited.add(neighbor);
                            distanceMap.put(neighbor, newDist);
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }

        return visited;
    }

    public static DistanceResult getShortestDistance(Zone start, Zone end, int maxDepth) {
        if (start == null || end == null || maxDepth < 0) return new DistanceResult(-1, false);
        if (start == end) return new DistanceResult(0, false);

        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.distance));
        Map<Zone, Integer> bestDistances = new HashMap<>();

        pq.add(new NodeDistance(start, 0, false));
        bestDistances.put(start, 0);

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();

            if (current.distance > maxDepth) continue;
            if (current.zone == end) {
                return new DistanceResult(current.distance, current.usesBoat);
            }

            if (current.distance > bestDistances.getOrDefault(current.zone, Integer.MAX_VALUE)) continue;

            if (current.zone.getAdjacentZones() != null) {
                for (Zone neighbor : current.zone.getAdjacentZones()) {
                    int newDist = current.distance + 1;
                    if (newDist <= maxDepth && newDist < bestDistances.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                        bestDistances.put(neighbor, newDist);
                        pq.add(new NodeDistance(neighbor, newDist, current.usesBoat));
                    }
                }
            }

            if (current.zone.getAdjacentBoatZones() != null) {
                for (Zone neighbor : current.zone.getAdjacentBoatZones()) {
                    int newDist = 4;
                    if (newDist <= maxDepth && newDist < bestDistances.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                        bestDistances.put(neighbor, newDist);
                        pq.add(new NodeDistance(neighbor, newDist, true));
                    }
                }
            }
        }

        return new DistanceResult(-1, false);
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
    private record NodeDistance(Zone zone, int distance, boolean usesBoat) {}
    public record DistanceResult(int distance, boolean isBoat) {}


    public enum PathType {
        SAME_ZONE,
        DIRECT,
        BOAT,
        IMPOSSIBLE
    }

    public record PathResult(List<Zone> zones, PathType type) {}

    public static void setMaxPathLength(int maxPathLength) {
        MAX_PATH_LENGTH = maxPathLength;
    }
}
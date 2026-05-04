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

        List<Zone> landPath = findLandPath(start, end);
        if (landPath != null && landPath.size() <= MAX_PATH_LENGTH) {
            return new PathResult(landPath, PathType.DIRECT);
        }

        if (isBoatReachable(start, end)) {
            return new PathResult(List.of(start, end), PathType.BOAT);
        }

        if (landPath != null) {
            NotificationController.show(
                "Movement Impossible",
                "Target is too far (" + (landPath.size() - 1) + " zones away). Check your movement range!",
                Notification.Type.ERROR,
                5000
            );
            return new PathResult(landPath, PathType.IMPOSSIBLE);
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
        }

        if (maxDistance >= 4 && start.getAdjacentBoatZones() != null) {
            visited.addAll(start.getAdjacentBoatZones());
        }

        return visited;
    }

    public static DistanceResult getShortestDistance(Zone start, Zone end, int maxDepth) {
        if (start == null || end == null || maxDepth < 0) return new DistanceResult(-1, false);
        if (start == end) return new DistanceResult(0, false);

        int landDistance = computeLandDistance(start, end, maxDepth);
        int boatDistance = (maxDepth >= 4 && isBoatReachable(start, end)) ? 4 : -1;

        if (landDistance > 0 && (boatDistance == -1 || landDistance <= boatDistance)) {
            return new DistanceResult(landDistance, false);
        }

        if (boatDistance > 0) {
            return new DistanceResult(boatDistance, true);
        }

        return new DistanceResult(-1, false);
    }

    private static List<Zone> findLandPath(Zone start, Zone end) {
        Queue<Zone> queue = new LinkedList<>();
        Map<Zone, Zone> parent = new HashMap<>();
        Set<Zone> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Zone current = queue.poll();
            if (current == end) {
                return reconstructPath(parent, end);
            }

            if (current.getAdjacentZones() == null) continue;
            for (Zone neighbor : current.getAdjacentZones()) {
                if (visited.contains(neighbor)) continue;
                visited.add(neighbor);
                parent.put(neighbor, current);
                queue.add(neighbor);
            }
        }

        return null;
    }

    private static int computeLandDistance(Zone start, Zone end, int maxDepth) {
        Queue<Zone> queue = new LinkedList<>();
        Map<Zone, Integer> distanceMap = new HashMap<>();

        queue.add(start);
        distanceMap.put(start, 0);

        while (!queue.isEmpty()) {
            Zone current = queue.poll();
            int dist = distanceMap.get(current);
            if (dist > maxDepth) continue;
            if (current == end) return dist;

            if (current.getAdjacentZones() == null) continue;
            for (Zone neighbor : current.getAdjacentZones()) {
                int newDist = dist + 1;
                if (newDist <= maxDepth && newDist < distanceMap.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    distanceMap.put(neighbor, newDist);
                    queue.add(neighbor);
                }
            }
        }

        return -1;
    }

    private static boolean isBoatReachable(Zone start, Zone end) {
        if (start.getAdjacentBoatZones() == null) return false;
        if (start.getAdjacentBoatZones().contains(end)) return true;

        Queue<Zone> queue = new LinkedList<>();
        Set<Zone> visited = new HashSet<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Zone current = queue.poll();
            if (current == end) return true;
            if (current.getAdjacentBoatZones() == null) continue;
            for (Zone neighbor : current.getAdjacentBoatZones()) {
                if (visited.add(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }

        return false;
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
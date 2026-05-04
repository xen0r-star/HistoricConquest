package com.historicconquest.server.model.map;

import java.util.*;

public class ZonePathfinder {
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

    private record NodeDistance(Zone zone, int distance, boolean usesBoat) {}
    public record DistanceResult(int distance, boolean isBoat) {}
}
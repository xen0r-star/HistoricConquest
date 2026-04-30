package com.historicconquest.historicconquest.model.map;

import com.historicconquest.historicconquest.controller.game.GameController;
import com.historicconquest.historicconquest.controller.overlay.Notification;
import com.historicconquest.historicconquest.controller.overlay.NotificationController;

import java.util.*;

public class ZonePathfinder {
    private static int MAX_PATH_LENGTH = 5;
    private final static int MAX_SEARCH_DEPTH = 10;

    public static PathResult findPath(Zone start, Zone end) {
        if (start == end) {
            System.out.println("Resultat : SAME_ZONE");
            return new PathResult(List.of(start), PathType.SAME_ZONE);

        }


        Queue<Zone> queue = new LinkedList<>();
        Map<Zone, Zone> parent = new HashMap<>();
        Map<Zone, Integer> depth = new HashMap<>();
        Set<Zone> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);
        depth.put(start, 0);

        while (!queue.isEmpty()) {
            Zone current = queue.poll();
            int currentDepth = depth.get(current);

            if (current == end) {
                List<Zone> path = reconstructPath(parent, end);

                if (path.size() <= MAX_PATH_LENGTH) {
                    System.out.println("Resultat : DIRECT (Distance: " + (path.size() - 1) + ")");
                    return new PathResult(path, PathType.DIRECT);
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

            List<Zone> neighbors = current.getAdjacentZones();
            if (neighbors != null) {
                for (Zone neighbor : neighbors) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        parent.put(neighbor, current);
                        depth.put(neighbor, currentDepth + 1);
                        queue.add(neighbor);
                    }
                }
            }
        }

        GameController.getInstance().nextPlayer();
        return new PathResult(null, PathType.IMPOSSIBLE);
    }

    private static List<Zone> reconstructPath(Map<Zone, Zone> parent, Zone end) {
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


    public static void setMaxPathLength(int maxPathLength) {
        MAX_PATH_LENGTH = maxPathLength;
    }
}
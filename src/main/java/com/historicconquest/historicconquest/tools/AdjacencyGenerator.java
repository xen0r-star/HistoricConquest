package com.historicconquest.historicconquest.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.historicconquest.historicconquest.map.Zone;
import com.historicconquest.historicconquest.map.WorldMap;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class AdjacencyGenerator {
    private static final String filePath = "./adjacency.json";


    public static void main(String[] args) throws IOException {
        WorldMap worldMap = new WorldMap();
        List<Zone> zones = worldMap.getAllZones();


        Map<String, Set<String>> adjacencyZone = new HashMap<>();

        for (Zone zone : zones) {
            adjacencyZone.put(zone.getName(), new HashSet<>());
        }

        for (int i = 0; i < zones.size(); i++) {
            for (int j = i + 1; j < zones.size(); j++) {
                Zone z1 = zones.get(i);
                Zone z2 = zones.get(j);

                if (areAdjacent(z1, z2)) {
                    adjacencyZone.get(z1.getName()).add(z2.getName());
                    adjacencyZone.get(z2.getName()).add(z1.getName());
                }
            }
        }


        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File(filePath), adjacencyZone);
    }


    private static boolean areAdjacent(Zone z1, Zone z2) {
        Shape s1 = getShape(z1);
        Shape s2 = getShape(z2);

        if (s1 == null || s2 == null) return false;

        Shape intersection = Shape.intersect(s1, s2);

        Bounds bounds = intersection.getBoundsInLocal();

        return bounds.getWidth() > 0 || bounds.getHeight() > 0;
    }

    private static Shape getShape(Zone zone) {
        if (zone.getZoneSVGGroup() == null) {
            return null;
        }

        Shape combinedShape = null;
        for (Node node : zone.getZoneSVGGroup().getChildren()) {
            if (node instanceof SVGPath svgPath) {
                if (combinedShape == null) {
                    combinedShape = svgPath;
                } else {
                    combinedShape = Shape.union(combinedShape, svgPath);
                }
            }
        }

        return combinedShape;
    }
}

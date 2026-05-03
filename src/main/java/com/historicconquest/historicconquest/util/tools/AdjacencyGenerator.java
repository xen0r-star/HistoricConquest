package com.historicconquest.historicconquest.util.tools;

import com.historicconquest.historicconquest.model.map.WorldMap;
import com.historicconquest.historicconquest.model.map.Zone;
import com.historicconquest.historicconquest.view.map.MapView;
import com.historicconquest.historicconquest.view.map.MapViewFactory;
import com.historicconquest.historicconquest.view.map.ZoneView;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Scale;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class AdjacencyGenerator {
    private static final String FILE_PATH = "src/main/resources/map/adjacency.json";
    private static final String OCEANS_CONFIG_PATH = "/map/oceans/oceans_config.json";

    public static void main(String[] args) {
        WorldMap worldMap = new WorldMap();
        MapView mapView = MapViewFactory.build(worldMap, false);

        List<Zone> allZones = worldMap.getAllZones();
        List<OceanSpec> oceans = loadOceans();

        Set<String> oceanNames = new HashSet<>();
        for (OceanSpec ocean : oceans) {
            oceanNames.add(ocean.name());
        }

        List<Zone> landZones = new ArrayList<>();
        for (Zone zone : allZones) {
            if (!oceanNames.contains(zone.getName())) {
                landZones.add(zone);
            }
        }

        Map<String, Set<String>> adjacencyZone = new TreeMap<>();
        Map<String, Set<String>> adjacencyOcean = new TreeMap<>();

        for (Zone zone : landZones) {
            adjacencyZone.put(zone.getName(), new HashSet<>());
        }
        for (OceanSpec ocean : oceans) {
            adjacencyOcean.put(ocean.name(), new HashSet<>());
        }

        for (int i = 0; i < landZones.size(); i++) {
            for (int j = i + 1; j < landZones.size(); j++) {
                Zone z1 = landZones.get(i);
                Zone z2 = landZones.get(j);

                if (areAdjacent(mapView.getViewFor(z1), mapView.getViewFor(z2))) {
                    adjacencyZone.get(z1.getName()).add(z2.getName());
                    adjacencyZone.get(z2.getName()).add(z1.getName());
                }
            }
        }

        Map<String, Shape> oceanShapes = new HashMap<>();
        for (OceanSpec ocean : oceans) {
            Shape oceanShape = loadOceanShape(ocean);
            if (oceanShape != null) {
                oceanShapes.put(ocean.name(), oceanShape);
            }
        }

        for (Zone zone : landZones) {
            Shape zoneShape = getShape(mapView.getViewFor(zone));
            if (zoneShape == null) continue;

            for (Map.Entry<String, Shape> oceanEntry : oceanShapes.entrySet()) {
                if (areShapesAdjacent(zoneShape, oceanEntry.getValue())) {
                    adjacencyOcean.get(oceanEntry.getKey()).add(zone.getName());
                }
            }
        }

        // Adjacency correction
        adjacencyZone.get("Western Europe").add("British Isles - Iceland");
        adjacencyZone.get("British Isles - Iceland").add("Western Europe");

        Map<String, Object> output = new LinkedHashMap<>();
        output.put("zones", adjacencyZone);
        output.put("oceans", adjacencyOcean);

        try {
            new ObjectMapper().writeValue(new File(FILE_PATH), output);

        } catch (Exception e) {
            throw new RuntimeException("Failed to write adjacency file", e);
        }
    }

    private static boolean areAdjacent(ZoneView z1, ZoneView z2) {
        Shape s1 = getShape(z1);
        Shape s2 = getShape(z2);

        if (s1 == null || s2 == null) return false;
        return areShapesAdjacent(s1, s2);
    }

    private static boolean areShapesAdjacent(Shape s1, Shape s2) {
        Shape intersection = Shape.intersect(s1, s2);
        Bounds bounds = intersection.getBoundsInLocal();
        return bounds.getWidth() > 0 || bounds.getHeight() > 0;
    }

    private static Shape getShape(ZoneView zoneView) {
        if (zoneView == null || zoneView.getZoneSVGGroup() == null) return null;

        Shape combinedShape = null;
        for (Node node : zoneView.getZoneSVGGroup().getChildren()) {
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

    private static List<OceanSpec> loadOceans() {
        try (InputStream is = AdjacencyGenerator.class.getResourceAsStream(OCEANS_CONFIG_PATH)) {
            if (is == null) {
                throw new IllegalStateException("Ocean config not found: " + OCEANS_CONFIG_PATH);
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(is);
            List<OceanSpec> oceans = new ArrayList<>();

            for (JsonNode oceanNode : root) {
                oceans.add(new OceanSpec(
                        oceanNode.get("name").asString(),
                        oceanNode.get("x").asDouble(),
                        oceanNode.get("y").asDouble(),
                        oceanNode.get("w").asDouble(),
                        oceanNode.get("h").asDouble()
                ));
            }

            return oceans;

        } catch (Exception e) {
            throw new RuntimeException("Failed to load oceans config", e);
        }
    }

    private static Shape loadOceanShape(OceanSpec ocean) {
        String resourcePath = "/map/oceans/" + ocean.name() + ".svg";

        try (InputStream svgStream = AdjacencyGenerator.class.getResourceAsStream(resourcePath)) {
            if (svgStream == null) return null;

            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(svgStream);
            NodeList paths = doc.getElementsByTagName("path");

            Shape combinedShape = null;
            for (int i = 0; i < paths.getLength(); i++) {
                Element el = (Element) paths.item(i);
                SVGPath svgPath = new SVGPath();
                svgPath.setContent(el.getAttribute("d"));

                if (combinedShape == null) {
                    combinedShape = svgPath;

                } else {
                    combinedShape = Shape.union(combinedShape, svgPath);
                }
            }

            if (combinedShape == null) return null;

            Bounds bounds = combinedShape.getLayoutBounds();
            if (bounds.getWidth() > 0 && bounds.getHeight() > 0 && ocean.w() > 0 && ocean.h() > 0) {
                double scaleX = ocean.w() / bounds.getWidth();
                double scaleY = ocean.h() / bounds.getHeight();
                combinedShape.getTransforms().add(new Scale(scaleX, scaleY, 0, 0));
                combinedShape.setTranslateX(ocean.x());
                combinedShape.setTranslateY(ocean.y());
            }

            return combinedShape;

        } catch (Exception e) {
            return null;
        }
    }

    private record OceanSpec(String name, double x, double y, double w, double h) {}
}
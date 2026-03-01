package com.historicconquest.historicconquest.map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.historicconquest.historicconquest.Constant;
import com.historicconquest.historicconquest.questions.TypeThemes;
import javafx.scene.paint.Color;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class WorldMap {
    private final List<Bloc> blocs = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private JsonNode iconConfig;

    public WorldMap() {
        loadIconConfig();
        loadMapConfig();
        initializeAdjacencies();
    }

    private void loadIconConfig() {
        String path = Constant.PATH + "icons/icon_config.json";

        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) throw new RuntimeException("Icon config not found: " + path);
            iconConfig = mapper.readTree(is);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void loadMapConfig() {
        String path = Constant.PATH + "zones/map_config.json";

        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) throw new RuntimeException("Map config not found: " + path);

            JsonNode root = mapper.readTree(is);

            for (JsonNode blocNode : root) {
                String blocName = blocNode.get("name").asText();
                Bloc bloc = new Bloc(TypeBloc.valueOf(blocName.toUpperCase()));

                JsonNode children = blocNode.get("children");
                for (JsonNode zoneNode : children) {
                    String zoneName = zoneNode.get("name").asText();

                    JsonNode iconJson = searchIcon(blocName, zoneName);
                    if (iconJson == null) continue;

                    Zone.ZoneIcon icon = new Zone.ZoneIcon(
                        iconJson.get("x").asDouble(),
                        iconJson.get("y").asDouble(),
                        iconJson.get("w").asDouble(),
                        iconJson.get("h").asDouble()
                    );

                    Zone zone = new Zone(
                        zoneName,
                        blocName,
                        TypeThemes.getRandom(),
                        0,
                        zoneNode.get("x").asDouble(),
                        zoneNode.get("y").asDouble(),
                        icon,
                        Color.web("#C0A57F")
                    );

                    bloc.addZone(zone);
                }

                blocs.add(bloc);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private JsonNode searchIcon(String blocName, String zoneName) {
        for (JsonNode blocNode : iconConfig) {
            if (blocNode.path("name").asText().equals(blocName)) {
                for (JsonNode zoneNode : blocNode.path("children")) {
                    if (zoneNode.path("name").asText().equals(zoneName)) {
                        return zoneNode;
                    }
                }
            }
        }
        return null;
    }

    private void initializeAdjacencies() {
        String path = Constant.PATH + "map/adjacency.json";
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) return;

            ObjectMapper mapper = new ObjectMapper();
            JsonNode adjacencyData = mapper.readTree(is);

            List<Zone> allZones = getAllZones();
            for (Zone zone : allZones) {
                JsonNode zoneAdjacentNode = adjacencyData.get(zone.getName());

                if (zoneAdjacentNode != null && zoneAdjacentNode.isArray()) {
                    for (JsonNode adjacentName : zoneAdjacentNode) {
                        String neighborName = adjacentName.asText();

                        allZones.stream()
                                .filter(z -> z.getName().equals(neighborName))
                                .findFirst().ifPresent(zone::addAdjacentZones);
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load adjacency data");
        }
    }



    public List<Zone> getAllZones() {
        List<Zone> allZones = new ArrayList<>();
        for (Bloc bloc : blocs) {
            allZones.addAll(bloc.getZones());
        }
        return allZones;
    }

    public List<Bloc> getBlocs() {
        return blocs;
    }
}
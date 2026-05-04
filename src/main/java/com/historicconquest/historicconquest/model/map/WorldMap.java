package com.historicconquest.historicconquest.model.map;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.type.TypeReference;
import javafx.scene.paint.Color;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldMap {
    private final List<Bloc> blocs = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private JsonNode iconConfig;

    public WorldMap() {
        this(
            true, true, true,
            Color.web("#C0A57F"),
            Color.web("#635341")
        );
    }

    public WorldMap(boolean loadMapConfig, boolean loadIcon, boolean loadAdjacency, Color defaultZoneColor, Color defaultZoneBorderColor) {
        if (loadIcon) loadIconConfig();
        if (loadMapConfig) loadMapConfig(loadIcon, defaultZoneColor, defaultZoneBorderColor);
        if (loadAdjacency) initializeAdjacencies();
    }


    private void loadIconConfig() {
        String path = "/map/icons/icon_config.json";

        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) throw new RuntimeException("Icon config not found: " + path);
            iconConfig = mapper.readTree(is);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void loadMapConfig(boolean loadIcon, Color defaultZoneColor, Color defaultZoneBorderColor) {
        String path = "/map/zones/map_config.json";

        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) throw new RuntimeException("Map config not found: " + path);

            JsonNode root = mapper.readTree(is);

            for (JsonNode blocNode : root) {
                String blocName = blocNode.get("name").asString();
                Bloc bloc = new Bloc(TypeBloc.valueOf(blocName.toUpperCase()));

                JsonNode children = blocNode.get("children");
                for (JsonNode zoneNode : children) {
                    String zoneName = zoneNode.get("name").asString();
                    Zone zone;

                    if (loadIcon) {
                        JsonNode iconJson = searchIcon(blocName, zoneName);
                        if (iconJson == null) continue;

                        Zone.ZoneIcon icon = new Zone.ZoneIcon(
                            iconJson.get("x").asDouble(),
                            iconJson.get("y").asDouble(),
                            iconJson.get("w").asDouble(),
                            iconJson.get("h").asDouble()
                        );

                        zone = new Zone(
                            zoneName,
                            blocName,
                                0,
                            zoneNode.get("x").asDouble(),
                            zoneNode.get("y").asDouble(),
                            icon,
                            defaultZoneColor,
                            defaultZoneBorderColor
                        );

                    } else {
                        zone = new Zone(
                            zoneName,
                            blocName,
                            0,
                            zoneNode.get("x").asDouble(),
                            zoneNode.get("y").asDouble(),
                            defaultZoneColor,
                            defaultZoneBorderColor
                        );

                        zone.setColor(defaultZoneColor);
                    }

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
            if (blocNode.path("name").asString().equals(blocName)) {
                for (JsonNode zoneNode : blocNode.path("children")) {
                    if (zoneNode.path("name").asString().equals(zoneName)) {
                        return zoneNode;
                    }
                }
            }
        }

        return null;
    }

    private void initializeAdjacencies() {
        String path = "/map/adjacency.json";
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) return;

            ObjectMapper mapper = new ObjectMapper();
            JsonNode adjacencyData = mapper.readTree(is);

            List<Zone> allZones = getAllZones();
            Map<String, Zone> zonesByName = new HashMap<>();
            for (Zone zone : allZones) {
                zonesByName.put(zone.getName(), zone);
            }

            JsonNode zonesNode = adjacencyData.get("zones");
            Map<String, List<String>> landAdjacencies = mapper.convertValue(
                zonesNode != null ? zonesNode : adjacencyData,
                new TypeReference<>() {}
            );
            if (landAdjacencies != null) {
                landAdjacencies.forEach((zoneName, adjacentNames) -> connectLandAdjacencies(zonesByName, zoneName, adjacentNames));
            }

            JsonNode oceansNode = adjacencyData.get("oceans");
            Map<String, List<String>> oceanAdjacencies = mapper.convertValue(
                oceansNode,
                new TypeReference<>() {}
            );
            if (oceanAdjacencies != null) {
                oceanAdjacencies.forEach((oceanName, zoneNames) -> {
                    connectBoatAdjacencies(zonesByName, zoneNames, oceanName);
                });
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load adjacency data");
        }
    }


    private void connectLandAdjacencies(Map<String, Zone> zonesByName, String zoneName, List<String> adjacentNames) {
        Zone zone = zonesByName.get(zoneName);
        if (zone == null || adjacentNames == null) return;

        for (String adjacentName : adjacentNames) {
            Zone neighbor = zonesByName.get(adjacentName);
            if (neighbor != null) {
                zone.addAdjacentZone(neighbor);
            }
        }
    }

    private void connectBoatAdjacencies(Map<String, Zone> zonesByName, List<String> zoneNames, String oceanName) {
        if (zoneNames == null) return;

        List<Zone> oceanZones = new ArrayList<>();
        for (String zoneName : zoneNames) {
            Zone zone = zonesByName.get(zoneName);
            if (zone != null) {
                zone.setOceanName(oceanName);
                oceanZones.add(zone);
            }
        }

        for (int i = 0; i < oceanZones.size(); i++) {
            for (int j = i + 1; j < oceanZones.size(); j++) {
                oceanZones.get(i).addAdjacentBoatZone(oceanZones.get(j));
                oceanZones.get(j).addAdjacentBoatZone(oceanZones.get(i));
            }
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
        return Collections.unmodifiableList(blocs);
    }
}
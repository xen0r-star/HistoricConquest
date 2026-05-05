package com.historicconquest.server.model.map;

import com.historicconquest.server.model.questions.TypeThemes;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.*;

public class WorldMap {
    private final List<Bloc> blocs = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();


    public WorldMap() {
        loadMapConfig();
        initializeAdjacencies();
    }

    private void loadMapConfig() {
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
                    Zone zone = new Zone(
                        zoneName,
                            TypeThemes.getRandom(),
                        0
                    );

                    bloc.addZone(zone);
                }

                blocs.add(bloc);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
                oceanAdjacencies.forEach((oceanName, zoneNames) ->
                    connectBoatAdjacencies(zonesByName, zoneNames, oceanName)
                );
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

    public TypeBloc getBlocTypeForZone(String zoneName) {
        return blocs.stream()
            .filter(bloc -> bloc.getZones().stream()
                    .anyMatch(z -> z.getName().equalsIgnoreCase(zoneName)))
            .map(Bloc::getName)
            .findFirst()
            .orElse(null);
    }

    public List<Bloc> getBlocs() {
        return Collections.unmodifiableList(blocs);
    }
}
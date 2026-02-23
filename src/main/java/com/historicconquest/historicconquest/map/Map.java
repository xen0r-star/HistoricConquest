package com.historicconquest.historicconquest.map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.historicconquest.historicconquest.questions.TypeThemes;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Map {
    private final List<Bloc> blocs = new ArrayList<>();

    private final ObjectMapper mapper = new ObjectMapper();
    private JsonNode iconConfig;

    public Map() {
        loadIconConfig();
        loadMapConfig();
    }

    private void loadIconConfig() {
        String path = "/com/historicconquest/historicconquest/icons/icon_config.json";

        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) throw new RuntimeException("Icon config not found: " + path);
            iconConfig = mapper.readTree(is);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void loadMapConfig() {
        String path = "/com/historicconquest/historicconquest/zones/map_config.json";

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
                        icon
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

    public List<Bloc> getBlocs() {
        return blocs;
    }
}
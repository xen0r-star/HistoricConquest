package com.historicconquest.historicconquest.map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.historicconquest.historicconquest.Constant;
import com.historicconquest.historicconquest.questions.TypeThemes;
import com.historicconquest.historicconquest.ui.ZoneInfoPanel;
import javafx.scene.paint.Color;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Map {
    private final List<Bloc> blocs = new ArrayList<>();

    private final ObjectMapper mapper = new ObjectMapper();
    private JsonNode iconConfig;
    private final ZoneInfoPanel zoneInfoPanel;

    public Map(ZoneInfoPanel zoneInfoPanel) {
        this.zoneInfoPanel = zoneInfoPanel;
        loadIconConfig();
        loadMapConfig();
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
                        Color.web("#8D8051")
                    );

                    zone.setOnMouseClicked(e -> handleZoneSelection(zone));

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


    private void handleZoneSelection(Zone zone) {
        if (zone.isFocusedZone()) {
            // La zone est actuellement sélectionnée -> désélectionner
            zone.setFocusedZone(false);
            zone.setColor(Color.web("#8D8051")); // Couleur par défaut

            // Masquer le panneau d'infos
            if (zoneInfoPanel != null) {
                zoneInfoPanel.hide();
            }
        } else {
            // Désélectionner la zone précédente s'il y en a une
            blocs.forEach(bloc -> {
                bloc.getZones().forEach(z -> {
                    if (z.isFocusedZone() && !z.equals(zone)) {
                        z.setFocusedZone(false);
                        z.setColor(Color.web("#8D8051")); // Couleur par défaut
                    }
                });
            });

            // Sélectionner la nouvelle zone
            zone.setFocusedZone(true);
            zone.setColor(Color.web("#D4AF37")); // Couleur dorée pour zone sélectionnée

            // Afficher les infos de la zone
            if (zoneInfoPanel != null) {
                zoneInfoPanel.setData(zone.getName());
                zoneInfoPanel.show();
            }
        }
    }

    public List<Bloc> getBlocs() {
        return blocs;
    }
}
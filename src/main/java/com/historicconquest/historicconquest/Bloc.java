package com.historicconquest.historicconquest;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.scene.Group;

import java.util.ArrayList;
import java.util.List;

public class Bloc extends Group {
    private final String nom ;
    private final List<Zone> zones = new ArrayList<>();

    public Bloc(JsonNode blocNode)
    {
        this.nom = blocNode.get("name").asText();
        this.setLayoutX(0.0);
        this.setLayoutY(0.0);

        JsonNode children = blocNode.get("children");

        for(JsonNode zoneNode : children)
        {
            Zone zone = new Zone(
                    zoneNode.get("name").asText(),
                    zoneNode.get("x").asDouble(),
                    zoneNode.get("y").asDouble(),
            this.nom);

            this.zones.add(zone);
            this.getChildren().add(zone);
        }
    }

    public List<Zone> getZones()
    {
        return zones ;
    }
}

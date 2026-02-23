package com.historicconquest.historicconquest.map;

import javafx.scene.Group;

import java.util.ArrayList;
import java.util.List;

public class Bloc extends Group {
    private final TypeBloc name ;
    private final List<Zone> zones = new ArrayList<>();

    public Bloc(TypeBloc name) {
        this.name = name;
        this.setLayoutX(0.0);
        this.setLayoutY(0.0);
    }


    public void addZone(Zone zone) {
        if(zone != null && !zones.contains(zone)) {
            zones.add(zone);
            this.getChildren().add(zone);
        }
    }

    public List<Zone> getZones() {
        return zones ;
    }

    public TypeBloc getName() {
        return name;
    }
}

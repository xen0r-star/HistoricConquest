package com.historicconquest.server.model.map;

import java.util.ArrayList;
import java.util.List;

public class Bloc {
    private final TypeBloc name;
    private final List<Zone> zones = new ArrayList<>();

    public Bloc(TypeBloc name) {
        this.name = name;
    }

    public void addZone(Zone zone) {
        if (zone == null || zones.contains(zone)) return;
        zones.add(zone);
    }

    public List<Zone> getZones() {
        return zones;
    }

    public TypeBloc getName() {
        return name;
    }
}

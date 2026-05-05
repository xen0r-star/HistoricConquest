package com.historicconquest.server.model.map;

import com.historicconquest.server.model.questions.TypeThemes;

import java.util.ArrayList;
import java.util.List;

public class Zone {
    private final String name;
    private TypeThemes themes;
    private int power;
    private static final int MAX_POWER_ZONE = 8;

    private String nameOwner = "Nobody";

    private final List<Zone> adjacentZones;
    private final List<Zone> adjacentBoatZones;
    private String oceanName;


    public Zone(String name, TypeThemes themes, int power) {
        this.name = name;
        this.themes = themes;
        this.power = power;
        this.adjacentZones = new ArrayList<>();
        this.adjacentBoatZones = new ArrayList<>();
    }


    public void addAdjacentZone(Zone zone) {
        if (zone == null || adjacentZones.contains(zone)) return;
        adjacentZones.add(zone);
        zone.addAdjacentZone(this);
    }

    public void addAdjacentBoatZone(Zone zone) {
        if (zone == null || adjacentBoatZones.contains(zone)) return;
        adjacentBoatZones.add(zone);
        zone.addAdjacentBoatZone(this);
    }


    public List<Zone> getAdjacentZones() {
        return adjacentZones;
    }

    public List<Zone> getAdjacentBoatZones() {
        return adjacentBoatZones;
    }


    public String getName() {
        return name;
    }

    public void setNameOwner(String nameOwner) {
        this.nameOwner = nameOwner;
    }

    public String getNameOwner()
    {
        return nameOwner ;
    }

    public TypeThemes getThemes() {
        return themes;
    }

    public void setThemes(TypeThemes themes) {
        this.themes = themes;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getMAX_POWER_ZONE() {
        return MAX_POWER_ZONE;
    }

    public int getPower() {
        return power ;
    }

    public String getOceanName() {
        return oceanName;
    }

    public void setOceanName(String oceanName) {
        this.oceanName = oceanName;
    }
}

package com.historicconquest.historicconquest.model.map;

import com.historicconquest.historicconquest.model.player.Pawn;
import com.historicconquest.historicconquest.model.questions.TypeThemes;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class Zone {
    private final String name;
    private final String blocName;
    private final TypeThemes themes;
    private int power;

    private final List<Pawn> pawns;
    private final List<Zone> adjacentZones;

    private final ZoneIcon icon;

    private final double x;
    private final double y;
    private Color color;
    private final Color borderColor;


    public Zone(String name, String blocName, int power, double x, double y, Color color, Color borderColor) {
        this(name, blocName, TypeThemes.NONE, power, x, y, null, color, borderColor);
    }

    public Zone(String name, String blocName, TypeThemes themes, int power, double x, double y, ZoneIcon icon, Color color, Color borderColor) {
        this.name = name;
        this.blocName = blocName;
        this.themes = themes;
        this.power = power;
        this.icon = icon;
        this.x = x;
        this.y = y;
        this.color = color;
        this.borderColor = borderColor;
        this.pawns = new ArrayList<>();
        this.adjacentZones = new ArrayList<>();
    }


    public void addAdjacentZone(Zone zone) {
        if (zone == null || adjacentZones.contains(zone)) return;
        adjacentZones.add(zone);
        zone.addAdjacentZone(this);
    }


    public List<Zone> getAdjacentZones() {
        return adjacentZones;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public String getName() {
        return name;
    }

    public String getBlocName() {
        return blocName;
    }

    public TypeThemes getThemes() {
        return themes;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public List<Pawn> getPawns() {
        return pawns;
    }

    public ZoneIcon getIcon() {
        return icon;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }



    public record ZoneIcon(double x, double y, double width, double height) {}
}

package com.historicconquest.historicconquest.model.map;

import com.historicconquest.historicconquest.model.player.Pawn;
import com.historicconquest.historicconquest.model.questions.TypeThemes;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class Zone {
    private final String name;
    private final String blocName;
    private final TypeThemes themes;
    private int power;
    private final int MAX_POWER_ZONE = 8 ;

    private String nameOwner ="Nobody" ;

    private final List<Pawn> pawns;
    private final List<Zone> adjacentZones;

    private final ZoneIcon icon;

    private final double x;
    private final double y;
    private final Color baseColor;
    private final ObjectProperty<Color> colorProperty;
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
        this.baseColor = color;
        this.colorProperty = new SimpleObjectProperty<>(color);
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
        colorProperty.set(color);
    }

    public Color getColor() {
        return colorProperty.get();
    }

    public ObjectProperty<Color> colorProperty() {
        return colorProperty;
    }

    public Color getBaseColor() {
        return baseColor;
    }

    public Color getBorderColor() {
        return borderColor;
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

    public String getBlocName() {
        return blocName;
    }

    public TypeThemes getThemes() {
        return themes;
    }

    public String getPowertext() {
        return ""+power+"";
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

    public int getMAX_POWER_ZONE() {
        return MAX_POWER_ZONE;
    }

    public int getPower() {
        return power ;
    }


    public record ZoneIcon(double x, double y, double width, double height) {}
}

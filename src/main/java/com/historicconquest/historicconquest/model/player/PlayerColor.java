package com.historicconquest.historicconquest.model.player;

import javafx.scene.paint.Color;

public enum PlayerColor {
    RED("#A2383A"),
    ORANGE( "#B9693E"),
    YELLOW("#B68D3B"),
    GREEN("#61712A"),
    LIME("#89A238"),
    CYAN("#38A270"),
    BLUE("#389BA2"),
    LIGHT_BLUE("#385BA2"),
    PURPLE("#6838A2"),
    PINK("#A23887");

    private final String color;

    PlayerColor(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public Color getJavafxColor() {
        return Color.web(color);
    }
}



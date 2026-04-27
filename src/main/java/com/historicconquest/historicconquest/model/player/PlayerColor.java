package com.historicconquest.historicconquest.model.player;

import javafx.scene.paint.Color;

public enum PlayerColor {
    RED(Color.web("#A2383A")),
    ORANGE( Color.web("#B9693E")),
    YELLOW(Color.web("#B68D3B")),
    GREEN(Color.web("#61712A")),
    LIME(Color.web("#89A238")),
    CYAN(Color.web("#38A270")),
    BLUE(Color.web("#389BA2")),
    LIGHT_BLUE(Color.web("#385BA2")),
    PURPLE(Color.web("#6838A2")),
    PINK(Color.web("#A23887")) ;

    private final Color javafxColor ;

    PlayerColor(Color color )
    {
        this.javafxColor = color ;
    }

    public Color getJavafxColor() {
        return javafxColor;
    }
}



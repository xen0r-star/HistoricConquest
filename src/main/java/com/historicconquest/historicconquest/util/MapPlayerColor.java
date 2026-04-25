package com.historicconquest.historicconquest.util;

import com.historicconquest.historicconquest.model.player.PlayerColor;
import javafx.scene.paint.Color;

public class MapPlayerColor {
    private static final String DEFAULT_ZONE_COLOR = "#f2e1bf";


    public static String hex(String rawColor) {
        if (rawColor == null || rawColor.isBlank()) {
            return DEFAULT_ZONE_COLOR;
        }

        try {
            return switch (PlayerColor.valueOf(rawColor.trim().toUpperCase())) {
                case RED ->"#A2383A";
                case ORANGE -> "#B9693E";
                case YELLOW -> "#B68D3B";
                case GREEN -> "#61712A";
                case LIME -> "#89A238";
                case CYAN -> "#38A270";
                case BLUE -> "#389BA2";
                case LIGHT_BLUE -> "#385BA2";
                case PURPLE -> "#6838A2";
                case PINK -> "#A23887";
            };

        } catch (Exception e) {
            return DEFAULT_ZONE_COLOR;
        }
    }

    public static Color color(String rawColor) {
        if (rawColor == null || rawColor.isBlank()) {
            return Color.web(DEFAULT_ZONE_COLOR);
        }

        try {
            return switch (PlayerColor.valueOf(rawColor.trim().toUpperCase())) {
                case RED -> Color.web("#A2383A");
                case ORANGE -> Color.web("#B9693E");
                case YELLOW -> Color.web("#B68D3B");
                case GREEN -> Color.web("#61712A");
                case LIME -> Color.web("#89A238");
                case CYAN -> Color.web("#38A270");
                case BLUE -> Color.web("#389BA2");
                case LIGHT_BLUE -> Color.web("#385BA2");
                case PURPLE -> Color.web("#6838A2");
                case PINK -> Color.web("#A23887");
            };

        } catch (Exception e) {
            return Color.web(DEFAULT_ZONE_COLOR);
        }
    }
}

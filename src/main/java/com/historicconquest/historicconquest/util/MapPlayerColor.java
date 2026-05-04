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
            return PlayerColor.valueOf(rawColor.trim().toUpperCase()).getColor();

        } catch (Exception e) {
            return DEFAULT_ZONE_COLOR;
        }
    }

    public static Color color(String rawColor) {
        if (rawColor == null || rawColor.isBlank()) {
            return Color.web(DEFAULT_ZONE_COLOR);
        }

        try {
            return PlayerColor.valueOf(rawColor.trim().toUpperCase()).getJavafxColor();

        } catch (Exception e) {
            return Color.web(DEFAULT_ZONE_COLOR);
        }
    }
}

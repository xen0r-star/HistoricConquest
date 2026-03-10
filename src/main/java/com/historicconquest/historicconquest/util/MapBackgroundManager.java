package com.historicconquest.historicconquest.util;

import com.historicconquest.historicconquest.map.WorldMap;
import javafx.scene.paint.Color;

public class MapBackgroundManager {
    private static MapBackgroundManager instance;
    private static WorldMap backgroundMap;

    private static final Object LOCK = new Object();

    private MapBackgroundManager() { }


    public static MapBackgroundManager getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new MapBackgroundManager();
                }
            }
        }
        return instance;
    }

    public static WorldMap getBackgroundMap() {
        if (backgroundMap == null) {
            synchronized (LOCK) {
                if (backgroundMap == null) {
                    backgroundMap = new WorldMap(
                        true,
                        false,
                        false,
                        Color.web("#f2e1bf"),
                        Color.web("#C5A682")
                    );
                }
            }
        }
        return backgroundMap;
    }

    public static void clearCache() {
        synchronized (LOCK) {
            backgroundMap = null;
        }
    }
}


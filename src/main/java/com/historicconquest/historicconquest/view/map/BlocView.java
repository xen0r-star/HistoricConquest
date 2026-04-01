package com.historicconquest.historicconquest.view.map;

import javafx.scene.Group;

import java.util.ArrayList;
import java.util.List;

public class BlocView extends Group {
    private final List<ZoneView> zoneViews = new ArrayList<>();

    public BlocView() { }

    public void addZoneView(ZoneView zoneView) {
        if (zoneView == null || zoneViews.contains(zoneView)) return;
        zoneViews.add(zoneView);
        getChildren().add(zoneView);
    }

    public List<ZoneView> getZoneViews() {
        return zoneViews;
    }
}

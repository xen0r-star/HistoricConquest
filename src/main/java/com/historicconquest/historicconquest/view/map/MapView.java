package com.historicconquest.historicconquest.view.map;

import com.historicconquest.historicconquest.model.map.Zone;
import javafx.scene.Group;

import java.util.Map;

public class MapView {
    private final Group root;
    private final Map<Zone, ZoneView> zoneViewIndex;

    public MapView(Group root, Map<Zone, ZoneView> zoneViewIndex) {
        this.root = root;
        this.zoneViewIndex = zoneViewIndex;
    }

    public Group getRoot() {
        return root;
    }

    public ZoneView getViewFor(Zone zone) {
        return zoneViewIndex.get(zone);
    }
}

package com.historicconquest.historicconquest.view.map;

import com.historicconquest.historicconquest.model.map.Zone;
import javafx.scene.Group;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MapView {
    private final Group root;
    private final List<BlocView> blocs;
    private final Map<Zone, ZoneView> zoneViewIndex;

    public MapView(Group root, List<BlocView> blocs, Map<Zone, ZoneView> zoneViewIndex) {
        this.root = root;
        this.blocs = blocs;
        this.zoneViewIndex = zoneViewIndex;
    }

    public Group getRoot() {
        return root;
    }

    public List<BlocView> getBlocs() {
        return Collections.unmodifiableList(blocs);
    }

    public ZoneView getViewFor(Zone zone) {
        return zoneViewIndex.get(zone);
    }

    public List<ZoneView> toViews(List<Zone> zones) {
        if (zones == null) return List.of();
        return zones.stream()
            .map(zoneViewIndex::get)
            .filter(Objects::nonNull)
            .toList();
    }
}

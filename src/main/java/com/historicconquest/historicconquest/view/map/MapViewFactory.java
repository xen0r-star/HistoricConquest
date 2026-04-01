package com.historicconquest.historicconquest.view.map;

import com.historicconquest.historicconquest.model.map.Bloc;
import com.historicconquest.historicconquest.model.map.WorldMap;
import com.historicconquest.historicconquest.model.map.Zone;
import javafx.scene.Group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MapViewFactory {

    private MapViewFactory() { }

    public static MapView build(WorldMap worldMap, boolean enableHover) {
        Group root = new Group();
        List<BlocView> blocViews = new ArrayList<>();
        Map<Zone, ZoneView> zoneIndex = new HashMap<>();

        for (Bloc bloc : worldMap.getBlocs()) {
            BlocView blocView = new BlocView(bloc);
            for (Zone zone : bloc.getZones()) {
                ZoneView zoneView = new ZoneView(zone);
                if (!enableHover) {
                    zoneView.setBlockHover(true);
                }
                blocView.addZoneView(zoneView);
                zoneIndex.put(zone, zoneView);
            }
            blocViews.add(blocView);
            root.getChildren().add(blocView);
        }

        return new MapView(root, blocViews, zoneIndex);
    }
}

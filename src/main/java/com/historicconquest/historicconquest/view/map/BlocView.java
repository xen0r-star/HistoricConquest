package com.historicconquest.historicconquest.view.map;

import com.historicconquest.historicconquest.model.map.Bloc;
import javafx.scene.Group;

import java.util.ArrayList;
import java.util.List;

public class BlocView extends Group {
    private final Bloc bloc;
    private final List<ZoneView> zoneViews = new ArrayList<>();

    public BlocView(Bloc bloc) {
        this.bloc = bloc;
    }

    public void addZoneView(ZoneView zoneView) {
        if (zoneView == null || zoneViews.contains(zoneView)) return;
        zoneViews.add(zoneView);
        getChildren().add(zoneView);
    }

    public Bloc getBloc() {
        return bloc;
    }

    public List<ZoneView> getZoneViews() {
        return zoneViews;
    }
}

package com.historicconquest.historicconquest.model.game;

import com.historicconquest.historicconquest.model.map.Zone;

public interface GameActionExecutor {
    void handleZoneSelection(Zone targetZone);
}

package com.historicconquest.server.model.specialCard;

import com.historicconquest.server.model.Room;
import com.historicconquest.server.model.player.Player;

import java.util.Map;

public interface SpecialCard {
    String getName();
    String getDescription();
    double getLuck();

    Map<String, Object> apply(Room room, Player target);
}

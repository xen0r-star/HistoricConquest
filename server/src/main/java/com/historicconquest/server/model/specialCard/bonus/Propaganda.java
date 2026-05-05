package com.historicconquest.server.model.specialCard.bonus;

import com.historicconquest.server.model.Room;
import com.historicconquest.server.model.player.Player;
import com.historicconquest.server.model.specialCard.SpecialCard;

import java.util.Map;

public class Propaganda implements SpecialCard {
    @Override
    public String getName() {
        return "Propaganda";
    }

    @Override
    public String getDescription() {
        return "Choose a zone adjacent to yours. The owner loses 2 power points.";
    }

    @Override
    public double getLuck() {
        return 10.0;
    }

    @Override
    public Map<String, Object> apply(Room room, Player target) {
        return null;
    }
}

package com.historicconquest.server.model.specialCard.bonus;

import com.historicconquest.server.model.Room;
import com.historicconquest.server.model.player.Player;
import com.historicconquest.server.model.specialCard.SpecialCard;

import java.util.Map;

public class TacticalGenius implements SpecialCard {
    @Override
    public String getName() {
        return "Tactical Genius";
    }

    @Override
    public String getDescription() {
        return "The power of your next attack is doubled.";
    }

    @Override
    public double getLuck() {
        return 7.0;
    }

    @Override
    public Map<String, Object> apply(Room room, Player target) {
        return null;
    }
}

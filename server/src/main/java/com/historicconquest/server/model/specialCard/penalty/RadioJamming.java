package com.historicconquest.server.model.specialCard.penalty;

import com.historicconquest.server.model.Room;
import com.historicconquest.server.model.player.Player;
import com.historicconquest.server.model.specialCard.SpecialCard;

import java.util.Map;

public class RadioJamming implements SpecialCard {
    @Override
    public String getName() {
        return "Radio Jamming";
    }

    @Override
    public String getDescription() {
        return "For 2 turns, you no longer know the theme of the zones you move into.";
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

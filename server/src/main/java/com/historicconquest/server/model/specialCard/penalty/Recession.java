package com.historicconquest.server.model.specialCard.penalty;

import com.historicconquest.server.model.Room;
import com.historicconquest.server.model.player.Player;
import com.historicconquest.server.model.specialCard.SpecialCard;

import java.util.Map;

public class Recession implements SpecialCard {
    @Override
    public String getName() {
        return "Recession";
    }

    @Override
    public String getDescription() {
        return "You cannot perform the “Increase a zone’s power” action for the next 2 turns.";
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

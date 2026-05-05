package com.historicconquest.server.model.specialCard.bonus;

import com.historicconquest.server.model.Room;
import com.historicconquest.server.model.player.Player;
import com.historicconquest.server.model.specialCard.SpecialCard;

import java.util.Map;

public class ForcedMarch implements SpecialCard {
    @Override
    public String getName() {
        return "Forced March";
    }

    @Override
    public String getDescription() {
        return "Your next movement is facilitated and requires you to answer only a Level 1 question, which will be considered a Level 4 success.";
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

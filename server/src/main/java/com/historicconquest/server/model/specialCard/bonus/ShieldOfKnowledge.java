package com.historicconquest.server.model.specialCard.bonus;

import com.historicconquest.server.model.Room;
import com.historicconquest.server.model.player.Player;
import com.historicconquest.server.model.specialCard.SpecialCard;

import java.util.Map;

public class ShieldOfKnowledge implements SpecialCard {
    @Override
    public String getName() {
        return "Shield of Knowledge";
    }

    @Override
    public String getDescription() {
        return "The next attack suffered by one of your zones has its damage reduced by 2 points.";
    }

    @Override
    public double getLuck() {
        return 12.0;
    }

    @Override
    public Map<String, Object> apply(Room room, Player target) {
        return null;
    }
}

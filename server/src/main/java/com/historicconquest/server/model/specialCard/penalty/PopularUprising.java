package com.historicconquest.server.model.specialCard.penalty;

import com.historicconquest.server.model.Room;
import com.historicconquest.server.model.map.Zone;
import com.historicconquest.server.model.player.Player;
import com.historicconquest.server.model.specialCard.SpecialCard;

import java.util.Comparator;
import java.util.Map;

public class PopularUprising implements SpecialCard {
    @Override
    public String getName() {
        return "Popular Uprising";
    }

    @Override
    public String getDescription() {
        return "The power of your strongest zone is immediately reduced by 2 points.";
    }

    @Override
    public double getLuck() {
        return 12.0;
    }

    @Override
    public Map<String, Object> apply(Room room, Player target) {
        room.getPlayerZones(target.getId()).stream()
            .max(Comparator.comparingInt(Zone::getPower)).ifPresent(
                zone -> zone.setPower(Math.max(0, zone.getPower() - 2))
            );

        return null;
    }
}

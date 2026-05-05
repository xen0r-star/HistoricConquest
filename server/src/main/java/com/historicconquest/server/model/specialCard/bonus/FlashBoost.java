package com.historicconquest.server.model.specialCard.bonus;

import com.historicconquest.server.model.Room;
import com.historicconquest.server.model.map.Zone;
import com.historicconquest.server.model.player.Player;
import com.historicconquest.server.model.specialCard.SpecialCard;

import java.util.List;
import java.util.Map;

public class FlashBoost implements SpecialCard {
    @Override
    public String getName() {
        return "Flash Boost";
    }

    @Override
    public String getDescription() {
        return "Instantly add +2 power points to your most recently acquired zone";
    }

    @Override
    public double getLuck() {
        return 12.0;
    }

    @Override
    public Map<String, Object> apply(Room room, Player target) {
        Zone zoneChange;

        List<Zone> zones = room.getPlayerZones(target.getId());
        Zone zone = zones.stream()
            .skip((int) (Math.random() * zones.size()))
            .findFirst().orElse(null);

        if (zone != null) {
            zone.setPower(zone.getPower() + 2);
            zoneChange = zone;

            return Map.of(
                "zoneChange", zoneChange.getName(),
                "newPower", zoneChange.getPower()
            );
        }

        return null;
    }
}

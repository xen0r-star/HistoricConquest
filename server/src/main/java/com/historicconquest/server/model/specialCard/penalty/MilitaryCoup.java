package com.historicconquest.server.model.specialCard.penalty;

import com.historicconquest.server.model.Room;
import com.historicconquest.server.model.map.Zone;
import com.historicconquest.server.model.player.Player;
import com.historicconquest.server.model.specialCard.SpecialCard;

import java.util.Comparator;
import java.util.Map;

public class MilitaryCoup implements SpecialCard {
    @Override
    public String getName() {
        return "Military Coup";
    }

    @Override
    public String getDescription() {
        return "You immediately lose control of your weakest zone.";
    }

    @Override
    public double getLuck() {
        return 3.0;
    }

    @Override
    public Map<String, Object> apply(Room room, Player target) {
        room.getPlayerZones(target.getId()).stream()
            .min(Comparator.comparingInt(Zone::getPower)).ifPresent(
                zone -> {
                    zone.setPower(0);
                    zone.setNameOwner("Nobody");
                }
            );

        return null;
    }
}

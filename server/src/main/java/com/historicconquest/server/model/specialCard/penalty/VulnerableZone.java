package com.historicconquest.server.model.specialCard.penalty;

import com.historicconquest.server.model.Room;
import com.historicconquest.server.model.map.Zone;
import com.historicconquest.server.model.player.Player;
import com.historicconquest.server.model.questions.TypeThemes;
import com.historicconquest.server.model.specialCard.SpecialCard;

import java.util.List;
import java.util.Map;

public class VulnerableZone implements SpecialCard {
    @Override
    public String getName() {
        return "Vulnerable Zone";
    }

    @Override
    public String getDescription() {
        return "The theme of one of your zones is redrawn at random (which may disrupt your strategy if you had mastered the previous theme).";
    }

    @Override
    public double getLuck() {
        return 7.0;
    }

    @Override
    public Map<String, Object> apply(Room room, Player target) {
        List<Zone> zones = room.getPlayerZones(target.getId());
        Zone zone = zones.stream()
            .skip((int) (Math.random() * zones.size()))
            .findFirst().orElse(null);

        if (zone != null) {
            zone.setThemes(TypeThemes.getRandom());

            return Map.of(
                "zoneChange", zone.getName(),
                "newTheme", zone.getThemes().getLabel()
            );
        }

        return null;
    }
}

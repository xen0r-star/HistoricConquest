package com.historicconquest.historicconquest.model.specialCard.penalty;

import com.historicconquest.historicconquest.model.map.Zone;
import com.historicconquest.historicconquest.model.player.Player;
import com.historicconquest.historicconquest.model.specialCard.SpecialCard;

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
    public void apply(Player target) {
        target.getZones().stream()
            .max(Comparator.comparingInt(Zone::getPower)).ifPresent(
                zone -> zone.setPower(Math.max(0, zone.getPower() - 2))
            );
    }

    @Override
    public void apply(Player target, Map<String, Object> params) {
        apply(target);
    }
}

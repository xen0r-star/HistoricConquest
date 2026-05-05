package com.historicconquest.historicconquest.model.specialCard.penalty;

import com.historicconquest.historicconquest.model.map.Zone;
import com.historicconquest.historicconquest.model.player.Player;
import com.historicconquest.historicconquest.model.specialCard.SpecialCard;

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
    public void apply(Player target) {
        target.getZones().stream()
            .min(Comparator.comparingInt(Zone::getPower)).ifPresent(
                zone -> {
                    zone.setPower(0);
                    zone.setNameOwner("Nobody");
                    zone.setColor(zone.getBaseColor());
                }
            );
    }

    @Override
    public void apply(Player target, Map<String, Object> params) {
        apply(target);
    }
}

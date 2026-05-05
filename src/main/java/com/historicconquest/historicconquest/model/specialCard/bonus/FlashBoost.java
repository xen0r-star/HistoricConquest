package com.historicconquest.historicconquest.model.specialCard.bonus;

import com.historicconquest.historicconquest.model.map.Zone;
import com.historicconquest.historicconquest.model.player.Player;
import com.historicconquest.historicconquest.model.specialCard.SpecialCard;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

public class FlashBoost implements SpecialCard {
    private static final ObjectMapper MAPPER = new ObjectMapper();

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
    public void apply(Player target) {
        if (!target.getZones().isEmpty()) {
            Zone zone = target.getZones().getLast();
            zone.setPower(zone.getPower() + 2);
        }
    }

    @Override
    public void apply(Player target, Map<String, Object> params) {
        if (!target.getZones().isEmpty()) {
            String zoneName = (String) params.get("zoneChange");
            int newPower = MAPPER.convertValue(params.get("power"), Integer.class);

            if (zoneName != null && newPower > 0) {
                target.getZones().stream()
                    .filter(z -> z.getName().equals(zoneName))
                    .findFirst().ifPresent(zone ->
                        zone.setPower(newPower)
                    );
            }
        }
    }
}

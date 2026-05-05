package com.historicconquest.historicconquest.model.specialCard.penalty;

import com.historicconquest.historicconquest.model.player.Player;
import com.historicconquest.historicconquest.model.questions.TypeThemes;
import com.historicconquest.historicconquest.model.specialCard.SpecialCard;

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
    public void apply(Player target) {
        target.getZones().stream()
            .skip((int) (Math.random() * target.getZones().size()))
            .findFirst().ifPresent(zone ->
                zone.setThemes(TypeThemes.getRandom())
            );
    }

    @Override
    public void apply(Player target, Map<String, Object> params) {
        String zoneName = (String) params.get("zoneChange");
        String zoneTheme = (String) params.get("newTheme");

        if (zoneName != null && zoneTheme != null) {
            target.getZones().stream()
                .filter(z -> z.getName().equals(zoneName))
                .findFirst().ifPresent(zone ->
                    zone.setThemes(TypeThemes.fromString(zoneTheme))
                );
        }
    }
}

package com.historicconquest.historicconquest.model.specialCard.bonus;

import com.historicconquest.historicconquest.model.player.Player;
import com.historicconquest.historicconquest.model.specialCard.SpecialCard;

import java.util.Map;

public class Propaganda implements SpecialCard {
    @Override
    public String getName() {
        return "Propaganda";
    }

    @Override
    public String getDescription() {
        return "Choose a zone adjacent to yours. The owner loses 2 power points.";
    }

    @Override
    public double getLuck() {
        return 10.0;
    }

    @Override
    public void apply(Player target) {

    }

    @Override
    public void apply(Player target, Map<String, Object> params) {
        apply(target);
    }
}

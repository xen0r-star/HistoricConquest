package com.historicconquest.historicconquest.model.specialCard.bonus;

import com.historicconquest.historicconquest.model.player.Player;
import com.historicconquest.historicconquest.model.specialCard.SpecialCard;

import java.util.Map;

public class TacticalGenius implements SpecialCard {
    @Override
    public String getName() {
        return "Tactical Genius";
    }

    @Override
    public String getDescription() {
        return "The power of your next attack is doubled.";
    }

    @Override
    public double getLuck() {
        return 7.0;
    }

    @Override
    public void apply(Player target) {

    }

    @Override
    public void apply(Player target, Map<String, Object> params) {
        apply(target);
    }
}

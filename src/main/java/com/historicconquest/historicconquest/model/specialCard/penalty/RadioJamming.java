package com.historicconquest.historicconquest.model.specialCard.penalty;

import com.historicconquest.historicconquest.model.player.Player;
import com.historicconquest.historicconquest.model.specialCard.SpecialCard;

import java.util.Map;

public class RadioJamming implements SpecialCard {
    @Override
    public String getName() {
        return "Radio Jamming";
    }

    @Override
    public String getDescription() {
        return "For 2 turns, you no longer know the theme of the zones you move into.";
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

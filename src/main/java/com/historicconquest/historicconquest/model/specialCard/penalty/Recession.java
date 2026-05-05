package com.historicconquest.historicconquest.model.specialCard.penalty;

import com.historicconquest.historicconquest.model.player.Player;
import com.historicconquest.historicconquest.model.specialCard.SpecialCard;

import java.util.Map;

public class Recession implements SpecialCard {
    @Override
    public String getName() {
        return "Recession";
    }

    @Override
    public String getDescription() {
        return "You cannot perform the “Increase a zone’s power” action for the next 2 turns.";
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

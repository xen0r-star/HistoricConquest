package com.historicconquest.historicconquest.model.specialCard.bonus;

import com.historicconquest.historicconquest.model.player.Player;
import com.historicconquest.historicconquest.model.specialCard.SpecialCard;

import java.util.Map;

public class ForcedMarch implements SpecialCard {
    @Override
    public String getName() {
        return "Forced March";
    }

    @Override
    public String getDescription() {
        return "Your next movement is facilitated and requires you to answer only a Level 1 question, which will be considered a Level 4 success.";
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

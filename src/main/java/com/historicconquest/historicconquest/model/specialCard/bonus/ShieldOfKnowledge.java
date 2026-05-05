package com.historicconquest.historicconquest.model.specialCard.bonus;

import com.historicconquest.historicconquest.model.player.Player;
import com.historicconquest.historicconquest.model.specialCard.SpecialCard;

import java.util.Map;

public class ShieldOfKnowledge implements SpecialCard {
    @Override
    public String getName() {
        return "Shield of Knowledge";
    }

    @Override
    public String getDescription() {
        return "The next attack suffered by one of your zones has its damage reduced by 2 points.";
    }

    @Override
    public double getLuck() {
        return 12.0;
    }

    @Override
    public void apply(Player target) {

    }

    @Override
    public void apply(Player target, Map<String, Object> params) {
        apply(target);
    }
}

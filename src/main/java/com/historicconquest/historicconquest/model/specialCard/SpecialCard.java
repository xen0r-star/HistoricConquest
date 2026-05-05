package com.historicconquest.historicconquest.model.specialCard;

import com.historicconquest.historicconquest.model.player.Player;

import java.util.Map;

public interface SpecialCard {
    String getName();
    String getDescription();
    double getLuck();

    void apply(Player target);
    void apply(Player target, Map<String, Object> params);
}

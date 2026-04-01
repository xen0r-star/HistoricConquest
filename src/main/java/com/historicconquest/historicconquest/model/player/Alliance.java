package com.historicconquest.historicconquest.model.player;

import java.util.List;

public class Alliance {
    private static final int MAX_CAPACITY = 2; // Game.getPlayers().lenth() / 2

    private String name;
    private List<Player> members;

    public Alliance(String name) {
        this.name = name;
    }


    public boolean addMember(Player player) {
        if (members.size() < MAX_CAPACITY && !members.contains(player)) {
            members.add(player);
            player.setAlliance(this);
            return true;
        }

        return false;
    }
}

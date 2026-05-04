package com.historicconquest.historicconquest.model.player;

import com.historicconquest.historicconquest.model.map.Zone;
import javafx.scene.Node;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private final int id;
    private final String pseudo;
    private final PlayerColor color;
    private final List<Zone> zones;
    private int consecutiveSuccesses;
    private int consecutiveFailures;
    private Zone currentZone;
    private Node pawnNode;
    private Player ally;
    private Player pendingAllianceRequest;
    private Color currentAllianceColor;

    public Player(int id, String pseudo, PlayerColor color) {
        this.id = id;
        this.pseudo = pseudo;
        this.color = color;
        this.consecutiveSuccesses = 0;
        this.consecutiveFailures = 0;

        zones = new ArrayList<>();
    }

    public Player getPendingAllianceRequest() {
        return pendingAllianceRequest;
    }

    public void setPendingAllianceRequest(Player pendingAllianceRequest) {
        this.pendingAllianceRequest = pendingAllianceRequest;
    }

    public Color getCurrentAllianceColor() {
        return currentAllianceColor;
    }

    public void setCurrentAllianceColor(Color currentAllianceColor) {
        this.currentAllianceColor = currentAllianceColor;
    }

    public boolean hasPendingRequest() {
        return pendingAllianceRequest != null;
    }

    public void clearPendingRequest() {
        this.pendingAllianceRequest = null;
    }

    public boolean hasAlly() {
        return ally != null;
    }

    public Player getAlly() {
        return ally;
    }

    public void setAlly(Player ally) {
        this.ally = ally;
    }

    public int getConsecutiveSuccesses() {
        return consecutiveSuccesses;
    }

    public void setConsecutiveSuccesses(int consecutiveSuccesses) {
        this.consecutiveSuccesses = consecutiveSuccesses;
    }

    public int getConsecutiveFailures() {
        return consecutiveFailures;
    }

    public void setConsecutiveFailures(int consecutiveFailures) {
        this.consecutiveFailures = consecutiveFailures;
    }


    public String getPseudo() {
        return pseudo;
    }

    public int getId() {
        return id;
    }

    public PlayerColor getColor() {
        return color;
    }



    public Zone getCurrentZone() {
        return currentZone;
    }

    public void setCurrentZone(Zone currentZone) {
        this.currentZone = currentZone;
    }

    public Node getPawnNode() {
        return pawnNode;
    }

    public void setPawnNode(Node pawnNode) {
        this.pawnNode = pawnNode;
    }


    public void addZone(Zone zone) {
       zones.add(zone);
    }


    public List<Zone> getZones() {
        return zones;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Player p) {
            return p.getPseudo().equalsIgnoreCase(this.pseudo);
        }
        return false;
    }
}

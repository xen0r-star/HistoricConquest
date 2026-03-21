package com.historicconquest.historicconquest.network;

public class RoomPlayer {
    private String id;
    private String name;
    private String color;
    private final boolean isRobot;
    private int ping;
    private String status;
    private final boolean isRemovable;

    public RoomPlayer(String id, String name, String color, boolean isRobot, int ping, String status, boolean isRemovable) {
        this.id = id;
        this.name = name;
        this.isRobot = isRobot;
        this.ping = ping;
        this.status = status;
        this.isRemovable = isRemovable;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isRobot() {
        return isRobot;
    }

    public int getPing() {
        return ping;
    }

    public void setPing(int ping) {
        this.ping = ping;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isRemovable() {
        return isRemovable;
    }
}

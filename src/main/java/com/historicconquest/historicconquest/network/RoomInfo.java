package com.historicconquest.historicconquest.network;

public enum RoomInfo {
    PLAYER_JOIN("PLAYER_JOIN"),
    PLAYER_QUIT("PLAYER_QUIT"),
    PLAYER_COLOR_CHANGE("PLAYER_COLOR_CHANGE"),
    PLAYER_PSEUDO_CHANGE("PLAYER_PSEUDO_CHANGE"),
    PLAYER_PINGS("PLAYER_PINGS"),

    ROOM_DELETED("ROOM_DELETED");

    private final String label;

    RoomInfo(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static RoomInfo getRoomInfo(String label) {
        if (label == null) return null;

        for (RoomInfo info : RoomInfo.values()) {
            if (info.label.equalsIgnoreCase(label)) {
                return info;
            }
        }

        return null;
    }
}

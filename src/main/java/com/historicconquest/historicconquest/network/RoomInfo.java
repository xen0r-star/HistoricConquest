package com.historicconquest.historicconquest.network;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

public enum RoomInfo {
    PLAYER_JOIN {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            if (listener != null) {
                listener.onPlayerJoin();
            }
        }
    },

    PLAYER_QUIT {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            if (listener != null) {
                listener.onPlayerQuit();
            }
        }
    },

    PLAYER_COLOR_CHANGE {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            if (listener != null) {
                listener.onPlayerColorChange();
            }
        }
    },

    PLAYER_PSEUDO_CHANGE {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            if (listener != null) {
                listener.onPlayerPseudoChange();
            }
        }
    },

    PLAYER_PINGS {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            if (listener != null) {
                listener.onPlayerPings();
            }
        }
    },

    ROOM_DELETED {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            if (listener != null) {
                listener.onRoomDeleted();
            }
        }
    },

    UNKNOWN {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            System.out.println("Unknown event: " + node);
        }
    };

    public abstract void handle(JsonNode node, RoomEventListener listener);


    private static final Map<String, RoomInfo> MAP = new HashMap<>();

    static {
        for (RoomInfo info : values()) {
            MAP.put(info.name(), info);
        }
    }

    public static RoomInfo from(String type) {
        if (type == null) return UNKNOWN;
        return MAP.getOrDefault(type.toUpperCase(), UNKNOWN);
    }
}
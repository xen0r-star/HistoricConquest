package com.historicconquest.historicconquest.network.event;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.historicconquest.historicconquest.network.model.NetworkPlayer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum RoomInfo {
    PLAYER_JOIN {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            JsonNode playerNode = node.get("player");
            if (playerNode == null || playerNode.isNull()) return;

            try {
                NetworkPlayer newPlayer = MAPPER.treeToValue(playerNode, NetworkPlayer.class);

                notifyIfPresent(listener, l -> l.onPlayerJoin(
                    newPlayer
                ));

            } catch (Exception e) {
                throw new RuntimeException("Unable to parse joined player payload", e);
            }
        }
    },

    PLAYER_QUIT {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            notifyIfPresent(listener, l -> l.onPlayerQuit(
                getText(node, "playerId")
            ));
        }
    },

    PLAYER_KICK {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            notifyIfPresent(listener, l -> l.onPlayerKick(
                getText(node, "playerId")
            ));
        }
    },

    PLAYER_COLOR_CHANGE {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            notifyIfPresent(listener, l -> l.onPlayerColorChange(
                getText(node, "playerId"),
                getText(node, "data")
            ));
        }
    },

    PLAYER_PSEUDO_CHANGE {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            notifyIfPresent(listener, l -> l.onPlayerPseudoChange(
                getText(node, "playerId"),
                getText(node, "data")
            ));
        }
    },

    PLAYER_STATUS_CHANGE {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            notifyIfPresent(listener, l -> l.onPlayerStatusChange(
                getText(node, "playerId"),
                getText(node, "data")
            ));
        }
    },

    PLAYER_PINGS {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            JsonNode nodePings = node.get("pings");
            if (nodePings == null || nodePings.isNull()) return;

            // Convert JsonNode to Map<String, Integer>
            Map<String, Integer> pings = MAPPER.convertValue(nodePings, new TypeReference<>() { });

            notifyIfPresent(listener, l -> l.onPlayerPings(pings));
        }
    },

    ROOM_DELETED {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            notifyIfPresent(listener, RoomEventListener::onRoomDeleted);
        }
    },

    UNKNOWN {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            System.out.println("Unknown event: " + node);
        }
    };




    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Map<String, RoomInfo> MAP;

    public abstract void handle(JsonNode node, RoomEventListener listener);


    static {
        Map<String, RoomInfo> map = new HashMap<>();
        for (RoomInfo info : values()) {
            map.put(info.name(), info);
        }
        MAP = Collections.unmodifiableMap(map);
    }

    public static RoomInfo from(String type) {
        if (type == null) return UNKNOWN;
        return MAP.getOrDefault(type.toUpperCase(), UNKNOWN);
    }

    private static String getText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? "" : value.asText();
    }

    private static void notifyIfPresent(RoomEventListener listener, java.util.function.Consumer<RoomEventListener> callback) {
        if (listener != null) {
            callback.accept(listener);
        }
    }
}
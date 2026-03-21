package com.historicconquest.historicconquest.network;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public enum RoomInfo {
    PLAYER_JOIN {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode playerNode = node.get("player");

            try {
                ApiService.Player newPlayer = mapper.treeToValue(playerNode, ApiService.Player.class);

                if (listener != null) {
                    listener.onPlayerJoin(newPlayer);
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    },

    PLAYER_QUIT {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            String playerId = node.get("playerId").asText();

            if (listener != null) {
                listener.onPlayerQuit(playerId);
            }
        }
    },

    PLAYER_KICK {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            String playerId = node.get("playerId").asText();

            if (listener != null) {
                listener.onPlayerKick(playerId);
            }
        }
    },

    PLAYER_COLOR_CHANGE {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            String playerId = node.get("playerId").asText();
            String newColor = node.get("data").asText();

            if (listener != null) {
                listener.onPlayerColorChange(playerId, newColor);
            }
        }
    },

    PLAYER_PSEUDO_CHANGE {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            String playerId = node.get("playerId").asText();
            String newPseudo = node.get("data").asText();

            if (listener != null) {
                listener.onPlayerPseudoChange(playerId, newPseudo);
            }
        }
    },

    PLAYER_STATUS_CHANGE {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            String playerId = node.get("playerId").asText();
            String newStatus = node.get("data").asText();

            if (listener != null) {
                listener.onPlayerStatusChange(playerId, newStatus);
            }
        }
    },

    PLAYER_PINGS {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode nodePings = node.get("pings");

            Map<String, Integer> pings = mapper.convertValue(
                nodePings,
                new TypeReference<>() { }
            );

            if (listener != null) {
                listener.onPlayerPings(pings);
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
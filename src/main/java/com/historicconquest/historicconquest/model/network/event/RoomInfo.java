package com.historicconquest.historicconquest.model.network.event;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.historicconquest.historicconquest.model.network.model.NetworkPlayer;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

    GAME_COUNTDOWN_STARTED {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            JsonNode secondsNode = node.get("seconds");
            JsonNode startAtNode = node.get("startAt");

            int seconds = secondsNode == null || secondsNode.isNull() ? 5 : secondsNode.asInt(5);
            long startAt = startAtNode == null || startAtNode.isNull() ? 0L : startAtNode.asLong(0L);

            notifyIfPresent(listener, l -> l.onGameCountdownStarted(seconds, startAt));
        }
    },

    ZONE_SELECTION_STARTED {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            JsonNode secondsNode = node.get("seconds");
            JsonNode startAtNode = node.get("startAt");
            JsonNode selectedZonesNode = node.get("selectedZones");

            int seconds = secondsNode == null || secondsNode.isNull() ? 30 : secondsNode.asInt(30);
            long startAt = startAtNode == null || startAtNode.isNull() ? 0L : startAtNode.asLong(0L);
            Map<String, String> selectedZones = readStringMap(selectedZonesNode);

            notifyIfPresent(listener, l -> l.onZoneSelectionStarted(seconds, startAt, selectedZones));
        }
    },

    ZONE_SELECTION_UPDATED {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            notifyIfPresent(listener, l -> l.onZoneSelectionUpdated(readStringMap(node.get("selectedZones"))));
        }
    },

    GAME_START_CANCELLED {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            notifyIfPresent(listener, l -> l.onGameStartCancelled(getText(node, "reason")));
        }
    },

    GAME_STARTED {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            notifyIfPresent(listener, l -> l.onGameStarted(
                readStringMap(node.get("selectedZones")),
                readStringList(node.get("turnOrder")),
                getText(node, "currentPlayerId"),
                readStringMap(node.get("listThemeZone"))
            ));
        }
    },



    GAME_ACTION {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            notifyIfPresent(listener, l -> l.onGameAction(
                getText(node, "action"),
                getText(node, "playerId"),
                getText(node, "zone"),
                getInteger(node, "difficulty"),
                getBoolean(node)
            ));
        }
    },

    ACTION_SELECTED {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            notifyIfPresent(listener, l -> l.onActionSelected(
                getText(node, "action"),
                getText(node, "playerId"),
                getText(node, "zone"),
                getInteger(node, "difficulty")
            ));
        }
    },

    TURN_CHANGED {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            notifyIfPresent(listener, l -> l.onTurnChanged(
                getText(node, "currentPlayerId"),
                getInteger(node, "currentPlayerIndex")
            ));
        }
    },

    ANSWER_RESULT {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            notifyIfPresent(listener, l -> l.onAnswerResult(
                getText(node, "playerId"),
                getBoolean(node),
                getInteger(node, "difficulty")
            ));
        }
    },

    BONUS_MALUS {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            notifyIfPresent(listener, l -> l.onBonusMalus(
                getText(node, "playerId"),
                getText(node, "kind"),
                getText(node, "nameKind"),
                readMap(node, "resultSpecialCard")
            ));
        }
    },

    GAME_WON {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            notifyIfPresent(listener, l -> l.onGameWon(
                getText(node, "winnerName")
            ));
        }
    },

    COALITION_ACCEPTED {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            notifyIfPresent(listener, l -> l.onCoalitionAccepted(
                getText(node, "playerAId"),
                getText(node, "playerBId"),
                getText(node, "allianceColor")
            ));
        }
    },

    COALITION_DECLINED {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            notifyIfPresent(listener, l -> l.onCoalitionDeclined(
                getText(node, "requesterId"),
                getText(node, "targetId")
            ));
        }
    },

    COALITION_BROKEN {
        @Override
        public void handle(JsonNode node, RoomEventListener listener) {
            notifyIfPresent(listener, l -> l.onCoalitionBroken(
                getText(node, "playerAId"),
                getText(node, "playerBId")
            ));
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
        return value == null || value.isNull() ? "" : value.asString();
    }

    private static Map<String, String> readStringMap(JsonNode node) {
        if (node == null || node.isNull()) {
            return Map.of();
        }

        try {
            return MAPPER.convertValue(node, new TypeReference<>() { });

        } catch (Exception e) {
            return Map.of();
        }
    }

    private static Map<String, Object> readMap(JsonNode node, String field) {
        JsonNode value = node.get(field);

        if (value == null || value.isNull() || !value.isObject()) {
            return Map.of();
        }

        try {
            return MAPPER.convertValue(value, new TypeReference<>() {});

        } catch (Exception e) {
            return Map.of();
        }
    }


    private static List<String> readStringList(JsonNode node) {
        if (node == null || node.isNull()) {
            return List.of();
        }

        try {
            return MAPPER.convertValue(node, new TypeReference<>() { });

        } catch (Exception e) {
            return List.of();
        }
    }

    private static Integer getInteger(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asInt();
    }

    private static Boolean getBoolean(JsonNode node) {
        JsonNode value = node.get("correct");
        return value == null || value.isNull() ? null : value.asBoolean();
    }

    private static void notifyIfPresent(RoomEventListener listener, java.util.function.Consumer<RoomEventListener> callback) {
        if (listener != null) {
            callback.accept(listener);
        }
    }
}
package com.historicconquest.server.service;

import com.historicconquest.server.model.Player;
import com.historicconquest.server.model.Room;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RoomService {
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    public Room createRoom() {
        String code = generateCode();
        Room room = new Room(code);
        rooms.put(code, room);

        return room;
    }

    public void deleteRoom(String code) {
        rooms.remove(code);
    }


    public void addHost(String roomCode, Player player) throws Exception {
        Room room = rooms.get(roomCode);
        if (room != null) {
            room.addPlayer(player);
            room.setHostId(player.getId());
        }
    }

    public void addPlayer(String roomCode, Player player) throws Exception {
        Room room = rooms.get(roomCode);
        if (room != null) {
            room.addPlayer(player);
        }
    }

    public void removePlayer(String roomCode, String playerId) {
        Room room = rooms.get(roomCode);
        if (room == null) return;

        room.removePlayer(playerId);

        if (room.getPlayers().isEmpty()) {
            rooms.remove(roomCode);
            System.out.println("Room " + roomCode + " deleted because it is empty.");
        }
    }


    public Room getRoom(String code) {
        return rooms.get(code);
    }

    public Collection<Player> getPlayers(String roomCode) throws Exception {
        Room room = rooms.get(roomCode);
        if (room == null) {
            throw new Exception("Room not found");
        }

        return room.getPlayers();
    }

    public boolean pseudoIsUsed(String roomCode, String pseudo, String excludedPlayerId) {
        Room room = rooms.get(roomCode);
        if (room == null) {
            throw new IllegalArgumentException("Room not found");
        }

        String normalizedPseudo = pseudo == null ? "" : pseudo.trim();
        if (normalizedPseudo.isBlank()) {
            return false;
        }

        for (Player player : room.getPlayers()) {
            boolean isCurrentPlayer = excludedPlayerId != null && excludedPlayerId.equals(player.getId());
            if (isCurrentPlayer) {
                continue;
            }

            if (player.getPseudo() != null && player.getPseudo().equalsIgnoreCase(normalizedPseudo)) {
                return true;
            }
        }

        return false;
    }

    public Collection<String> getUsedColors(String roomCode, String excludedPlayerId) {
        Room room = rooms.get(roomCode);
        if (room == null) {
            throw new IllegalArgumentException("Room not found");
        }

        return room.getPlayers().stream()
            .filter(player -> excludedPlayerId == null || !excludedPlayerId.equals(player.getId()))
            .map(Player::getColor)
            .filter(color -> color != null && !color.isBlank())
            .toList();
    }

    private String generateCode() {
        Random random = new Random();
        int number = random.nextInt(900000) + 100000;

        return String.valueOf(number);
    }

    public Collection<Room> getAllRooms() {
        return rooms.values();
    }
}

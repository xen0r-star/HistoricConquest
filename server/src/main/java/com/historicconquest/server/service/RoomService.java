package com.historicconquest.server.service;

import com.historicconquest.server.model.player.Player;
import com.historicconquest.server.model.Room;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class RoomService {
    private static final List<String> AVAILABLE_COLORS = List.of(
        "RED", "ORANGE", "YELLOW", "LIME", "GREEN",
        "LIGHT_BLUE", "BLUE", "PINK", "PURPLE"
    );

    private static final List<String> AVAILABLE_ZONES = List.of(
        "Australasia", "Southeast Asia", "Mainland Southeast Asia", "Indian subcontinent", "North-East Archipelagos",
        "East Asia", "Central Asia", "Eastern Middle East", "Arabian Peninsula", "Middle East",
        "Mercosur", "Great Chaco", "Southern Cone", "South Central", "South America North",
        "Central America and the Caribbean", "Mexico", "United States", "Canada", "Alaska",
        "Southern Africa", "South-Central Africa", "Congo Basin", "East Africa", "Horn of Africa",
        "Gulf of Guinea", "West Africa", "Sahel", "Nile Valley", "Maghreb",
        "Russia", "Eurasia & Caucasus", "Eastern Europe", "Balkans & Greece", "Central Europe - Alpine",
        "Iberian Peninsula", "Western Europe", "Scandinavia", "British Isles - Iceland", "Greenland"
    );

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
            assignRandomAvailableColor(room, player);
            room.addPlayer(player);
            room.setHostId(player.getId());
        }
    }

    public void addPlayer(String roomCode, Player player) throws Exception {
        Room room = rooms.get(roomCode);
        if (room != null) {
            assignRandomAvailableColor(room, player);
            room.addPlayer(player);
        }
    }

    private void assignRandomAvailableColor(Room room, Player player) {
        if (player.getColor() != null && !player.getColor().isBlank()) {
            return;
        }

        Set<String> usedColors = room.getPlayers().stream()
            .map(Player::getColor)
            .filter(color -> color != null && !color.isBlank())
            .map(String::toUpperCase)
            .collect(Collectors.toSet());

        List<String> freeColors = new ArrayList<>();
        for (String color : AVAILABLE_COLORS) {
            if (!usedColors.contains(color)) {
                freeColors.add(color);
            }
        }

        if (freeColors.isEmpty()) {
            player.setColor(AVAILABLE_COLORS.get(new Random().nextInt(AVAILABLE_COLORS.size())));
            return;
        }

        player.setColor(freeColors.get(new Random().nextInt(freeColors.size())));
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

    public boolean canStartGame(String roomCode) {
        Room room = rooms.get(roomCode);
        return room != null && room.canStartGame();
    }

    public long startGame(String roomCode) throws Exception {
        Room room = rooms.get(roomCode);
        if (room == null) {
            throw new Exception("Room not found");
        }

        if (room.isGameStarting()) {
            throw new Exception("Game start is already in progress");
        }

        if (!room.hasLaunchConditions()) {
            throw new Exception("Room must contain 4 players and everyone except the host must be ready");
        }

        long startAt = System.currentTimeMillis() + 5000;
        room.markGameStarting(startAt);
        return startAt;
    }

    public ZoneSelectionStart startZoneSelection(String roomCode) throws Exception {
        Room room = rooms.get(roomCode);
        if (room == null) {
            throw new Exception("Room not found");
        }

        if (room.isGameStarted()) {
            throw new Exception("Game is already started");
        }

        if (room.isZoneSelectionStarted()) {
            throw new Exception("Zone selection is already in progress");
        }

        long startAt = System.currentTimeMillis() + 30_000L;
        room.markZoneSelectionStarted(startAt);

        autoAssignBotZones(room);
        return new ZoneSelectionStart(startAt, room.getSelectedZones());
    }

    public ZoneSelectionUpdate selectZone(String roomCode, String playerId, String zoneName) throws Exception {
        Room room = rooms.get(roomCode);
        if (room == null) {
            throw new Exception("Room not found");
        }

        if (!room.isZoneSelectionStarted()) {
            throw new Exception("Zone selection is not active");
        }

        if (room.getPlayerById(playerId) == null) {
            throw new Exception("Player not found in room");
        }

        String normalizedZone = normalizeZoneName(zoneName);
        if (normalizedZone == null) {
            throw new Exception("Unknown zone");
        }

        boolean accepted = room.selectZone(playerId, normalizedZone);
        if (!accepted) {
            throw new Exception("This zone is already taken");
        }

        return new ZoneSelectionUpdate(room.getSelectedZones(), room.areAllPlayersAssigned());
    }

    public ZoneSelectionUpdate completeZoneSelection(String roomCode) throws Exception {
        Room room = rooms.get(roomCode);
        if (room == null) {
            throw new Exception("Room not found");
        }

        if (!room.isZoneSelectionStarted() && !room.isGameStarted()) {
            throw new Exception("Zone selection is not active");
        }

        if (!room.isGameStarted()) {
            fillMissingZones(room);
            room.markGameStarted();
        }

        return new ZoneSelectionUpdate(room.getSelectedZones(), true);
    }

    public boolean stillCanStartGame(String roomCode) {
        Room room = rooms.get(roomCode);
        return room != null && room.hasLaunchConditions();
    }

    public void cancelGameStart(String roomCode) {
        Room room = rooms.get(roomCode);
        if (room != null) {
            room.cancelGameStarting();
        }
    }

    public void finishGameStart(String roomCode) {
        Room room = rooms.get(roomCode);
        if (room != null) {
            room.markGameStarted();
        }
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

    private void autoAssignBotZones(Room room) throws Exception {
        Set<String> alreadySelected = room.getSelectedZones().values().stream()
            .filter(zone -> zone != null && !zone.isBlank())
            .map(String::toUpperCase)
            .collect(Collectors.toSet());

        for (Player player : room.getPlayers()) {
            if (!(Player.Type.Bot == player.getType())) {
                continue;
            }

            if (room.getSelectedZones().containsKey(player.getId())) {
                continue;
            }

            String freeZone = pickFreeZone(alreadySelected);
            if (freeZone == null) {
                throw new Exception("Not enough free zones for every player");
            }

            room.selectZone(player.getId(), freeZone);
            alreadySelected.add(freeZone.toUpperCase());
        }
    }

    private void fillMissingZones(Room room) throws Exception {
        Set<String> occupied = room.getSelectedZones().values().stream()
            .filter(zone -> zone != null && !zone.isBlank())
            .map(String::toUpperCase)
            .collect(Collectors.toSet());

        for (Player player : room.getPlayers()) {
            if (room.getSelectedZones().containsKey(player.getId())) {
                continue;
            }

            String freeZone = pickFreeZone(occupied);
            if (freeZone == null) {
                throw new Exception("Not enough free zones for every player");
            }

            room.selectZone(player.getId(), freeZone);
            occupied.add(freeZone.toUpperCase());
        }
    }

    private String pickFreeZone(Set<String> occupiedZones) {
        List<String> freeZones = AVAILABLE_ZONES.stream()
            .filter(zone -> !occupiedZones.contains(zone.toUpperCase()))
            .toList();

        if (freeZones.isEmpty()) {
            return null;
        }

        return freeZones.get(new Random().nextInt(freeZones.size()));
    }

    private String normalizeZoneName(String zoneName) {
        if (zoneName == null || zoneName.isBlank()) {
            return null;
        }

        for (String availableZone : AVAILABLE_ZONES) {
            if (availableZone.equalsIgnoreCase(zoneName.trim())) {
                return availableZone;
            }
        }

        return null;
    }

    private String generateCode() {
        Random random = new Random();
        int number = random.nextInt(900000) + 100000;

        return String.valueOf(number);
    }

    public Collection<Room> getAllRooms() {
        return rooms.values();
    }

    public record ZoneSelectionStart(long startAt, Map<String, String> selectedZones) {}

    public record ZoneSelectionUpdate(Map<String, String> selectedZones, boolean completed) {}
}

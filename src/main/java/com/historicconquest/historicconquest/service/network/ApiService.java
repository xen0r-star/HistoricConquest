package com.historicconquest.historicconquest.service.network;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.databind.ObjectMapper;
import com.historicconquest.historicconquest.model.network.model.ErrorRequest;
import com.historicconquest.historicconquest.model.network.model.NetworkPlayer;
import javafx.application.Platform;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ApiService {
    private static final String API_BASE_URL = "http://51.38.227.202:8084/api";

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();




    public static boolean serverIsUp() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/health"))
                .GET()
                .build();

            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            return response.statusCode() == 200;

        } catch (Exception e) {
            return false;
        }
    }


    public static HttpRequest createRoom(String playerName) {
        playerName = URLEncoder.encode(playerName, StandardCharsets.UTF_8);
        return buildPost("/gameroom/create?playerName=%s".formatted(playerName));
    }

    public static HttpRequest joinRoom(String roomCode) {
        roomCode = URLEncoder.encode(roomCode, StandardCharsets.UTF_8);
        return buildPost("/gameroom/join?roomCode=%s".formatted(roomCode));
    }

    public static HttpRequest pseudoIsUsed(String token, String pseudo) {
        String encodedPseudo = URLEncoder.encode(pseudo, StandardCharsets.UTF_8);
        return buildPost("/gameroom/pseudo/isUsed?pseudo=%s".formatted(encodedPseudo), token);
    }

    public static HttpRequest getInfoRoomForReconnection(String token) {
        return buildPost("/gameroom/reconnection", token);
    }

    public static HttpRequest getUsedColors(String token) {
        return buildGet("/gameroom/colors/used", token);
    }

    public static HttpRequest getGameStartStatus(String token) {
        return buildGet("/gameroom/start/status", token);
    }


    private static HttpRequest buildGet(String endpoint, String token) {
        return HttpRequest.newBuilder()
            .uri(URI.create(API_BASE_URL + endpoint))
            .header("Authorization", "Bearer " + token)
            .GET()
            .build();
    }

    private static HttpRequest buildPost(String endpoint) {
        return HttpRequest.newBuilder()
            .uri(URI.create(API_BASE_URL + endpoint))
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();
    }

    private static HttpRequest buildPost(String endpoint, String token) {
        return HttpRequest.newBuilder()
            .uri(URI.create(API_BASE_URL + endpoint))
            .header("Authorization", "Bearer " + token)
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();
    }


    public static <T> void request(HttpRequest request, Class<T> type, Consumer<T> callback) {
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
              .thenApply(HttpResponse::body)
              .thenApply(json -> {
                  try {
                      return mapper.readValue(json, type);
                  } catch (Exception e) {
                      System.err.println(e.getMessage());
                      throw new RuntimeException(e);
                  }

              })
              .thenAccept(result -> Platform.runLater(() -> callback.accept(result)))
              .exceptionally(error -> {
                  System.err.println("HTTP request failed: " + error.getMessage());
                  return null;
              });
    }



    // RECORD ------------------------------------------------------------
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CreateRoomResponse(
        String roomCode,
        String token,
        String pseudo,
        String color,

        ErrorRequest error
    ) {}


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record JoinRoomResponse(
        String token,
        String pseudo,
        String color,
        Collection<NetworkPlayer> players,

        ErrorRequest error
    ) {}


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GetPlayersRoomResponse(
        Collection<NetworkPlayer> players,

        ErrorRequest error
    ) {}


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PseudoIsUsedResponse(
            Boolean isUsed,

            ErrorRequest error
    ) {}


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UsedColorsResponse(
            Collection<String> colors,

            ErrorRequest error
    ) {}


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GameStartStatusResponse(
        Boolean canStart,
        Boolean isStarting,
        Boolean isSelecting,
        Integer playerCount,
        Integer requiredPlayers,

        ErrorRequest error
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ReconnectionRoomResponse(
        String pseudo,
        String color,
        int consecutiveSuccesses,
        int consecutiveFailures,
        Collection<NetworkPlayer> players,

        String currentPhase, // IN_PROGRESS, STARTING, ZONE_SELECTION, LOBBY
        Map<String, String> selectedZones,
        String currentPlayerId,
        List<String> turnOrder,
        String pendingAction,

        ErrorRequest error
    ) {}
}
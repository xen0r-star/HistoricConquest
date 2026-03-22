package com.historicconquest.historicconquest.network.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.historicconquest.historicconquest.network.model.NetworkPlayer;
import javafx.application.Platform;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.function.Consumer;

public class ApiService {
    private static final String API_BASE_URL = "http://localhost:8080/api";

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();



    public static HttpRequest checkRoom(String roomCode) {
        return buildGet("/gameroom/check?roomCode=%s".formatted(roomCode));
    }

    public static HttpRequest createRoom(String playerName) {
        return buildPost("/gameroom/create?playerName=%s".formatted(playerName));
    }

    public static HttpRequest joinRoom(String roomCode, String playerName) {
        return buildPost("/gameroom/join?roomCode=%s&playerName=%s".formatted(roomCode, playerName));
    }



    private static HttpRequest buildGet(String endpoint) {
        return HttpRequest.newBuilder()
            .uri(URI.create(API_BASE_URL + endpoint))
            .GET()
            .build();
    }

    private static HttpRequest buildPost(String endpoint) {
        return HttpRequest.newBuilder()
            .uri(URI.create(API_BASE_URL + endpoint))
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
    public record CheckRoomResponse(
        boolean exists
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CreateRoomResponse(
        String roomCode,
        String token
    ) {}


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record JoinRoomResponse(
        String token,
        Collection<NetworkPlayer> players
    ) {}
}
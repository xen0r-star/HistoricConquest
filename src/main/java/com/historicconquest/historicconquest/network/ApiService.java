package com.historicconquest.historicconquest.network;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.function.Consumer;

public class ApiService {
    private static final String API_LINK = "http://localhost:8080/api";

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();


    public record CheckRoomResponse(
        boolean exists
    ) {}

    public record CreateRoomResponse(
        String roomCode,
        String id,
        String token
    ) {}


    public record Player(
        String id,
        String pseudo,
        String color,
        String type,
        int ping
    ) {}

    public record JoinRoomResponse(
        String id,
        String token,
        Collection<Player> players
    ) {}



    public static HttpRequest checkRoom(String roomCode) {
        return HttpRequest.newBuilder()
            .uri(URI.create(API_LINK +
                "/gameroom/check?roomCode=%s".formatted(roomCode)
            ))
            .GET()
            .build();
    }

    public static HttpRequest createRoom(String playerName) {
        return HttpRequest.newBuilder()
            .uri(URI.create(API_LINK +
                "/gameroom/create?playerName=%s".formatted(playerName)
            ))
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();
    }

    public static HttpRequest joinRoom(String roomCode, String playerName) {
        return HttpRequest.newBuilder()
            .uri(URI.create(API_LINK +
                "/gameroom/join?roomCode=%s&playerName=%s".formatted(roomCode, playerName)
            ))
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
              .thenAccept(result ->
                  Platform.runLater(() -> callback.accept(result))
              );
    }
}
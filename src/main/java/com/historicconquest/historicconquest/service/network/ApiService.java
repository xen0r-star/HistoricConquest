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
import java.util.function.Consumer;

public class ApiService {
    private static final String API_BASE_URL = "http://localhost:8080/api";

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
    public record CreateRoomResponse(
        String roomCode,
        String token,

        ErrorRequest error
    ) {}


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record JoinRoomResponse(
        String token,
        String pseudo,
        Collection<NetworkPlayer> players,

        ErrorRequest error
    ) {}
}
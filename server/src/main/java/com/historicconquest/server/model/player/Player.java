package com.historicconquest.server.model.player;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.historicconquest.server.util.JwtService;
import java.util.UUID;

public class Player {
    private final String id;

    @JsonIgnore private final String token;

    private String pseudo;
    private String color;
    private final Type type;
    private Status status;
    private int ping;

    private int consecutiveSuccesses;
    private int consecutiveFailures;


    public Player(String pseudo, String roomCode) {
        this.id = UUID.randomUUID().toString();
        this.token = JwtService.generateToken(id, roomCode);
        this.pseudo = pseudo;
        this.type = Type.Player;
        this.status = Status.Waiting;
    }

    public Player(String pseudo, Type type, String roomCode, Status status) {
        this.id = UUID.randomUUID().toString();
        this.token = JwtService.generateToken(id, roomCode);
        this.pseudo = pseudo;
        this.type = type;
        this.status = status;
    }


    @JsonProperty public String getId() {
        return id;
    }
    @JsonProperty public Type getType() {
        return type;
    }
    @JsonProperty public String getPseudo() {
        return pseudo;
    }
    @JsonProperty public String getColor() {
        return color;
    }
    @JsonProperty public Status getStatus() {
        return status;
    }
    @JsonProperty public int getPing() {
        return ping;
    }

    @JsonIgnore public String getToken() {
        return token;
    }


    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }
    public void setColor(String color) {
        this.color = color;
    }
    public void setStatus(Status status) {
        this.status = status;
    }
    public void setPing(int ping) {
        this.ping = ping;
    }



    public enum Status {
        Ready(),
        Waiting();

        Status() { }
    }

    public enum Type {
        Bot(),
        Player();

        Type() { }
    }
}

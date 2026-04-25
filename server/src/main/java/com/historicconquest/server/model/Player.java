package com.historicconquest.server.model;

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
        Ready("Ready"),
        Waiting("Waiting");

        private final String label;

        Status(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum Type {
        Bot("Bot"),
        Player("Player");

        private final String label;

        Type(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}

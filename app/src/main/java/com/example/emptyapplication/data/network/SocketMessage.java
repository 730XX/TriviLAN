package com.example.emptyapplication.data.network;

import com.google.gson.Gson;
import java.util.Objects;

public class SocketMessage {

    public static final String TYPE_JOIN = "JOIN";
    public static final String TYPE_LOBBY_UPDATE = "LOBBY_UPDATE";
    public static final String TYPE_START_GAME = "START_GAME";
    public static final String TYPE_SUBMIT_SCORE = "SUBMIT_SCORE";
    public static final String TYPE_PODIUM = "PODIUM";

    private static final Gson GSON = new Gson();

    private String type;
    private String payload;

    public SocketMessage() {
    }

    public SocketMessage(String type, String payload) {
        this.type = type;
        this.payload = payload;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    public static SocketMessage fromJson(String json) {
        return GSON.fromJson(json, SocketMessage.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SocketMessage that = (SocketMessage) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, payload);
    }

    @Override
    public String toString() {
        return "SocketMessage{" +
                "type='" + type + '\'' +
                ", payload='" + payload + '\'' +
                '}';
    }
}

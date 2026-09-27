package com.example.emptyapplication.domain.model;

import java.util.Objects;

/**
 * Domain model representing an available LAN trivia room.
 */
public class Room {

    private String id;
    private String host;
    private String ip;
    private int port;
    private int players;
    private int maxPlayers;
    private int ping;

    public Room() {
    }

    public Room(String id, String host, String ip, int port, int players, int maxPlayers, int ping) {
        this.id = id;
        this.host = host;
        this.ip = ip;
        this.port = port;
        this.players = players;
        this.maxPlayers = maxPlayers;
        this.ping = ping;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPlayers() {
        return players;
    }

    public void setPlayers(int players) {
        this.players = players;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getPing() {
        return ping;
    }

    public void setPing(int ping) {
        this.ping = ping;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return port == room.port &&
                players == room.players &&
                maxPlayers == room.maxPlayers &&
                ping == room.ping &&
                Objects.equals(id, room.id) &&
                Objects.equals(host, room.host) &&
                Objects.equals(ip, room.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, host, ip, port, players, maxPlayers, ping);
    }

    @Override
    public String toString() {
        return "Room{" +
                "id='" + id + '\'' +
                ", host='" + host + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", players=" + players +
                ", maxPlayers=" + maxPlayers +
                ", ping=" + ping +
                '}';
    }
}

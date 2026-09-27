package com.example.emptyapplication.domain.model;

import java.util.Objects;

public class Player {

    private final String id;
    private final String name;
    private int score;
    private boolean isFinished;
    private int avatarResId;
    private boolean isHost;
    private int accuracy = 100;
    private double avgTime = 3.5;

    public Player(String id, String name) {
        this(id, name, 0, false, 0, false);
    }

    public Player(String id, String name, int score, boolean isFinished) {
        this(id, name, score, isFinished, 0, false);
    }

    public Player(String id, String name, int score, boolean isFinished, int avatarResId, boolean isHost) {
        this(id, name, score, isFinished, avatarResId, isHost, 100, 3.5);
    }

    public Player(String id, String name, int score, boolean isFinished, int avatarResId, boolean isHost, int accuracy, double avgTime) {
        this.id = id;
        this.name = name;
        this.score = score;
        this.isFinished = isFinished;
        this.avatarResId = avatarResId;
        this.isHost = isHost;
        this.accuracy = accuracy;
        this.avgTime = avgTime;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    public int getAvatarResId() {
        return avatarResId;
    }

    public void setAvatarResId(int avatarResId) {
        this.avatarResId = avatarResId;
    }

    public boolean isHost() {
        return isHost;
    }

    public void setHost(boolean host) {
        isHost = host;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }

    public double getAvgTime() {
        return avgTime;
    }

    public void setAvgTime(double avgTime) {
        this.avgTime = avgTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return score == player.score &&
                isFinished == player.isFinished &&
                avatarResId == player.avatarResId &&
                isHost == player.isHost &&
                Objects.equals(id, player.id) &&
                Objects.equals(name, player.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, score, isFinished, avatarResId, isHost);
    }

    @Override
    public String toString() {
        return "Player{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", score=" + score +
                ", isFinished=" + isFinished +
                ", avatarResId=" + avatarResId +
                ", isHost=" + isHost +
                '}';
    }
}

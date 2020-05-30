package com.adam.pong.game;

public class ChatMessage {

    private String message;
    private double creationTime;

    public ChatMessage( String message) {
        this.message = message;
        creationTime = System.currentTimeMillis();
    }

    public String getMessage() {
        return message;
    }

    public double getCreationTime() {
        return creationTime;
    }
}

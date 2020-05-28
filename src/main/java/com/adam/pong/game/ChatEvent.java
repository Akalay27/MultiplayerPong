package com.adam.pong.game;

public class ChatEvent extends GraphicsEvent {

    private final String message;
    public ChatEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

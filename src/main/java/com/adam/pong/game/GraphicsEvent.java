package com.adam.pong.game;

import java.io.Serializable;
import java.util.UUID;

public class GraphicsEvent implements Serializable {
    private final UUID uuid;
    private final double initTime;
    public GraphicsEvent() {
        initTime = System.currentTimeMillis();
        uuid = UUID.randomUUID();
    }

    public double getInitTime() {
        return initTime;
    }

    public UUID getUUID() {
        return uuid;
    }
}

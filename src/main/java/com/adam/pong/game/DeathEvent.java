package com.adam.pong.game;

public class DeathEvent extends GraphicsEvent {

    private final PlayerBounds bounds;
    private final Point2D focus;

    public DeathEvent(PlayerBounds bounds, Point2D focus) {
        this.bounds = bounds;
        this.focus = focus;
    }

    public PlayerBounds getBounds() {
        return bounds;
    }

    public Point2D getFocus() {
        return focus;
    }
}

package com.adam.pong.game;

public class DirectionIndicatorEvent extends GraphicsEvent {

    private final double finalAngle;
    private final double startAngle;

    public DirectionIndicatorEvent(double finalAngle, double startAngle) {
        this.finalAngle = finalAngle;
        this.startAngle = startAngle;
    }

    public double getFinalAngle() {
        return finalAngle;
    }

    public double getStartAngle() {
        return startAngle;
    }
}

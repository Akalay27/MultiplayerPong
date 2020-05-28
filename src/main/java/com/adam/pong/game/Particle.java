package com.adam.pong.game;

public class Particle {

    private Point2D position;
    private Point2D velocity;
    private double angle;
    private double length;
    private double lifetime;
    private double initTime;
    private double angVelocity;
    public Particle(Point2D position, Point2D velocity, double angle, double length, double lifetime, double initTime, double angVelocity) {
        this.position = position;
        this.velocity = velocity;
        this.angle = angle;
        this.length = length;
        this.lifetime = lifetime;
        this.initTime = initTime;
        this.angVelocity = angVelocity;
    }

    public Point2D getPosition() {
        return position;
    }

    public Point2D getVelocity() {
        return velocity;
    }

    public double getAngle() {
        return angle;
    }

    public double getLength() {
        return length;
    }

    public double getLifetime() {
        return lifetime;
    }

    public double getInitTime() {
        return initTime;
    }

    public void move() {
        this.angle+=angVelocity;
        this.position = this.position.add(this.velocity);
    }
}

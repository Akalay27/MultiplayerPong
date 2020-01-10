package com.adam.pong.game;

public class Ball {

    private Point2D position;
    private Point2D velocity;
    private double radius;

    public Ball(Point2D position, Point2D velocity, double radius) {
        this.position = position;
        this.velocity = velocity;
        this.radius = radius;
    }

    public void move(double timeDelta) {
        position = position.add(velocity.multiply(timeDelta));
    }

    public Point2D checkCollision(Point2D pt1, Point2D pt2) {
        return PongUtils.projectPointOnLine(pt1,pt2,position);
    }


    public Ball(Point2D pos) {
        this.position = pos;
    }

    public Point2D getPosition() {
        return position;
    }

    public double getRadius() {
        return radius;
    }

    public void setVelocity(Point2D vel) {
        this.position = vel;
    }

    public Point2D getVelocity() { return velocity; }

    public void setDirection(double angle) {
        double magnitude = Math.sqrt(Math.pow(velocity.getX(),2)+Math.pow(velocity.getY(),2));
        this.velocity = new Point2D(Math.cos(angle)*magnitude,Math.sin(angle)*magnitude);
    }
    public double getDirection() {
        return Math.atan2(velocity.getY(),velocity.getX());
    }
}

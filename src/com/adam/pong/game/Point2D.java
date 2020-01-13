package com.adam.pong.game;

import java.io.Serializable;

public class Point2D implements Serializable {

    private double x, y;

    public Point2D(double x,double y) {
        this.x = x;
        this.y = y;
    }

    public Point2D subtract(Point2D pt) {
        return new Point2D(x-pt.getX(),y-pt.getY());
    }
    public Point2D add(Point2D pt) {
        return new Point2D(x+pt.getX(),y+pt.getY());
    }
    public Point2D multiply(double magnitude) {
        return new Point2D(x*magnitude,y*magnitude);
    }
    public double dotProduct(Point2D pt) {
        return (x*pt.getX() + y*pt.getY());
    }
    public double distance(Point2D pt) {
        return Math.sqrt(Math.pow(x-pt.getX(),2) + Math.pow(y-pt.getY(),2));
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return x + ", " + y;
    }
}

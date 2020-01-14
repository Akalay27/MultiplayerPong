package com.adam.pong.game;



import java.io.Serializable;

public class PlayerBounds implements Serializable {

    Point2D pt1;
    Point2D pt2;

    public PlayerBounds(Point2D pt1, Point2D pt2) {
        this.pt1 = pt1;
        this.pt2 = pt2;
    }

    @Override
    public String toString() {
        return (pt1 + " - " + pt2);
    }

    public boolean isNull() {
        return pt1==null||pt2==null;
    }
}

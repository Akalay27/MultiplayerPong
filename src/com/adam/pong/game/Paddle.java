package com.adam.pong.game;



import java.io.Serializable;

public class Paddle implements Serializable {

    public Point2D pt1, pt2, pt3, pt4;


    public Paddle(Point2D pt1, Point2D pt2, Point2D pt3, Point2D pt4) {
        this.pt1 = pt1;
        this.pt2 = pt2;
        this.pt3 = pt3;
        this.pt4 = pt4;
    }
}

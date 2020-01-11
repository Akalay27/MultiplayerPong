package com.adam.pong.game;



import java.io.Serializable;

public class Paddle implements Serializable {

    public Point2D pt1, pt2, pt3, pt4;

    /**
     * @param pt1 The top right corner of the paddle.
     * @param pt2 The bottom right corner of the paddle.
     * @param pt3 The bottom left corner of the paddle.
     * @param pt4 The top left corner of the paddle.
     */
    public Paddle(Point2D pt1, Point2D pt2, Point2D pt3, Point2D pt4) {
        this.pt1 = pt1;
        this.pt2 = pt2;
        this.pt3 = pt3;
        this.pt4 = pt4;
    }
    public Point2D getCenter() {
        return new Point2D((pt1.getX()+pt2.getX()+pt3.getX()+pt4.getX())/4,(pt1.getY()+pt2.getY()+pt3.getY()+pt4.getY())/4);
    }
}

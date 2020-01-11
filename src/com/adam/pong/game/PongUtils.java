package com.adam.pong.game;


public class PongUtils
{

    public static Point2D getRotatedPoint(Point2D pos, double r, Point2D offset) {
        double rotX = pos.getX() + (offset.getX() * Math.cos(r)) - (offset.getY() * Math.sin(r));
        double rotY = pos.getY() + (offset.getX() * Math.sin(r)) + (offset.getY() * Math.cos(r));
        return new Point2D(rotX, rotY);
    }

    public static Point2D projectPointOnLine(Point2D linePoint1, Point2D linePoint2, Point2D pt) {

        Point2D ap = pt.subtract(linePoint1);
        Point2D ab = linePoint2.subtract(linePoint1);

        double coef = ap.dotProduct(ab) / ab.dotProduct(ab);

        return linePoint1.add(ab.multiply(coef));
    }

    public static double lerp(double from, double to, double amnt) {
        return from + (to-from)*amnt;
    }

    public static boolean isPointOnSegment(Point2D segPt1, Point2D segPt2, Point2D point, double tolerance) {
        if (Math.abs(segPt1.distance(point) + segPt2.distance(point)-segPt1.distance(segPt2)) < tolerance) {
            return true;
        }
        return false;
    }


}

package com.adam.pong.game;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.paint.Color;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Camera {

    private final GraphicsContext gc;
    private Point2D center;
    private double rotation = 0;
    private double scale = 1;

    public Camera (GraphicsContext gc) {
        this.gc = gc;
        center = new Point2D(0,0);
    }

    public void drawPlayer(GraphicsContext gc, Player player) {
        java.awt.Color tempColor = player.getColor();
        Color playerColor = Color.rgb(tempColor.getRed(),tempColor.getGreen(),tempColor.getBlue());

        PlayerBounds playerBounds = player.getPlayerBounds();
        Point2D boundsPt1 = transPt(playerBounds.pt1);
        Point2D boundsPt2 = transPt(playerBounds.pt2);

        gc.strokeLine(boundsPt1.getX(), boundsPt1.getY(), boundsPt2.getX(), boundsPt2.getY());
        gc.setFill(playerColor);
        Paddle paddle = player.getPaddle();
      //  if (paddle != null && !paddle.isNull()) {
            Point2D paddlePt1 = transPt(paddle.pt1);
            Point2D paddlePt2 = transPt(paddle.pt2);
            Point2D paddlePt3 = transPt(paddle.pt3);
            Point2D paddlePt4 = transPt(paddle.pt4);
            gc.fillPolygon(new double[]{paddlePt1.getX(), paddlePt2.getX(), paddlePt3.getX(), paddlePt4.getX()}, new double[]{paddlePt1.getY(), paddlePt2.getY(), paddlePt3.getY(), paddlePt4.getY()}, 4);
     //   }
    }

    private Point2D transPt(Point2D pt) {
        return PongUtils.getRotatedPoint(center,rotation,center.subtract(pt)).multiply(scale).add(new Point2D(gc.getCanvas().getWidth()/2,gc.getCanvas().getHeight()/2));
    }

    public void drawBall(Point2D ballPos){
        gc.setFill(Color.BLACK);
        Point2D transPos = transPt(ballPos);
        gc.fillOval(transPos.getX() - 15*scale, transPos.getY() - 15*scale, 30*scale, 30*scale);
    }

    public void setTransform(Player[] players, int focusedId) {
        List<Double> boundsYValues = new ArrayList<>();
        boolean foundPlayer = false;
        double offsetFor2P = 0;
        for (int p = 0; p < players.length; p++) {
            if (!players[p].getPlayerBounds().isNull()) {
                boundsYValues.add(players[p].getPlayerBounds().pt1.getY());
                boundsYValues.add(players[p].getPlayerBounds().pt2.getY());

                if (players[p].getId() == focusedId) {
                    if (players.length == 2 && p == 1) offsetFor2P = -Math.PI;
                    rotation = PongUtils.lerp(rotation, -Math.PI * 2 / players.length / 2 * (1 + 2 * p) - Math.PI / 2 + offsetFor2P, 0.3);
                    foundPlayer = true;
                }
            }
        }
        if (!foundPlayer) rotation = PongUtils.lerp(rotation,0,1);

        double minY = Collections.min(boundsYValues);
        double maxY = Collections.max(boundsYValues);

        double cvsHeight = gc.getCanvas().getHeight();
        scale = PongUtils.lerp(scale,cvsHeight/(maxY-minY)*0.9,0.3);



    }
    public void drawPoints(GraphicsContext gc, Point2D[] points,double size) {
        gc.setFill(Color.RED);
        for (Point2D p : points) {
            Point2D pt = transPt(p);
            gc.fillOval(pt.getX()-size/2,pt.getY()-size/2,size,size);
        }
    }

    public void setCenter(Point2D center) {
        this.center = center;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }
}

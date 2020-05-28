package com.adam.pong.game;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Camera {

    private final GraphicsContext gc;
    private Point2D center;
    private double rotation = 0;
    private double scale = 1;
    private ArrayList<Particle> particles;
    private double angleDisplayTimer;

    public Camera (GraphicsContext gc) {
        this.gc = gc;
        center = new Point2D(0,0);
        particles = new ArrayList<>();
    }

    public void drawPlayer(Player player) {
        java.awt.Color tempColor = player.getColor();
        Color playerColor = Color.rgb(tempColor.getRed(),tempColor.getGreen(),tempColor.getBlue());

        PlayerBounds playerBounds = player.getPlayerBounds();
        Point2D boundsPt1 = transPt(playerBounds.pt1);
        Point2D boundsPt2 = transPt(playerBounds.pt2);

        gc.strokeLine(boundsPt1.getX(), boundsPt1.getY(), boundsPt2.getX(), boundsPt2.getY());
        gc.setFill(playerColor);

        Paddle paddle = player.getPaddle();
        if (!paddle.isNull()) {
            Point2D paddlePt1 = transPt(paddle.pt1);
            Point2D paddlePt2 = transPt(paddle.pt2);
            Point2D paddlePt3 = transPt(paddle.pt3);
            Point2D paddlePt4 = transPt(paddle.pt4);
            gc.fillPolygon(new double[]{paddlePt1.getX(), paddlePt2.getX(), paddlePt3.getX(), paddlePt4.getX()}, new double[]{paddlePt1.getY(), paddlePt2.getY(), paddlePt3.getY(), paddlePt4.getY()}, 4);
        }
    }

    private Point2D transPt(Point2D pt) {
        return transPt(pt, true);
    }

    private Point2D transPt(Point2D pt, boolean useRotation) {
        return PongUtils.getRotatedPoint(center,useRotation ? rotation : 0,center.subtract(pt)).multiply(scale).add(new Point2D(gc.getCanvas().getWidth()/2,gc.getCanvas().getHeight()/2));
    }

    public void drawBall(Point2D ballPos){
        gc.setFill(Color.BLACK);
        Point2D transPos = transPt(ballPos);
        gc.fillOval(transPos.getX() - GameLoop.ballRadius*scale, transPos.getY() - GameLoop.ballRadius*scale, GameLoop.ballRadius*2*scale, GameLoop.ballRadius*2*scale);
    }

    public void setTransform(Player[] players, int focusedId) {
        List<Double> boundsYValues = new ArrayList();
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
    public void drawPoints(Point2D[] points,double size) {
        gc.setFill(Color.RED);
        for (Point2D p : points) {
            Point2D pt = transPt(p);
            gc.fillOval(pt.getX()-size/2,pt.getY()-size/2,size,size);
        }
    }

    public void drawMessage(String message) {
        gc.setFill(Color.WHITE);
        gc.setFont(new Font(null,scale*20));
        gc.setTextBaseline(VPos.CENTER);
        Point2D textPos = transPt(new Point2D(0,0),false);
        gc.fillText(message,textPos.getX(),textPos.getY());
    }

    public void drawPlayerName(Player player, double opacity) {
        gc.save();
        gc.setFont(new Font(null,scale*10));
        gc.setFill(Color.BLACK);
        Paddle paddle = player.getPaddle();
        Point2D boundsPt1 = transPt(paddle.pt2);
        Point2D boundsPt2 = transPt(paddle.pt3);

        double angle = Math.atan2(boundsPt1.getY()-boundsPt2.getY(),boundsPt1.getX()-boundsPt2.getX()) + Math.PI;
        if (angle >= Math.PI/2 && angle <= Math.PI*3/2) {
            angle+=Math.PI;
            gc.setTextBaseline(VPos.BOTTOM);
        } else {
            gc.setTextBaseline(VPos.TOP);
        }
        gc.setGlobalAlpha(opacity);

        Point2D midpoint = PongUtils.lerp(boundsPt1,boundsPt2,0.5);

        gc.translate(midpoint.getX(),midpoint.getY());
        gc.rotate(Math.toDegrees(angle));


        gc.fillText(player.getName(),0,0);
        gc.restore();
    }

    public void drawShatteredLine(Point2D pt1, Point2D pt2, Point2D focus) {

        double angle = Math.atan2(pt1.getY()-pt2.getY(),pt1.getX()-pt2.getX()) + Math.PI/2;
        int numSegments = 200;
        double totalDist = pt1.distance(pt2);
        for (int i = 0; i < numSegments; i++) {
            Point2D center = PongUtils.lerp(pt1,pt2,(double)i/numSegments).add(PongUtils.fromAngle(angle).multiply(totalDist/numSegments/2));
            //double velAngle = Math.atan2(focus.getY()-center.getY(),focus.getX()-center.getX()) + Math.PI/2;

            particles.add(new Particle(center,PongUtils.fromAngle(angle).multiply(1.2+Math.random()*16/(focus.distance(center)-GameLoop.ballRadius/2)),angle-Math.PI/2,totalDist/numSegments,3000,System.currentTimeMillis(),(Math.random()-0.5)*5/focus.distance(center) ));
        }
    }

    public void drawParticles() {
        for (Particle p : particles) {
            Point2D pt1 = transPt(PongUtils.fromAngle(p.getAngle()).multiply(p.getLength()/2).add(p.getPosition()));
            Point2D pt2 = transPt(PongUtils.fromAngle(p.getAngle()).multiply(-p.getLength()/2).add(p.getPosition()));

            gc.strokeLine(pt1.getX(),pt1.getY(),pt2.getX(),pt2.getY());
        }
    }

    public void moveParticles() {
        for (int p = 0; p < particles.size(); p++) {
            particles.get(p).move();
            if (System.currentTimeMillis() - particles.get(p).getInitTime() > particles.get(p).getLifetime()) {
                particles.remove(p);
            }
        }
    }

    public void drawAngleDisplay(double angle) {

        double angleDiff = Math.PI/4;

        Point2D center = transPt(this.center);

        double a1 = angle - angleDiff/2;
        double a2 = angle + angleDiff/2;

        Point2D pt2 = (PongUtils.fromAngle(a1).multiply(GameLoop.ballRadius*1.5));
        Point2D pt1 = (PongUtils.fromAngle(a2).multiply(GameLoop.ballRadius*1.5));

        Point2D pt4 = (PongUtils.fromAngle(a1).multiply(GameLoop.ballRadius*2));
        Point2D pt3 = (PongUtils.fromAngle(a2).multiply(GameLoop.ballRadius*2));

        gc.beginPath();
      // gc.arc(center.getX(),center.getY(),GameLoop.ballRadius*scale*1.5,GameLoop.ballRadius*scale*1.5,Math.toDegrees(angle-this.rotation),100*scale);

        gc.arcTo(pt1.getX(),pt1.getY(),pt2.getX(),pt2.getY(),GameLoop.ballRadius*1.5);
        gc.arcTo(pt3.getX(),pt1.getY(),pt4.getX(),pt2.getY(),GameLoop.ballRadius*2);

        //System.out.println(pt1);
        gc.closePath();
        gc.stroke();


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

    public void setAngleDisplayTimer(double angleDisplayTimer) {
        this.angleDisplayTimer = angleDisplayTimer;
    }
}

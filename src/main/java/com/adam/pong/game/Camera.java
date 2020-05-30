package com.adam.pong.game;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Camera {

    private final GraphicsContext gc;
    private Point2D center;
    private double rotation = 0;
    private double scale = 1;
    private final ArrayList<Particle> particles;
    private double angleDisplayTimer;
    private double startAngle;
    private double finalAngle;
    private final ArrayList<ChatMessage> chatMessages;

    public Camera (GraphicsContext gc) {
        this.gc = gc;
        center = new Point2D(0,0);
        particles = new ArrayList<>();
        chatMessages = new ArrayList<>();
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
        if (paddle != null && !paddle.isNull()) {
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

    private Point2D transPt(Point2D pt, boolean useRotation, boolean useScale) {
        return PongUtils.getRotatedPoint(center,useRotation ? rotation : 0,center.subtract(pt)).multiply(useScale ? scale : 1).add(new Point2D(gc.getCanvas().getWidth()/2,gc.getCanvas().getHeight()/2));
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
                    if (players.length == 2) offsetFor2P = -Math.PI/2;

                    Point2D playerMid = PongUtils.lerp(players[p].getPlayerBounds().pt1,players[p].getPlayerBounds().pt2,0.5);
                    rotation = PongUtils.lerp(rotation, -Math.atan2(playerMid.getY(),playerMid.getX()) - Math.PI/2+ offsetFor2P, 1);
                    foundPlayer = true;
                }
            }
        }
        if (!foundPlayer) {
            rotation = rotation % (Math.PI*2);
            rotation = PongUtils.lerp(rotation,0,0.05);
        }

        double minY = Collections.min(boundsYValues);
        double maxY = Collections.max(boundsYValues);

        double cvsHeight = Math.min(gc.getCanvas().getHeight(), gc.getCanvas().getWidth());


        double scaleOffset = 0.90;
        if (players.length == 3) {
            scaleOffset = 0.80;
        }


        scale = PongUtils.lerp(scale,cvsHeight/(maxY-minY) * scaleOffset,0.3);

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
        gc.setFont(new Font("Kayak Sans",scale*20));
        gc.setTextBaseline(VPos.CENTER);
        gc.setTextAlign(TextAlignment.CENTER);
        Point2D textPos = transPt(new Point2D(0,0),false);
        gc.fillText(message,textPos.getX(),textPos.getY());
    }

    public void drawPlayerName(Player player) {
        gc.save();
        gc.setFont(new Font("Kayak Sans", scale * 10));
        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.CENTER);
        Paddle paddle = player.getPaddle();
        Point2D boundsPt1 = transPt(paddle.pt2);
        Point2D boundsPt2 = transPt(paddle.pt3);

        double angle = Math.atan2(boundsPt1.getY() - boundsPt2.getY(), boundsPt1.getX() - boundsPt2.getX()) + Math.PI;
        if (angle >= Math.PI / 2 && angle <= Math.PI * 3 / 2) {
            angle += Math.PI;
            gc.setTextBaseline(VPos.BOTTOM);
        } else {
            gc.setTextBaseline(VPos.TOP);
        }

        Point2D midpoint = PongUtils.lerp(boundsPt1, boundsPt2, 0.5);

        gc.translate(midpoint.getX(), midpoint.getY());
        gc.rotate(Math.toDegrees(angle));


        gc.fillText(player.getName(), 0, 0);
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

    public void drawAngleDisplay() {

        if (System.currentTimeMillis() - angleDisplayTimer < 4000) {

            double tempTimer = (System.currentTimeMillis() - angleDisplayTimer) / 3000;

            tempTimer = (tempTimer >= 1) ? 1 : tempTimer;

            double angle = Math.sin(tempTimer * Math.PI / 2)*(finalAngle-startAngle) + startAngle;

            double radius = GameLoop.ballRadius * 1.2;
            double angleDiff = Math.PI / 2;

            Point2D center = transPt(this.center);

            double a1 = angle - angleDiff / 2;
            double a2 = angle + angleDiff / 2;

            int resolution = 30;
            ArrayList<Double> xPts = new ArrayList<>();
            ArrayList<Double> yPts = new ArrayList<>();


            for (double a = a1; a <= a2; a += (a2 - a1) / (resolution)) {
                Point2D pt = transPt(PongUtils.fromAngle(a).multiply(radius));
                xPts.add(pt.getX());
                yPts.add(pt.getY());
            }
            gc.save();
            gc.setLineWidth(3*scale);
            gc.setLineCap(StrokeLineCap.ROUND);
            gc.strokePolyline(ArrayUtils.toPrimitive(xPts.toArray(new Double[0])),ArrayUtils.toPrimitive(yPts.toArray(new Double[0])),xPts.size());
            gc.restore();
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

    public void setAngleDisplayTimer(double angleDisplayTimer) {
        this.angleDisplayTimer = angleDisplayTimer;
    }

    public void addChatMessage(String message) {
       chatMessages.add(new ChatMessage(message));
    }

    public void drawChat() {

        double chatScale = gc.getCanvas().getWidth()/2160 ;
        double xPos = gc.getCanvas().getWidth()-30;
        gc.setFont(new Font("Kayak Sans",37*chatScale));
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setFill(Color.BLACK);
        for (int c = 0; c < chatMessages.size(); c++) {
            double yPos = c*48*chatScale+45;
            double alpha = (System.currentTimeMillis()-chatMessages.get(c).getCreationTime()) / 500.0;
            alpha = (alpha >= 1) ? 1 : alpha;
            double disappearAlpha = (System.currentTimeMillis()-chatMessages.get(c).getCreationTime()-6500) / 1500.0;
            disappearAlpha = (disappearAlpha <= 0) ? 0 : disappearAlpha;
            gc.setGlobalAlpha(alpha-disappearAlpha);
            gc.fillText(chatMessages.get(c).getMessage(),xPos,yPos);
            gc.setGlobalAlpha(1);
        }
    }
    public void updateChatMessages() {
        chatMessages.removeIf(e -> (System.currentTimeMillis() - e.getCreationTime() >= 8000));
    }

    public void setBallAngles(double finalAngle, double startAngle) {
        this.startAngle = startAngle;
        this.finalAngle = finalAngle;
    }
}

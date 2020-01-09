package com.adam.pong.game;


import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameLoop extends Thread {

    private boolean running;
    private List<Player> players;
    private Ball ball;
    private double paddleWidth = 50;
    private double paddleHeight = 15;
    private boolean regenerateBounds;

    public GameLoop (CopyOnWriteArrayList players) {
        this.players = players;
        resetBall();
        regenerateBounds = false;
    }
    public void run() {

        running = true;

        final int TARGET_FPS = 60;
        final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;
        long lastLoopTime = System.nanoTime();

        while (running) {

            long now = System.nanoTime();
            long updateLength = now - lastLoopTime;
            lastLoopTime = now;

            double timeDelta = updateLength / ((double)(OPTIMAL_TIME));

            updatePlayerPositions(timeDelta);
            createPaddleCoordinates();
            ball.move(timeDelta);
            checkCollisions();

            //System.out.println("I am currently dealing with " + players.size() + " players!");

            // do I need to actually sleep the thread if we already have a deltatime value?
//            try{
//                Thread.sleep( (lastLoopTime-System.nanoTime() + OPTIMAL_TIME)/1000000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            };
        }
    }
    /* for each player, use bounds and position to create the paddle coordinates */
    private void createPaddleCoordinates() {

        for (Player p : players) {

            double position = p.getPosition();
            PlayerBounds bounds = p.getPlayerBounds();
            if (bounds != null) {

                Point2D paddleCenter = bounds.pt1.add(bounds.pt2.subtract(bounds.pt1).multiply(position));
                double boundsAngle = Math.atan2(bounds.pt2.getY() - bounds.pt1.getY(), bounds.pt2.getX() - bounds.pt1.getX());

                Point2D pt1 = PongUtils.getRotatedPoint(paddleCenter, boundsAngle, new Point2D(paddleWidth, paddleHeight));
                Point2D pt2 = PongUtils.getRotatedPoint(paddleCenter, boundsAngle, new Point2D(paddleWidth, -paddleHeight));
                Point2D pt3 = PongUtils.getRotatedPoint(paddleCenter, boundsAngle, new Point2D(-paddleWidth, -paddleHeight));
                Point2D pt4 = PongUtils.getRotatedPoint(paddleCenter, boundsAngle, new Point2D(-paddleWidth, paddleHeight));

                p.setPaddle(new Paddle(pt1,pt2,pt3,pt4));
            }
        }
    }

    private void updatePlayerPositions(double timeDelta) {

        // calculate moveable distance of the PlayerBounds
        if (players.size() > 0) {
            PlayerBounds bounds = players.get(0).getPlayerBounds();
            if (bounds != null) {
                double entireWidth = Math.sqrt(Math.pow(bounds.pt1.getX() - bounds.pt2.getX(), 2) + Math.pow(bounds.pt1.getY() - bounds.pt2.getY(), 2));
                double usableWidth = entireWidth - paddleWidth - paddleHeight * Math.tan(Math.PI / players.size());
                double moveAmount = usableWidth / entireWidth;
                double moveBound = (1 - moveAmount);

                for (Player p : players) {
                    if (p.getInput() == UserInput.LEFT) {
                        p.changePosition(0.01 * timeDelta);
                    }
                    if (p.getInput() == UserInput.RIGHT) {
                        p.changePosition(-0.01 * timeDelta);
                    }
                    if (p.getPosition() > 1 - moveBound) {
                        p.setPosition(1 - moveBound);
                    }
                    if (p.getPosition() < moveBound) {
                        p.setPosition(moveBound);
                    }
                }
            }
        }

    }

    private void checkCollisions() {

        Point2D ballPos = ball.getPosition();

        for (Player p : players) {

            Paddle paddle = p.getPaddle();
            if (paddle != null) {
                Point2D collisionPt = ball.checkCollision(paddle.pt1,paddle.pt4);
                double distToPt = ballPos.distance(collisionPt);
                if (distToPt < ball.getRadius()) {
                    if (PongUtils.isPointOnSegment(paddle.pt1,paddle.pt4,collisionPt,0.001)) {
                        // ball hit paddle
                        double ballDir = ball.getDirection();
                        double angleToPt = Math.atan2(ballPos.getY() - collisionPt.getY(), ballPos.getX() - collisionPt.getX());
                        double diff = angleToPt - ballDir;
                        ball.setDirection(ballDir - diff * 2);
                    } else {
                        // player is eliminated
                        p.setEliminated(true);
                        regenerateBounds = true;
                        resetBall();

                    }
                }
            }

        }
    }

    public Point2D getBallPosition() {
        return ball.getPosition();
    }

    public boolean needToRegenerateBounds() {
        if (regenerateBounds) {
            regenerateBounds = false;
            return true;
        } else {
            return false;
        }
    }

    private void resetBall() {
        ball = new Ball(new Point2D(0,0), new Point2D(1,5),15);
    }
}

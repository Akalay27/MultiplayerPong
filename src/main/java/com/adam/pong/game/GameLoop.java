package com.adam.pong.game;

/*
DONE: Add game pacing through countdowns and rearrange animations
DONE: First move generatePlayerBounds into GameLoop
DONE: Then just make it so the game starts on GameLoop.start() or once a certain number of players join
TODO: Create a countdown system that changes a message String that PongServer gets and sends to the client
TODO: Also implement a rearrange animation that goes along with the countdown
TODO: Create a death animation trigger that the Camera instances see and then do either a particle system with points or line segments
--> Rearrange is handled by server, death animation is handled by Game/Camera.
TODO: Add some kind of way to tell which way the ball is going to go to start or implement a last-killer gets to choose system.
TODO: Change CPU's so they decide every once and awhile a random segment on their paddle to keep the ball on so its not always in the middle -> purposely change the direction of the ball.
 */
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameLoop extends Thread {

    private boolean physicsRunning;
    private boolean running;
    private List<Player> players;
    private Ball ball;
    private double ballStartAngle;
    public static double paddleWidth = 25;
    public static double paddleHeight = 7.5;
    public static double ballRadius = 15;
    private double invisibleBounds = -1;
    private double ballSpeed = 7;
    private double lastTouchedId = -1;
    private Point2D[] debugPoints = null;


    public GameLoop (CopyOnWriteArrayList players) {
        this.players = players;
        resetBall();

    }
    public void run() {
        running = true;
        physicsRunning = true;

        final int TARGET_FPS = 60;
        final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;
        long lastLoopTime = System.nanoTime();

        while (running) {
            long now = System.nanoTime();
            long loopTimeMillis = System.currentTimeMillis();
            long updateLength = now - lastLoopTime;
            double timeDelta = updateLength / ((double) (OPTIMAL_TIME));
            lastLoopTime = now;
            if (physicsRunning) {
                ball.move(timeDelta);
                checkCollisions();
            } else {
                lastTouchedId = -1;
            }
            updatePlayerPositions(timeDelta);
            createPaddleCoordinates();
            //System.out.println("I am currently dealing with " + players.size() + " players!");

            // do I need to actually sleep the thread if we already have a deltatime value?
            Thread.yield();
          try{
                long duration = (long) ((loopTimeMillis - System.currentTimeMillis()) + (1000.0 / 200));
                System.out.println(duration);
                Thread.sleep((duration >= 0) ? duration : 0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            };
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
                double usableWidth = entireWidth - paddleWidth;
                if (currentPlayers().size() > 2) usableWidth = usableWidth - paddleHeight * Math.tan(Math.PI / players.size());
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
            if (paddle != null && p.getState() == Player.State.INGAME) {
                Point2D collisionPt = ball.checkCollision(paddle.pt1,paddle.pt4);

                if (ballPos.distance(collisionPt) < ball.getRadius()) {
                    if (PongUtils.isPointOnSegment(paddle.pt1,paddle.pt4,collisionPt,0.001) && p.getId() != lastTouchedId) {

                        Point2D paddleCenter = paddle.getCenter();
                        double angleToCenter = Math.atan2(ballPos.getY()-paddleCenter.getY(),ballPos.getX()-paddleCenter.getX());
                        ball.setDirection(angleToCenter);
                        ball.setVelocity(ball.getVelocity().add(ball.getVelocity().multiply(1/ball.getVelocity().getMagnitude()*0.3)));
                        lastTouchedId = p.getId();
                    }
                Point2D boundsCollisionPt = ball.checkCollision(p.getPlayerBounds().pt1, p.getPlayerBounds().pt2);
                if (ballPos.distance(boundsCollisionPt) < ball.getRadius()) {
                    if (PongUtils.isPointOnSegment(p.getPlayerBounds().pt1, p.getPlayerBounds().pt2, boundsCollisionPt, 0.001)) {
                        // player is eliminated
                        p.setState(Player.State.DEATH);
                    }
                }

                }
            }

        }

        if (invisibleBounds >= 0) {
            double radius = ball.getRadius();
            if (ballPos.getY() - radius < -invisibleBounds && lastTouchedId != -2) {
                Point2D vel = ball.getVelocity();
                ball.setVelocity(new Point2D(vel.getX(),-vel.getY()));
                lastTouchedId = -2;
            } else
            if (ballPos.getY() + radius > invisibleBounds && lastTouchedId != -3) {
                Point2D vel = ball.getVelocity();
                ball.setVelocity(new Point2D(vel.getX(),-vel.getY()));
                lastTouchedId = -3;
            }
        }
    }

    public Point2D getBallPosition() {
        return ball.getPosition();
    }

    public double getStartAngle() { return ballStartAngle; }

    public void setInvisibleBounds(double bounds) {
        invisibleBounds = bounds;
    }

    public ArrayList<Player> currentPlayers() {
        ArrayList<Player> cPlayers = new ArrayList();
        for (Player p : players) {
            if (p.getState() == Player.State.INGAME) cPlayers.add(p);
        }

        return cPlayers;
    }

    public Point2D[] getDebugPoints() {
        return debugPoints;
    }

    public void resetBall() {
        //ballStartAngle = (((int)(Math.random()*players.size()))+0.5)/(double)players.size()*Math.PI*2;
        ballStartAngle = Math.PI*2*Math.random();
        ball = new Ball(new Point2D(0,0), new Point2D(Math.cos(ballStartAngle)*ballSpeed,Math.sin(ballStartAngle)*ballSpeed), ballRadius);
    }

    public void setPhysicsRunning(boolean physicsRunning) {
        this.physicsRunning = physicsRunning;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}

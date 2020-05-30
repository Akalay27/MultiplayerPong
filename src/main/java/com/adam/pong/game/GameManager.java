package com.adam.pong.game;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameManager extends Thread {

    private final double countdownTime = 4000;
    private final int minPlayersToStart = 2;
    private boolean running;
    private List<Player> players;
    private GameLoop gameLoop;
    private boolean gameInProgress = false;
    private String message = "";
    private double timer = 0;
    private boolean sendNewPositions = false;
    private boolean reviveAll = true;
    private ArrayList<GraphicsEvent> graphicsEvents;

    private PlayerBounds[] previousPlayerBounds;
    private double boundsAnimationTimer = 0;

    public GameManager(CopyOnWriteArrayList players, GameLoop gameLoop) {
        this.players = players;
        this.gameLoop = gameLoop;
        this.graphicsEvents = new ArrayList<>();
    }

    @Override
    public void run() {
        running = true;
        boolean useAnimatedBounds = false;

        while (running) {

            if (reviveAll) {
                for (Player p : players) {
                    p.setState(Player.State.INGAME);
                }
            }
            startBoundsAnimation();
            boolean waiting = true;
            gameLoop.setPhysicsRunning(false);
            gameLoop.resetBall();

            final int TARGET_FPS = 60;
            final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;
            long lastLoopTime = System.nanoTime();

            while (waiting && running) {

                long now = System.nanoTime();
                long updateLength = now - lastLoopTime;
                double timeDelta = updateLength / ((double) (OPTIMAL_TIME));
                lastLoopTime = now;
                if (reviveAll) {
                    for (Player p : players) {
                        p.setState(Player.State.INGAME);
                    }

                }
                if (!gameInProgress && players.size() >= minPlayersToStart) {
                    timer = System.currentTimeMillis();
                    gameInProgress = true;

                    double ballAngle = gameLoop.getStartAngle();
                    graphicsEvents.add(new DirectionIndicatorEvent(ballAngle, ballAngle+(Math.signum(Math.random()-0.5)*(Math.PI*3*(Math.random()* 0.3 + 0.7)))));
                }

                if (gameInProgress && System.currentTimeMillis() - timer < countdownTime) {
                    message = "" + ( (int)((countdownTime / 1000 - (System.currentTimeMillis() - timer) / 1000))+1);
                    gameLoop.setPhysicsRunning(false);


                }

                if (gameInProgress && System.currentTimeMillis() - timer >= countdownTime) {
                    waiting = false;
                    message = "";
                    gameLoop.setPhysicsRunning(true);
                }


                createNewBounds(useAnimatedBounds);
                boundsAnimationTimer+=0.05*timeDelta;
                updateEventList();
                Thread.yield();
            }
            if (reviveAll)
                reviveAll = false;
            useAnimatedBounds = true;


            //gameLoop.resetBall();
            while (gameInProgress && running) {


                for (Player p : players) {
                    if (p.getState() == Player.State.DEATH) {

                        p.setState(Player.State.ELIMINATED);

                        gameLoop.setPhysicsRunning(false);
                        graphicsEvents.add(new DeathEvent(p.getPlayerBounds(),gameLoop.getBallPosition()));
                        graphicsEvents.add(new ChatEvent(p.getName() + " is out!"));
                        gameLoop.resetBall();
                        startBoundsAnimation();
                        gameLoop.setPhysicsRunning(true);
                        timer = System.currentTimeMillis();
                        gameInProgress = false;

                        break;
                    }
                }
                updateEventList();
            }
            if (currentPlayers().size() < 2) {
                gameInProgress = false;
                gameLoop.setPhysicsRunning(false);
                reviveAll = true;
                graphicsEvents.add(new ChatEvent(currentPlayers().get(0).getName() + " WINS!!!"));
                timer = System.currentTimeMillis();
            }
            Thread.yield();
        }
    }

    private void startBoundsAnimation() {
        ArrayList<Player> cPlayers = currentPlayers();
        previousPlayerBounds = new PlayerBounds[cPlayers.size()];
        for (int p = 0; p < cPlayers.size(); p++) {
            previousPlayerBounds[p] = cPlayers.get(p).getPlayerBounds();
        }
        boundsAnimationTimer = 0;
    }


//    public void createNewBounds(double amnt) {
//
//        PlayerBounds[] bounds = generatePlayerBounds();
//        ArrayList<Player> cPlayers = currentPlayers();
//        boolean done = false;
//        double iterations = 1000;
//        for (int i = 0; i < iterations; i++) {
//            for (int p = 0; p < bounds.length; p++) {
//                PlayerBounds lastBounds = cPlayers.get(p).getPlayerBounds();
//                PlayerBounds newBounds = bounds[p];
//                if (lastBounds != null && !lastBounds.isNull()) {
//                    PlayerBounds midBounds = new PlayerBounds(PongUtils.lerp(lastBounds.pt1, newBounds.pt1, amnt), PongUtils.lerp(lastBounds.pt2, newBounds.pt2, amnt));
//                    cPlayers.get(p).setPlayerBounds(midBounds);
//                    Point2D diff1 = midBounds.pt1.subtract(newBounds.pt1);
//                    Point2D diff2 = midBounds.pt2.subtract(newBounds.pt2);
//                } else {
//                    cPlayers.get(p).setPlayerBounds(newBounds);
//                }
//            }
//            sendNewPositions = true;
//        }
//    }

    public void createNewBounds(boolean animation) {
        PlayerBounds[] bounds = generatePlayerBounds();
        ArrayList<Player> cPlayers = currentPlayers();

        double limitedAnimTimer = (boundsAnimationTimer >= 2) ? 2 : boundsAnimationTimer;
        limitedAnimTimer = (limitedAnimTimer <= 1) ? 1 : limitedAnimTimer;
        double changeAmount = Math.sin((limitedAnimTimer-1) * Math.PI / 2);
        if (previousPlayerBounds == null || previousPlayerBounds.length <= 0)
            animation = false;
        if (cPlayers.size() >= 2) {
            for (int p = 0; p < bounds.length; p++) {
                if (animation) {
                    PlayerBounds lastBounds = previousPlayerBounds[p];
                    PlayerBounds newBounds = bounds[p];
                    if (lastBounds != null && !lastBounds.isNull()) {
                        PlayerBounds midBounds = new PlayerBounds(PongUtils.lerp(lastBounds.pt1, newBounds.pt1, changeAmount), PongUtils.lerp(lastBounds.pt2, newBounds.pt2, changeAmount));
                        cPlayers.get(p).setPlayerBounds(midBounds);
                    } else {
                        cPlayers.get(p).setPlayerBounds(newBounds);
                    }
                } else {
                    cPlayers.get(p).setPlayerBounds(bounds[p]);
                }
            }

            sendNewPositions = true;
        }

    }

    public PlayerBounds[] generatePlayerBounds() {
        ArrayList<Player> cPlayers = currentPlayers();
        PlayerBounds[] bounds = new PlayerBounds[cPlayers.size()];
        boolean randomOrder = false;
        double radius = 200 / (2 * Math.sin(Math.PI / cPlayers.size()));

        double[] center = {0, 0};
        double incr = Math.PI * 2 / cPlayers.size();
        if (cPlayers.size() > 2) {
            gameLoop.setInvisibleBounds(-1);
            for (int p = 0; p < cPlayers.size(); p++) {
                double angle1 = p * incr;
                double angle2 = (p + 1) * incr;
                PlayerBounds playerBounds = new PlayerBounds(new Point2D((int) (Math.cos(angle1) * radius + center[0]), (int) (Math.sin(angle1) * radius + center[1])), new Point2D(
                        (int) (Math.cos(angle2) * radius + center[0]), (int) (Math.sin(angle2) * radius + center[1])));
                bounds[p] = playerBounds;
            }
        } else if (cPlayers.size() == 2) {
            double arenaScale = 4;
            double aspectRatio = 1.78/2;
            PlayerBounds left = new PlayerBounds(new Point2D(radius * aspectRatio * arenaScale, -radius/2 * arenaScale), new Point2D(radius * aspectRatio * arenaScale, radius / 2 * arenaScale));
            PlayerBounds right = new PlayerBounds(new Point2D(-radius * aspectRatio * arenaScale, radius  / 2 * arenaScale), new Point2D(-radius * aspectRatio * arenaScale, -radius / 2 * arenaScale));
            bounds[0] = left;
            bounds[1] = right;
            gameLoop.setInvisibleBounds(radius / 2 * arenaScale);
        }
        return bounds;
    }

    public ArrayList<Player> currentPlayers() {
        ArrayList<Player> cPlayers = new ArrayList();
        for (Player p : players) {
            if (p.getState() == Player.State.INGAME) cPlayers.add(p);
        }
        return cPlayers;
    }

    public String getMessage() {
        return message;
    }

    public boolean needToUpdatePlayers() {
        if (sendNewPositions) {
            sendNewPositions = false;
            return true;
        } else {
            return false;
        }
    }

    public void updateEventList() {

        graphicsEvents.removeIf(e -> (System.currentTimeMillis() - e.getInitTime() >= 5000));
        for (Player p : players) {
            if (p.getState() == Player.State.JOINED) {
                graphicsEvents.add(new ChatEvent(p.getName() + " joined the game."));
                p.setState(Player.State.ELIMINATED);
                System.out.println(p.getName() + " joined the game.");
            }
            if (System.currentTimeMillis() - p.getLastPacketTime() >= 2000) {
                graphicsEvents.add(new ChatEvent(p.getName() + " left the game."));
                System.out.println(p.getName() + " left the game.");
                players.remove(p);
            }
        }

    }

    public GraphicsEvent[] getGraphicsEvents() {
        return graphicsEvents.toArray(new GraphicsEvent[0]);
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}


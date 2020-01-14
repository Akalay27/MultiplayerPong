package com.adam.pong.game;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameManager extends Thread {

    private final double countdownTime = 5000;
    private final int minPlayersToStart = 4;
    private boolean running;
    private List<Player> players;
    private GameLoop gameLoop;
    private boolean gameInProgress = false;
    private String message = "";
    private double timer = 0;
    boolean sendNewPositions = false;

    public GameManager(CopyOnWriteArrayList players, GameLoop gameLoop) {
        this.players = players;
        this.gameLoop = gameLoop;
    }
    @Override
    public void run() {
        running = true;
        while (running) {

            boolean waiting = true;

            while (waiting) {
                if (!gameInProgress && players.size() >= minPlayersToStart) {
                    timer = System.currentTimeMillis();
                    gameInProgress = true;
                }

                if (gameInProgress && System.currentTimeMillis() - timer < countdownTime) {
                    message = Integer.toString((int) (countdownTime/1000-(System.currentTimeMillis() - timer) / 1000+1));

                }

                if (gameInProgress && System.currentTimeMillis() - timer >= countdownTime) {
                    waiting = false;
                    message = "";
                }
            }

            for (Player p : players) {
                p.setState(Player.State.INGAME);
            }
            createNewBounds(1);
            gameLoop.resetBall();
            while (gameInProgress) {


                if (currentPlayers().size() <= 1) {
                    gameInProgress = false;
                }
                for (Player p : players) {
                    if (p.getState() == Player.State.DEATH) {
                        p.setState(Player.State.ELIMINATED);
                        while(createNewBounds(0.00001)) {
                            gameLoop.resetBall();
                        }
                    }
                }
            }

        }
    }

    public boolean createNewBounds(double amnt) {

        PlayerBounds[] bounds = generatePlayerBounds();
        ArrayList<Player> cPlayers = currentPlayers();
        boolean done = false;
        for (int p = 0; p < bounds.length; p++) {
            PlayerBounds lastBounds = cPlayers.get(p).getPlayerBounds();
            PlayerBounds newBounds = bounds[p];
            if (lastBounds != null) {
                PlayerBounds midBounds = new PlayerBounds(PongUtils.lerp(lastBounds.pt1, newBounds.pt1, amnt), PongUtils.lerp(lastBounds.pt2, newBounds.pt2, amnt));
                cPlayers.get(p).setPlayerBounds(midBounds);
                Point2D diff1 = midBounds.pt1.subtract(newBounds.pt1);
                Point2D diff2 = midBounds.pt2.subtract(newBounds.pt2);
                if (diff1.getX()+diff1.getY()+diff2.getX()+diff2.getY() < 0.1) {
                    cPlayers.get(p).setPlayerBounds(newBounds);
                    done = true;
                }
            } else {
                cPlayers.get(p).setPlayerBounds(newBounds);
                done = true;
            }

        }
        sendNewPositions = true;
        return done;
    }

    public PlayerBounds[] generatePlayerBounds() {
        ArrayList<Player> cPlayers = currentPlayers();
        PlayerBounds[] bounds = new PlayerBounds[cPlayers.size()];
        boolean randomOrder = false;
        double radius = 200/(2*Math.sin(Math.PI/cPlayers.size()));

        double[] center = {0,0};
        double incr = Math.PI*2/cPlayers.size();
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
            PlayerBounds left = new PlayerBounds(new Point2D(radius*arenaScale,-radius/2*arenaScale),new Point2D(radius*arenaScale,radius/2*arenaScale));
            PlayerBounds right = new PlayerBounds(new Point2D(-radius*arenaScale,radius/2*arenaScale),new Point2D(-radius*arenaScale,-radius/2*arenaScale));
            bounds[0] = left;
            bounds[1] = right;
            gameLoop.setInvisibleBounds(radius/2*arenaScale);
        }

        return bounds;
    }

    public ArrayList<Player> currentPlayers() {
        ArrayList<Player> cPlayers = new ArrayList<>();
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
}

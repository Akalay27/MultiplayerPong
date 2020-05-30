package com.adam.pong.game;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class CPPongClient extends PongClient {

    private final int TARGET_FPS = 60;
    private boolean running = false;
    private double distanceTolerance;
    private long lastLoopTime;

    // This isn't a resource because the DedicatedServer isn't using resources
    private static String[] botNames = {"Bot_Miguel","Bot_Isabella","Bot_Mateo","Bot_Savannah","Bot_Emilia","Bot_Maya","Bot_Elena","Bot_Gabriella","Bot_Santiago","Bot_Isla","Bot_Jose","Bot_Leonardo","Bot_Molly","Bot_Amy","Bot_Claire","Bot_DeShawn","Bot_DeAndre","Bot_Marquis","Bot_Darnell","Bot_Terrell","Bot_Malik","Bot_Trevon","Bot_Tyrone","Bot_Willie","Bot_Dominique","Bot_Demetrius","Bot_Reginald","Bot_Jamal","Bot_Maurice","Bot_Jalen","Bot_Darius","Bot_Xavier","Bot_Terrance","Bot_Andre","Bot_Darryl","Bot_Imani","Bot_Shanice","Bot_Nia","Bot_Deja","Bot_Kiara","Bot_Alexus","Bot_Raven","Bot_Liam","Bot_Noah","Bot_William","Bot_James","Bot_Oliver","Bot_Benjamin","Bot_Elijah","Bot_Elias","Bot_Lucas","Bot_Mason","Bot_Logan","Bot_Emma","Bot_Uniqua","Bot_Olivia","Bot_Ava","Bot_Isabella","Bot_Sophia","Bot_Charlotte","Bot_Mia","Bot_Amelia","Bot_Harper","Bot_Evelyn","Bot_Stephen","Bot_Adam","Bot_Garry","Bot_Garrett","Bot_Matthew","Bot_Matt","Bot_John","Bot_Robert","Bot_Michael","Bot_William","Bot_David","Bot_Richard","Bot_Joseph","Bot_Thomas","Bot_Charles","Bot_Christopher","Bot_Mark","Bot_Anthony","Bot_George","Bot_Roy","Bot_Sharkeisha","Bot_Mia","Bot_Shaiden","Bot_Charlotte","Bot_Abigail","Bot_Harper","Bot_Emily","Bot_Riley","Bot_Romeo","Bot_Nick"};

    public CPPongClient(byte[] address, int port) throws SocketException, UnknownHostException {
        super(botNames[(int)(botNames.length*Math.random())], address, port);
    }

    @Override
    public void run() {

        running = true;
        try {
            connectToServer();
            minimalUpdate(UserInput.NONE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setDistanceTolerance();
        while (running) {

            Paddle paddle = null;
            PlayerBounds bounds = null;
            UserInput userInput = UserInput.NONE;

            lastLoopTime = System.currentTimeMillis();
            for (int p = 0; p < players.length; p++) {
                if (players[p].getId() == id) {
                    paddle = players[p].getPaddle();
                    bounds = players[p].getPlayerBounds();

                }
            }


            if (!(paddle == null || paddle.isNull())) {
                Point2D paddleCenter = paddle.getCenter();
                Point2D projPointL = PongUtils.projectPointOnLine(bounds.pt1, paddleCenter, ballPosition);
                Point2D projPointR = PongUtils.projectPointOnLine(paddleCenter, bounds.pt2, ballPosition);


                boolean onLeft = PongUtils.isPointOnSegment(paddleCenter, bounds.pt1, projPointL, 0.001);
                boolean onRight = PongUtils.isPointOnSegment(paddleCenter, bounds.pt2, projPointR, 0.001);
                double distToCenter = projPointL.distance(paddleCenter);
                if (distToCenter > distanceTolerance) {
                    if (onLeft) userInput = UserInput.RIGHT;
                    if (onRight) userInput = UserInput.LEFT;
                }
            }

            if (Math.random() > 0.9) setDistanceTolerance();

            try {
                minimalUpdate(userInput);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                long duration = (long) ((lastLoopTime - System.currentTimeMillis()) + (1000.0 / TARGET_FPS));
                Thread.sleep((duration >= 0) ? duration : 0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void setDistanceTolerance() {
        distanceTolerance = Math.random() * GameLoop.paddleWidth / 3 + GameLoop.paddleWidth / 12;
    }


}

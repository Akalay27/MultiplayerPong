package com.adam.pong.game;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

// TODO: Add CPU client
public class CPPongClient extends PongClient {

    private boolean running = false;
    private double distanceTolerance;
    public CPPongClient (String name, byte[] address) throws SocketException, UnknownHostException {
        super(name,address);
    }

    @Override
    public void run() {

        running = true;
        try {
            connectToServer();
            update(UserInput.NONE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setDistanceTolerance();
        while (running) {

            Paddle paddle = null;
            PlayerBounds bounds = null;
            UserInput userInput = UserInput.NONE;

            boolean inGame = false;
            for (int p = 0; p < players.length; p++) {
                if (players[p].getId() == id) {
                    paddle = players[p].getPaddle();
                    bounds = players[p].getPlayerBounds();
                    inGame = true;
                }
            }
            if (!inGame) {
                running = false;
                System.out.println(getName() + " is out!");
                continue;

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
                update(userInput);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }

    public void setDistanceTolerance() {
        distanceTolerance = Math.random()*GameLoop.paddleWidth/3;
    }


}
